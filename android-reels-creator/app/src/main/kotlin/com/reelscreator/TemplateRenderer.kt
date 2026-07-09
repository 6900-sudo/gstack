package com.reelscreator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbFilter
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import com.google.common.collect.ImmutableList
import java.io.File

@OptIn(UnstableApi::class)
object TemplateRenderer {

    enum class DramaticStyle { CINEMATIC, SEPIA, NOIR }

    // ── Effects builders ──────────────────────────────────────────────────────

    fun resizeEffects(): Effects = Effects(
        emptyList(),
        listOf(Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
    )

    fun captionEffects(text: String): Effects {
        val overlay = staticOverlay(captionBitmap(text))
        return Effects(emptyList(), listOf(OverlayEffect(ImmutableList.of(overlay))))
    }

    fun txtOverlayEffects(lines: List<String>, videoDurationSec: Double): Effects {
        val sliceDurUs = (videoDurationSec * 1_000_000L / lines.size).toLong()
        val overlay = timedOverlay(lines, sliceDurUs)
        return Effects(emptyList(), listOf(OverlayEffect(ImmutableList.of(overlay))))
    }

    // May throw (OOM) — callers must catch Throwable
    fun dramaticEffects(style: DramaticStyle): List<Effect> {
        val grayscale = RgbFilter.createGrayscaleFilter()
        val presentation = Presentation.createForWidthAndHeight(
            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
        )
        return when (style) {
            DramaticStyle.CINEMATIC -> listOf(grayscale, presentation)
            DramaticStyle.SEPIA -> {
                val tint = colorTintOverlay(Color.argb(55, 120, 80, 20))
                listOf(grayscale, OverlayEffect(ImmutableList.of(tint)), presentation)
            }
            DramaticStyle.NOIR -> {
                val tint = colorTintOverlay(Color.argb(40, 10, 20, 60))
                listOf(grayscale, OverlayEffect(ImmutableList.of(tint)), presentation)
            }
        }
    }

    // May throw (OOM) — callers must catch Throwable
    fun breakingNewsEffects(headline: String): Effects {
        val overlay = staticOverlay(breakingNewsBitmap(headline))
        return Effects(
            emptyList(),
            listOf(
                OverlayEffect(ImmutableList.of(overlay)),
                Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP)
            )
        )
    }

    // ── Composition builders ──────────────────────────────────────────────────

    // Returns (composition, temp files to delete after export).  May throw.
    fun textSlideComposition(context: Context, lines: List<String>): Pair<Composition, List<File>> {
        val tmpFiles = mutableListOf<File>()
        val slides = lines.map { line ->
            val tmp = File.createTempFile("slide_", ".png", context.cacheDir)
            tmpFiles += tmp
            slideBitmap(line).let { bmp ->
                tmp.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bmp.recycle()
            }
            EditedMediaItem.Builder(
                MediaItem.Builder().setUri(Uri.fromFile(tmp)).setImageDurationMs(3000L).build()
            ).setEffects(Effects(
                emptyList(),
                listOf(Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
            )).build()
        }
        return Composition.Builder(listOf(EditedMediaItemSequence(slides))).build() to tmpFiles
    }

    // Returns (composition, temp files to delete after export).  May throw.
    fun newsReelComposition(context: Context, headlines: List<String>): Pair<Composition, List<File>> {
        val tmpFiles = mutableListOf<File>()
        val slides = headlines.map { headline ->
            val tmp = File.createTempFile("news_slide_", ".png", context.cacheDir)
            tmpFiles += tmp
            newsSliderBitmap(headline).let { bmp ->
                tmp.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bmp.recycle()
            }
            EditedMediaItem.Builder(
                MediaItem.Builder().setUri(Uri.fromFile(tmp)).setImageDurationMs(4500L).build()
            ).setEffects(Effects(
                emptyList(),
                listOf(Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
            )).build()
        }
        return Composition.Builder(listOf(EditedMediaItemSequence(slides))).build() to tmpFiles
    }

    // ── Overlay helpers ───────────────────────────────────────────────────────

    internal fun staticOverlay(bmp: Bitmap): BitmapOverlay =
        object : BitmapOverlay() {
            override fun getBitmap(presentationTimeUs: Long): Bitmap = bmp
            override fun release() { if (!bmp.isRecycled) bmp.recycle() }
        }

    private fun timedOverlay(lines: List<String>, sliceDurUs: Long): BitmapOverlay {
        val cache = object : LinkedHashMap<Int, Bitmap>(8, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Bitmap>?): Boolean {
                if (size > 4) {
                    eldest?.value?.let { if (!it.isRecycled) it.recycle() }
                    return true
                }
                return false
            }
        }
        return object : BitmapOverlay() {
            override fun getBitmap(presentationTimeUs: Long): Bitmap {
                val idx = (presentationTimeUs / sliceDurUs).toInt().coerceIn(0, lines.size - 1)
                return cache.getOrPut(idx) { captionBitmap(lines[idx]) }
            }
            override fun release() {
                cache.values.forEach { if (!it.isRecycled) it.recycle() }
                cache.clear()
            }
        }
    }

    private fun colorTintOverlay(argb: Int): BitmapOverlay {
        val bmp = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawColor(argb)
        return staticOverlay(bmp)
    }

    // ── Bitmap builders ───────────────────────────────────────────────────────

    private fun captionBitmap(text: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply { color = Color.argb(160, 0, 0, 0) }
        val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 60f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val y = h - 100f
        canvas.drawRect(0f, y - 80f, w.toFloat(), y + 30f, bgPaint)
        canvas.drawText(text.take(80), w / 2f, y, txtPaint)
        return bmp
    }

    private fun slideBitmap(text: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.BLACK)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 72f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val wrappedLines = wrapText(text, paint, (w - 120).toFloat(), Int.MAX_VALUE)
        val lineH = paint.textSize + 16f
        var y = (h - wrappedLines.size * lineH) / 2f + paint.textSize
        wrappedLines.forEach { line -> canvas.drawText(line, w / 2f, y, paint); y += lineH }
        return bmp
    }

    private fun newsSliderBitmap(headline: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.BLACK)
        Paint().apply { color = Color.RED }.also { canvas.drawRect(40f, 160f, 220f, 168f, it) }
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED; textSize = 38f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT
        }.also { canvas.drawText("BREAKING NEWS", 40f, 140f, it) }
        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 78f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT
        }
        var ty = 280f
        wrapText(headline, headlinePaint, 1000f, 5).forEach { line ->
            canvas.drawText(line, 40f, ty, headlinePaint); ty += headlinePaint.textSize + 20f
        }
        return bmp
    }

    private fun breakingNewsBitmap(headline: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        Paint().apply { color = Color.RED }
            .also { canvas.drawRect(0f, (h - 220).toFloat(), w.toFloat(), (h - 140).toFloat(), it) }
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 52f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT
        }.also { canvas.drawText("⚡ BREAKING", 40f, (h - 158).toFloat(), it) }
        Paint().apply { color = Color.argb(200, 0, 0, 0) }
            .also { canvas.drawRect(0f, (h - 140).toFloat(), w.toFloat(), h.toFloat(), it) }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 44f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT
        }
        val wrappedLines = wrapText(headline, textPaint, 1000f, 3)
        val lineH = textPaint.textSize + 10f
        val totalH = wrappedLines.size * lineH
        var ty = (h - 140).toFloat() + (140 - totalH) / 2f + textPaint.textSize
        wrappedLines.forEach { line -> canvas.drawText(line, 40f, ty, textPaint); ty += lineH }
        return bmp
    }

    internal fun wrapText(text: String, paint: Paint, maxWidth: Float, maxLines: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var cur = StringBuilder()
        for (word in words) {
            if (lines.size >= maxLines) break
            val test = if (cur.isEmpty()) word else "$cur $word"
            if (paint.measureText(test) > maxWidth) {
                if (cur.isNotEmpty()) lines += cur.toString()
                cur = StringBuilder(word)
            } else {
                cur = StringBuilder(test)
            }
        }
        if (cur.isNotEmpty() && lines.size < maxLines) lines += cur.toString()
        return lines
    }
}
