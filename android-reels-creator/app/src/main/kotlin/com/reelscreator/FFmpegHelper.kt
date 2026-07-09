package com.reelscreator

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
object FFmpegHelper {

    fun cancel() = ReelEngine.cancel()

    fun trimVideo(context: Context, input: String, output: String,
                  startSec: Double, durationSec: Double, onDone: (Boolean) -> Unit) {
        val item = EditedMediaItem.Builder(
            MediaItem.Builder()
                .setUri(Uri.parse("file://$input"))
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs((startSec * 1000).toLong())
                        .setEndPositionMs(((startSec + durationSec) * 1000).toLong())
                        .build()
                )
                .build()
        ).build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun mergeClips(context: Context, inputs: List<String>, output: String, onDone: (Boolean) -> Unit) {
        val items = inputs.map { p ->
            EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$p"))).build()
        }
        val composition = Composition.Builder(listOf(EditedMediaItemSequence(items))).build()
        ReelEngine.start(context, composition, output, onDone = onDone)
    }

    fun addAudio(videoInput: String, audioInput: String, output: String, onDone: (Boolean) -> Unit) {
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        Thread {
            val ok = try { muxVideoWithAudio(videoInput, audioInput, output); true } catch (_: Exception) { false }
            mainHandler.post { onDone(ok) }
        }.start()
    }

    fun resizeToReels(context: Context, input: String, output: String, onDone: (Boolean) -> Unit) {
        val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(TemplateRenderer.resizeEffects())
            .build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun addTextOverlay(context: Context, input: String, output: String,
                       text: String, onDone: (Boolean) -> Unit) {
        val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(TemplateRenderer.captionEffects(text))
            .build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun textToVideo(context: Context, lines: List<String>, output: String,
                    onProgress: ((Int) -> Unit)? = null, onDone: (Boolean) -> Unit) {
        FFmpegRenderWorker.renderSlides(context, lines, output, onProgress, onDone)
    }

    fun addTxtOverlay(context: Context, input: String, output: String,
                      lines: List<String>, videoDurationSec: Double, onDone: (Boolean) -> Unit) {
        if (lines.isEmpty()) { onDone(false); return }
        val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(TemplateRenderer.txtOverlayEffects(lines, videoDurationSec))
            .build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun applyDramaticEffect(context: Context, input: String, output: String,
                            style: TemplateRenderer.DramaticStyle, onDone: (Boolean) -> Unit) {
        val effects = try {
            TemplateRenderer.dramaticEffects(style)
        } catch (_: Throwable) {
            onDone(false)
            return
        }
        val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(Effects(emptyList(), effects))
            .build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun addBreakingNewsOverlay(context: Context, input: String, output: String,
                               headline: String, onDone: (Boolean) -> Unit) {
        val effects = try {
            TemplateRenderer.breakingNewsEffects(headline)
        } catch (_: Throwable) {
            onDone(false)
            return
        }
        val item = EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse("file://$input")))
            .setEffects(effects)
            .build()
        ReelEngine.startSingle(context, item, output, onDone)
    }

    fun dramaticNewsReel(context: Context, headlines: List<String>, output: String,
                         onProgress: ((Int) -> Unit)? = null, onDone: (Boolean) -> Unit) {
        FFmpegRenderWorker.renderNewsReel(context, headlines, output, onProgress, onDone)
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
