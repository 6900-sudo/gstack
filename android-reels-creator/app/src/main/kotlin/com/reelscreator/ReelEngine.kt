package com.reelscreator

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer

@OptIn(UnstableApi::class)
object ReelEngine {

    private const val TAG = "ReelEngine"

    @Volatile private var active: Transformer? = null

    fun cancel() {
        active?.cancel()
        active = null
    }

    @MainThread
    fun start(
        context: Context,
        composition: Composition,
        output: String,
        onProgress: ((Int) -> Unit)? = null,
        onDone: (Boolean, String?) -> Unit
    ) {
        try {
            active?.cancel()
            val transformer = build(context, onDone)
            active = transformer
            transformer.start(composition, output)
            onProgress?.let { startProgressPolling(transformer, it) }
        } catch (e: Throwable) {
            Log.e(TAG, "start() threw", e)
            onDone(false, e.message ?: e.javaClass.simpleName)
        }
    }

    @MainThread
    fun startSingle(
        context: Context,
        item: EditedMediaItem,
        output: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        try {
            active?.cancel()
            val transformer = build(context, onDone)
            active = transformer
            transformer.start(item, output)
        } catch (e: Throwable) {
            Log.e(TAG, "startSingle() threw", e)
            onDone(false, e.message ?: e.javaClass.simpleName)
        }
    }

    private fun build(context: Context, onDone: (Boolean, String?) -> Unit): Transformer =
        Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    active = null
                    onDone(true, null)
                }
                override fun onError(composition: Composition, exportResult: ExportResult,
                                     exportException: ExportException) {
                    active = null
                    val msg = buildString {
                        append(exportException.errorCodeName)
                        exportException.message?.takeIf { it.isNotBlank() }?.let { append(": ").append(it) }
                        exportException.cause?.message?.takeIf { it.isNotBlank() }?.let { append(" (").append(it).append(")") }
                    }
                    Log.e(TAG, "Transformer error: $msg", exportException)
                    onDone(false, msg)
                }
            })
            .build()

    private fun startProgressPolling(transformer: Transformer, onProgress: (Int) -> Unit) {
        val holder = ProgressHolder()
        val handler = Handler(Looper.getMainLooper())
        val poll = object : Runnable {
            override fun run() {
                val state = transformer.getProgress(holder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onProgress(holder.progress)
                }
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    handler.postDelayed(this, 200)
                }
            }
        }
        handler.post(poll)
    }
}
