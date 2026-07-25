package com.reelscreator

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
object FFmpegRenderWorker {

    fun renderSlides(
        context: Context,
        lines: List<String>,
        output: String,
        onProgress: ((Int) -> Unit)? = null,
        onDone: (Boolean, String?) -> Unit
    ) {
        Thread {
            val (composition, tmpFiles) = try {
                TemplateRenderer.textSlideComposition(context, lines)
            } catch (e: Throwable) {
                val msg = "Slide render failed: ${e.message ?: e.javaClass.simpleName}"
                Handler(Looper.getMainLooper()).post { onDone(false, msg) }
                return@Thread
            }
            Handler(Looper.getMainLooper()).post {
                ReelEngine.start(context, composition, output, onProgress) { ok, err ->
                    tmpFiles.forEach { it.delete() }
                    onDone(ok, err)
                }
            }
        }.start()
    }

    fun renderNewsReel(
        context: Context,
        headlines: List<String>,
        output: String,
        onProgress: ((Int) -> Unit)? = null,
        onDone: (Boolean, String?) -> Unit
    ) {
        Thread {
            val (composition, tmpFiles) = try {
                TemplateRenderer.newsReelComposition(context, headlines)
            } catch (e: Throwable) {
                val msg = "News reel render failed: ${e.message ?: e.javaClass.simpleName}"
                Handler(Looper.getMainLooper()).post { onDone(false, msg) }
                return@Thread
            }
            Handler(Looper.getMainLooper()).post {
                ReelEngine.start(context, composition, output, onProgress) { ok, err ->
                    tmpFiles.forEach { it.delete() }
                    onDone(ok, err)
                }
            }
        }.start()
    }
}
