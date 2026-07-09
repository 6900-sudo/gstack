package com.reelscreator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.Looper
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.nio.ByteBuffer

object FFmpegHelper {

    enum class DramaticStyle { CINEMATIC, SEPIA, NOIR }

    private val mainHandler = Handler(Looper.getMainLooper())

    fun cancel() = FFmpegKit.cancel()

    private fun exec(cmd: String, onDone: (Boolean) -> Unit) {
        FFmpegKit.executeAsync(cmd) { session ->
            val ok = ReturnCode.isSuccess(session.returnCode)
            mainHandler.post { onDone(ok) }
        }
    }

    fun trimVideo(context: Context, input: String, output: String,
                  startSec: Double, durationSec: Double, onDone: (Boolean) -> Unit) {
        exec("-y -ss $startSec -i \"$input\" -t $durationSec -c copy \"$output\"", onDone)
    }

    fun mergeClips(context: Context, inputs: List<String>, output: String, onDone: (Boolean) -> Unit) {
        val listFile = File(context.cacheDir, "concat_${System.currentTimeMillis()}.txt")
        listFile.writeText(inputs.joinToString("\n") { "file '${it.replace("'", "\\'")}'" })
        FFmpegKit.executeAsync(
            "-y -f concat -safe 0 -i \"${listFile.absolutePath}\" -c copy \"$output\""
        ) { session ->
            listFile.delete()
            val ok = ReturnCode.isSuccess(session.returnCode)
            mainHandler.post { onDone(ok) }
        }
    }

    fun addAudio(videoInput: String, audioInput: String, output: String, onDone: (Boolean) -> Unit) {
        Thread {
            val ok = try { muxVideoWithAudio(videoInput, audioInput, output); true } catch (_: Exception) { false }
            mainHandler.post { onDone(ok) }
        }.start()
    }

    fun resizeToReels(context: Context, input: String, output: String, onDone: (Boolean) -> Unit) {
        val vf = "scale=1080:1920:force_original_aspect_ratio=decrease," +
                 "pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black,format=yuv420p"
        exec("-y -i \"$input\" -vf \"$vf\" -c:v h264_mediacodec -b:v 4M -c:a aac \"$output\"", onDone)
    }

    fun addTextOverlay(context: Context, input: String, output: String,
                       text: String, onDone: (Boolean) -> Unit) {
        val escaped = text.take(80).ffEscape()
        val vf = "drawtext=text='$escaped':fontsize=60:fontcolor=white:" +
                 "box=1:boxcolor=black@0.6:boxborderw=10:x=(w-text_w)/2:y=h-160,format=yuv420p"
        exec("-y -i \"$input\" -vf \"$vf\" -c:v h264_mediacodec -b:v 4M -c:a aac \"$output\"", onDone)
    }

    fun textToVideo(context: Context, lines: List<String>, output: String,
                    onProgress: ((Int) -> Unit)? = null, onDone: (Boolean) -> Unit) {
        Thread {
            try {
                val clips = lines.mapIndexed { i, line ->
                    val png = File(context.cacheDir, "slide_${i}_${System.currentTimeMillis()}.png")
                    val bmp = slideBitmap(line)
                    png.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bmp.recycle()
                    val clip = File(context.cacheDir, "clip_${i}_${System.currentTimeMillis()}.mp4")
                    val s = FFmpegKit.execute(
                        "-y -loop 1 -i \"${png.absolutePath}\" -t 3 " +
                        "-vf \"scale=1080:1920,format=yuv420p\" " +
                        "-c:v h264_mediacodec -b:v 4M -r 25 \"${clip.absolutePath}\""
                    )
                    png.delete()
                    if (!ReturnCode.isSuccess(s.returnCode)) throw Exception("slide $i failed")
                    mainHandler.post { onProgress?.invoke((i + 1) * 100 / lines.size) }
                    clip
                }
                val listFile = File(context.cacheDir, "concat_${System.currentTimeMillis()}.txt")
                listFile.writeText(clips.joinToString("\n") { "file '${it.absolutePath}'" })
                val s = FFmpegKit.execute(
                    "-y -f concat -safe 0 -i \"${listFile.absolutePath}\" -c copy \"$output\""
                )
                listFile.delete()
                clips.forEach { it.delete() }
                mainHandler.post { onDone(ReturnCode.isSuccess(s.returnCode)) }
            } catch (_: Exception) {
                mainHandler.post { onDone(false) }
            }
        }.start()
    }

    fun addTxtOverlay(context: Context, input: String, output: String,
                      lines: List<String>, videoDurationSec: Double, onDone: (Boolean) -> Unit) {
        if (lines.isEmpty() || videoDurationSec <= 0.0) { mainHandler.post { onDone(false) }; return }
        val sliceDur = videoDurationSec / lines.size
        val filters = lines.mapIndexed { i, line ->
            val esc = line.take(60).ffEscape()
            val t0 = "%.3f".format(i * sliceDur)
            val t1 = "%.3f".format((i + 1) * sliceDur)
            "drawtext=text='$esc':fontsize=52:fontcolor=white:" +
            "box=1:boxcolor=black@0.6:boxborderw=8:x=(w-text_w)/2:y=h-160:" +
            "enable='between(t,$t0,$t1)'"
        }.joinToString(",") + ",format=yuv420p"
        exec("-y -i \"$input\" -vf \"$filters\" -c:v h264_mediacodec -b:v 4M -c:a aac \"$output\"", onDone)
    }

    fun applyDramaticEffect(context: Context, input: String, output: String,
                            style: DramaticStyle, onDone: (Boolean) -> Unit) {
        val scale = "scale=1080:1920:force_original_aspect_ratio=decrease," +
                    "pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black"
        val vf = when (style) {
            DramaticStyle.CINEMATIC -> "hue=s=0,$scale,format=yuv420p"
            DramaticStyle.SEPIA ->
                "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131,$scale,format=yuv420p"
            DramaticStyle.NOIR ->
                "hue=s=0,curves=all='0/0 0.25/0 0.75/1 1/1',$scale,format=yuv420p"
        }
        exec("-y -i \"$input\" -vf \"$vf\" -c:v h264_mediacodec -b:v 4M -c:a aac \"$output\"", onDone)
    }

    fun addBreakingNewsOverlay(context: Context, input: String, output: String,
                               headline: String, onDone: (Boolean) -> Unit) {
        val overlay = File(context.cacheDir, "bn_${System.currentTimeMillis()}.png")
        try {
            val bmp = breakingNewsBitmap(headline)
            overlay.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bmp.recycle()
        } catch (_: Exception) {
            mainHandler.post { onDone(false) }
            return
        }
        FFmpegKit.executeAsync(
            "-y -i \"$input\" -i \"${overlay.absolutePath}\" " +
            "-filter_complex \"[0:v][1:v]overlay=0:0,format=yuv420p\" " +
            "-c:v h264_mediacodec -b:v 4M -c:a aac \"$output\""
        ) { session ->
            overlay.delete()
            val ok = ReturnCode.isSuccess(session.returnCode)
            mainHandler.post { onDone(ok) }
        }
    }

    fun dramaticNewsReel(context: Context, headlines: List<String>, output: String,
                         onProgress: ((Int) -> Unit)? = null, onDone: (Boolean) -> Unit) {
        Thread {
            try {
                val clips = headlines.mapIndexed { i, headline ->
                    val png = File(context.cacheDir, "news_${i}_${System.currentTimeMillis()}.png")
                    val bmp = newsSliderBitmap(headline)
                    png.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bmp.recycle()
                    val clip = File(context.cacheDir, "newsclip_${i}_${System.currentTimeMillis()}.mp4")
                    val s = FFmpegKit.execute(
                        "-y -loop 1 -i \"${png.absolutePath}\" -t 4.5 " +
                        "-vf \"scale=1080:1920,format=yuv420p\" " +
                        "-c:v h264_mediacodec -b:v 4M -r 25 \"${clip.absolutePath}\""
                    )
                    png.delete()
                    if (!ReturnCode.isSuccess(s.returnCode)) throw Exception("news slide $i failed")
                    mainHandler.post { onProgress?.invoke((i + 1) * 100 / headlines.size) }
                    clip
                }
                val listFile = File(context.cacheDir, "newscats_${System.currentTimeMillis()}.txt")
                listFile.writeText(clips.joinToString("\n") { "file '${it.absolutePath}'" })
                val s = FFmpegKit.execute(
                    "-y -f concat -safe 0 -i \"${listFile.absolutePath}\" -c copy \"$output\""
                )
                listFile.delete()
                clips.forEach { it.delete() }
                mainHandler.post { onDone(ReturnCode.isSuccess(s.returnCode)) }
            } catch (_: Exception) {
                mainHandler.post { onDone(false) }
            }
        }.start()
    }

    // ── FFmpeg string escaping for drawtext ──────────────────────────────────

    private fun String.ffEscape(): String =
        replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace(":", "\\:")
            .replace("[", "\\[")
            .replace("]", "\\]")

    // ── Bitmap builders ───────────────────────────────────────────────────────

    private fun slideBitmap(text: String): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.BLACK)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 72f
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        val wrapped = wrapText(text, paint, (w - 120).toFloat(), Int.MAX_VALUE)
        val lineH = paint.textSize + 16f
        var y = (h - wrapped.size * lineH) / 2f + paint.textSize
        wrapped.forEach { line -> canvas.drawText(line, w / 2f, y, paint); y += lineH }
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
        val wrapped = wrapText(headline, textPaint, 1000f, 3)
        val lineH = textPaint.textSize + 10f
        val totalH = wrapped.size * lineH
        var ty = (h - 140).toFloat() + (140 - totalH) / 2f + textPaint.textSize
        wrapped.forEach { line -> canvas.drawText(line, 40f, ty, textPaint); ty += lineH }
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

    // ── Audio muxer ───────────────────────────────────────────────────────────

    private fun muxVideoWithAudio(videoPath: String, audioPath: String, outputPath: String) {
        val vEx = MediaExtractor()
        val aEx = MediaExtractor()
        try {
            vEx.setDataSource(videoPath)
            aEx.setDataSource(audioPath)

            val vTrack = (0 until vEx.trackCount).firstOrNull {
                vEx.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            } ?: throw IllegalArgumentException("No video track in $videoPath")
            val aTrack = (0 until aEx.trackCount).firstOrNull {
                aEx.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw IllegalArgumentException("No audio track in $audioPath")
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
