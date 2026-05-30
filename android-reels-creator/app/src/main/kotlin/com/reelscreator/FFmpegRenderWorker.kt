package com.reelscreator

import android.content.Context
import android.os.Handler
import android.os.Looper

object FFmpegRenderWorker {

    fun renderSlides(
        context: Context,
        lines: List<String>,
        output: String,
        onProgress: ((Int) -> Unit)? = null,
        onDone: (Boolean) -> Unit
    ) {
        Thread {
            val (composition, tmpFiles) = try {
                TemplateRenderer.textSlideComposition(context, lines)
            } catch (_: Throwable) {
                Handler(Looper.getMainLooper()).post { onDone(false) }
                return@Thread
            }
            Handler(Looper.getMainLooper()).post {
                ReelEngine.start(context, composition, output, onProgress) { ok ->
                    tmpFiles.forEach { it.delete() }
                    onDone(ok)
                }
            }
        }.start()
    }

    fun renderNewsReel(
        context: Context,
        headlines: List<String>,
        output: String,
        onProgress: ((Int) -> Unit)? = null,
        onDone: (Boolean) -> Unit
    ) {
        Thread {
            val (composition, tmpFiles) = try {
                TemplateRenderer.newsReelComposition(context, headlines)
            } catch (_: Throwable) {
                Handler(Looper.getMainLooper()).post { onDone(false) }
                return@Thread
            }
            Handler(Looper.getMainLooper()).post {
                ReelEngine.start(context, composition, output, onProgress) { ok ->
                    tmpFiles.forEach { it.delete() }
                    onDone(ok)
                }
            }
        }.start()
    }
}
