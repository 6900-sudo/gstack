package com.reelscreator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import androidx.annotation.OptIn
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
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import java.io.File
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
object FFmpegHelper {

    @Volatile private var activeTransformer: Transformer? = null

    fun cancel() {
        activeTransformer?.cancel()
        activeTransformer = null
    }

    fun trimVideo(context: Context, input: String, output: String,
                  startSec: Double, durationSec: Double, onDone: (Boolean) -> Unit) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse("file://$input"))
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs((startSec * 1000).toLong())
                    .setEndPositionMs(((startSec + durationSec) * 1000).toLong())
                    .build()
            )
            .build()
        startSingle(context, EditedMediaItem.Builder(mediaItem).build(), output, onDone)
    }

    fun mergeClips(context: Context, inputs: List<String>, output: String, onDone: (Boolean) -> Unit) {
        val items = inputs.map { p ->
            EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$p"))).build()
        }
        val composition = Composition.Builder(listOf(EditedMediaItemSequence(items))).build()
        buildTransformer(context, onDone).start(composition, output)
    }

    fun addAudio(videoInput: String, audioInput: String, output: String, onDone: (Boolean) -> Unit) {
        Thread {
            try {
                muxVideoWithAudio(videoInput, audioInput, output)
                onDone(true)
            } catch (_: Exception) {
                onDone(false)
            }
        }.start()
    }

    fun resizeToReels(context: Context, input: String, output: String, onDone: (Boolean) -> Unit) {
        val effects = Effects(
            emptyList(),
            listOf(Presentation.createForWidthAndHeight(
                1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
        )
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(effects)
            .build()
        startSingle(context, editedItem, output, onDone)
    }

    fun addTextOverlay(context: Context, input: String, output: String,
                       text: String, onDone: (Boolean) -> Unit) {
        val overlay = staticOverlay(captionBitmap(text))
        val effects = Effects(emptyList(), listOf(OverlayEffect(ImmutableList.of(overlay))))
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(effects)
            .build()
        startSingle(context, editedItem, output, onDone)
    }

    fun textToVideo(context: Context, lines: List<String>, output: String, onDone: (Boolean) -> Unit) {
        Thread {
            val tmpFiles = mutableListOf<File>()
            try {
                val slides = lines.map { line ->
                    val tmp = File.createTempFile("slide_", ".png", context.cacheDir)
                    tmpFiles += tmp
                    slideBitmap(line).let { bmp ->
                        tmp.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bmp.recycle()
                    }
                    EditedMediaItem.Builder(
                        MediaItem.Builder()
                            .setUri(Uri.fromFile(tmp))
                            .setImageDurationMs(3000L)
                            .build()
                    ).setEffects(Effects(
                        emptyList(),
                        listOf(Presentation.createForWidthAndHeight(
                            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
                    )).build()
                }
                val composition = Composition.Builder(listOf(EditedMediaItemSequence(slides))).build()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        buildTransformer(context) { ok ->
                            tmpFiles.forEach { it.delete() }
                            onDone(ok)
                        }.start(composition, output)
                    } catch (e: Throwable) {
                        tmpFiles.forEach { it.delete() }
                        onDone(false)
                    }
                }
            } catch (e: Throwable) {
                tmpFiles.forEach { it.delete() }
                onDone(false)
            }
        }.start()
    }

    fun addTxtOverlay(context: Context, input: String, output: String,
                      lines: List<String>, videoDurationSec: Double, onDone: (Boolean) -> Unit) {
        if (lines.isEmpty()) { onDone(false); return }
        val sliceDurUs = (videoDurationSec * 1_000_000L / lines.size).toLong()
        val overlay = timedOverlay(lines, sliceDurUs)
        val effects = Effects(emptyList(), listOf(OverlayEffect(ImmutableList.of(overlay))))
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(effects)
            .build()
        startSingle(context, editedItem, output, onDone)
    }

    // ── Dramatic Effects ──────────────────────────────────────────────────────

    enum class DramaticStyle { CINEMATIC, SEPIA, NOIR }

    fun applyDramaticEffect(context: Context, input: String, output: String,
                            style: DramaticStyle, onDone: (Boolean) -> Unit) {
        val grayscale = RgbFilter.createGrayscaleFilter()
        val presentation = Presentation.createForWidthAndHeight(
            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
        )
        val effects: List<androidx.media3.common.Effect> = when (style) {
            DramaticStyle.CINEMATIC -> listOf(grayscale, presentation)
            DramaticStyle.SEPIA -> {
                val tintOverlay = colorTintOverlay(Color.argb(55, 120, 80, 20))
                listOf(grayscale, OverlayEffect(ImmutableList.of(tintOverlay)), presentation)
            }
            DramaticStyle.NOIR -> {
                val tintOverlay = colorTintOverlay(Color.argb(40, 10, 20, 60))
                listOf(grayscale, OverlayEffect(ImmutableList.of(tintOverlay)), presentation)
            }
        }
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(Effects(emptyList(), effects))
            .build()
        startSingle(context, editedItem, output, onDone)
    }

    // ── Breaking News Overlay ─────────────────────────────────────────────────

    fun addBreakingNewsOverlay(context: Context, input: String, output: String,
                               headline: String, onDone: (Boolean) -> Unit) {
        val overlay = staticOverlay(breakingNewsBitmap(headline))
        val effects = Effects(
            emptyList(),
            listOf(
                OverlayEffect(ImmutableList.of(overlay)),
                Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP)
            )
        )
        val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(effects)
            .build()
        startSingle(context, editedItem, output, onDone)
    }

    fun dramaticNewsReel(context: Context, headlines: List<String>, output: String,
                         onDone: (Boolean) -> Unit) {
        Thread {
            val tmpFiles = mutableListOf<File>()
            try {
                val slides = headlines.map { headline ->
                    val tmp = File.createTempFile("news_slide_", ".png", context.cacheDir)
                    tmpFiles += tmp
                    newsSliderBitmap(headline).let { bmp ->
                        tmp.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bmp.recycle()
                    }
                    EditedMediaItem.Builder(
                        MediaItem.Builder()
                            .setUri(Uri.fromFile(tmp))
                            .setImageDurationMs(4500L)
                            .build()
                    ).setEffects(Effects(
                        emptyList(),
                        listOf(Presentation.createForWidthAndHeight(
                            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
                    )).build()
                }
                val composition = Composition.Builder(listOf(EditedMediaItemSequence(slides))).build()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        buildTransformer(context) { ok ->
                            tmpFiles.forEach { it.delete() }
                            onDone(ok)
                        }.start(composition, output)
                    } catch (e: Throwable) {
                        tmpFiles.forEach { it.delete() }
                        onDone(false)
                    }
                }
            } catch (e: Throwable) {
                tmpFiles.forEach { it.delete() }
                onDone(false)
            }
        }.start()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun startSingle(context: Context, editedItem: EditedMediaItem,
                            output: String, onDone: (Boolean) -> Unit) {
        buildTransformer(context, onDone).start(editedItem, output)
    }

    private fun buildTransformer(context: Context, onDone: (Boolean) -> Unit): Transformer {
        activeTransformer?.cancel()
        return Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    activeTransformer = null
                    onDone(true)
                }
                override fun onError(composition: Composition, exportResult: ExportResult,
                                     exportException: ExportException) {
                    activeTransformer = null
                    onDone(false)
                }
            })
            .build()
            .also { activeTransformer = it }
    }

    private fun staticOverlay(bmp: Bitmap): BitmapOverlay =
        object : BitmapOverlay() {
            override fun getBitmap(presentationTimeUs: Long): Bitmap = bmp
            override fun release() { if (!bmp.isRecycled) bmp.recycle() }
        }

    private fun timedOverlay(lines: List<String>, sliceDurUs: Long): BitmapOverlay {
        val cache = LinkedHashMap<Int, Bitmap>(4, 0.75f, true)
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

    private fun captionBitmap(text: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply { color = Color.argb(160, 0, 0, 0) }
        val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 60f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
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
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val maxW = (w - 120).toFloat()
        val words = text.split(" ")
        val wrappedLines = mutableListOf<String>()
        var cur = StringBuilder()
        for (word in words) {
            val test = if (cur.isEmpty()) word else "$cur $word"
            if (paint.measureText(test) > maxW) {
                wrappedLines += cur.toString()
                cur = StringBuilder(word)
            } else {
                cur = StringBuilder(test)
            }
        }
        if (cur.isNotEmpty()) wrappedLines += cur.toString()

        val lineH = paint.textSize + 16f
        var y = (h - wrappedLines.size * lineH) / 2f + paint.textSize
        wrappedLines.forEach { line ->
            canvas.drawText(line, w / 2f, y, paint)
            y += lineH
        }
        return bmp
    }

    private fun colorTintOverlay(argb: Int): BitmapOverlay {
        val bmp = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawColor(argb)
        return staticOverlay(bmp)
    }

    private fun breakingNewsBitmap(headline: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Red banner strip
        val redPaint = Paint().apply { color = Color.RED }
        canvas.drawRect(0f, (h - 220).toFloat(), w.toFloat(), (h - 140).toFloat(), redPaint)

        // "⚡ BREAKING" white bold left-aligned
        val breakingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("⚡ BREAKING", 40f, (h - 158).toFloat(), breakingPaint)

        // Dark semi-transparent lower strip
        val darkPaint = Paint().apply { color = Color.argb(200, 0, 0, 0) }
        canvas.drawRect(0f, (h - 140).toFloat(), w.toFloat(), h.toFloat(), darkPaint)

        // Word-wrap headline text in white
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        val wrappedLines = wrapText(headline, textPaint, 1000f, 3)
        val lineH = textPaint.textSize + 10f
        val totalH = wrappedLines.size * lineH
        var ty = (h - 140).toFloat() + (140 - totalH) / 2f + textPaint.textSize
        wrappedLines.forEach { line ->
            canvas.drawText(line, 40f, ty, textPaint)
            ty += lineH
        }
        return bmp
    }

    private fun newsSliderBitmap(headline: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.BLACK)

        // Red accent bar
        val accentPaint = Paint().apply { color = Color.RED }
        canvas.drawRect(40f, 160f, 220f, 168f, accentPaint)

        // "BREAKING NEWS" label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            textSize = 38f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("BREAKING NEWS", 40f, 140f, labelPaint)

        // Headline text white large, word-wrapped
        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 78f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        val wrappedLines = wrapText(headline, headlinePaint, 1000f, 5)
        val lineH = headlinePaint.textSize + 20f
        var ty = 280f
        wrappedLines.forEach { line ->
            canvas.drawText(line, 40f, ty, headlinePaint)
            ty += lineH
        }
        return bmp
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float, maxLines: Int): List<String> {
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

    private fun muxVideoWithAudio(videoPath: String, audioPath: String, outputPath: String) {
        val vEx = MediaExtractor()
        val aEx = MediaExtractor()
        try {
            vEx.setDataSource(videoPath)
            aEx.setDataSource(audioPath)

            val vTrack = (0 until vEx.trackCount).firstOrNull {
                vEx.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            } ?: throw IllegalArgumentException("No video track found in $videoPath")
            val aTrack = (0 until aEx.trackCount).firstOrNull {
                aEx.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw IllegalArgumentException("No audio track found in $audioPath")
            vEx.selectTrack(vTrack)
            aEx.selectTrack(aTrack)

            val vFmt = vEx.getTrackFormat(vTrack)
            val aFmt = aEx.getTrackFormat(aTrack)
            val durUs = runCatching { vFmt.getLong(MediaFormat.KEY_DURATION) }.getOrDefault(Long.MAX_VALUE)

            val bufSize = maxOf(
                runCatching { vFmt.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) }.getOrDefault(0),
                runCatching { aFmt.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) }.getOrDefault(0),
                2 * 1024 * 1024
            )
            val buf = ByteBuffer.allocate(bufSize)
            val info = MediaCodec.BufferInfo()

            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            try {
                val muxV = muxer.addTrack(vFmt)
                val muxA = muxer.addTrack(aFmt)
                muxer.start()

                fun copyTrack(ex: MediaExtractor, muxTrack: Int, limitUs: Long = Long.MAX_VALUE) {
                    while (true) {
                        val size = ex.readSampleData(buf, 0)
                        if (size < 0 || ex.sampleTime > limitUs) break
                        info.set(0, size, ex.sampleTime,
                            if (ex.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0)
                                MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
                        muxer.writeSampleData(muxTrack, buf, info)
                        ex.advance()
                    }
                }

                copyTrack(vEx, muxV)
                copyTrack(aEx, muxA, durUs)
                muxer.stop()
            } finally {
                muxer.release()
            }
        } finally {
            vEx.release()
            aEx.release()
        }
    }
}
