package com.reelscreator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReelsState(
    val isProcessing: Boolean = false,
    val progress: Double = 0.0,
    val outputPath: String? = null,
    val error: String? = null
)

data class NewsState(
    val isLoading: Boolean = false,
    val items: List<NewsItem> = emptyList(),
    val error: String? = null,
    val sourceName: String = ""
)

class ReelsViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _state = MutableStateFlow(ReelsState())
    val state: StateFlow<ReelsState> = _state.asStateFlow()

    private val _newsState = MutableStateFlow(NewsState())
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()

    private val _busy = AtomicBoolean(false)

    // ── Processing state helpers ──────────────────────────────────────────

    private fun processing() = _state.value.copy(isProcessing = true, error = null, outputPath = null)
    private fun done(path: String?) = _state.value.copy(isProcessing = false, outputPath = path, progress = 0.0)
        .also { _busy.set(false) }
    private fun failed(msg: String) = _state.value.copy(isProcessing = false, error = msg, progress = 0.0)
        .also { _busy.set(false) }
    private fun updateProgress(pct: Int) {
        _state.value = _state.value.copy(progress = pct / 100.0)
    }

    private fun guardBusy(): Boolean = !_busy.compareAndSet(false, true)

    override fun onCleared() {
        super.onCleared()
        FFmpegHelper.cancel()
        _busy.set(false)
    }

    fun clearResult() {
        _state.value = _state.value.copy(outputPath = null, error = null)
    }

    // ── News feed ─────────────────────────────────────────────────────────

    fun fetchNews(url: String, sourceName: String) {
        viewModelScope.launch {
            _newsState.value = NewsState(isLoading = true, sourceName = sourceName)
            RssRepository.fetch(url)
                .onSuccess { items ->
                    _newsState.value = NewsState(items = items, sourceName = sourceName)
                }
                .onFailure { e ->
                    _newsState.value = NewsState(
                        error = e.message ?: "Network error — check your connection",
                        sourceName = sourceName
                    )
                }
        }
    }

    // ── Video operations ──────────────────────────────────────────────────

    fun trimVideo(input: String, output: String, start: Double, duration: Double) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.trimVideo(ctx, input, output, start, duration) { ok ->
            _state.value = if (ok) done(output) else failed("Trim failed")
        }
    }

    fun mergeClips(inputs: List<String>, output: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.mergeClips(ctx, inputs, output) { ok ->
            _state.value = if (ok) done(output) else failed("Merge failed")
        }
    }

    fun addAudio(video: String, audio: String, output: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.addAudio(video, audio, output) { ok ->
            _state.value = if (ok) done(output) else failed("Audio mix failed")
        }
    }

    fun resizeToReels(input: String, output: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.resizeToReels(ctx, input, output) { ok ->
            _state.value = if (ok) done(output) else failed("Resize failed")
        }
    }

    fun addCaption(input: String, output: String, text: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.addTextOverlay(ctx, input, output, text) { ok ->
            _state.value = if (ok) done(output) else failed("Caption failed")
        }
    }

    fun textToVideo(lines: List<String>, output: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.textToVideo(ctx, lines, output, onProgress = ::updateProgress) { ok ->
            _state.value = if (ok) done(output) else failed("Text-to-video failed")
        }
    }

    fun addTxtOverlay(video: String, txtPath: String, output: String, durationSec: Double) {
        if (guardBusy()) return
        _state.value = processing()
        viewModelScope.launch {
            val lines = withContext(Dispatchers.IO) {
                runCatching { java.io.File(txtPath).readLines().filter { it.isNotBlank() } }
                    .getOrElse { emptyList() }
            }
            if (lines.isEmpty()) {
                _state.value = failed("TXT file empty or unreadable")
                return@launch
            }
            FFmpegHelper.addTxtOverlay(ctx, video, output, lines, durationSec) { ok ->
                _state.value = if (ok) done(output) else failed("TXT overlay failed")
            }
        }
    }

    fun applyDramaticEffect(input: String, output: String, style: TemplateRenderer.DramaticStyle) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.applyDramaticEffect(ctx, input, output, style) { ok ->
            _state.value = if (ok) done(output) else failed("Effect failed")
        }
    }

    fun addBreakingNewsOverlay(input: String, output: String, headline: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.addBreakingNewsOverlay(ctx, input, output, headline) { ok ->
            _state.value = if (ok) done(output) else failed("Breaking news overlay failed")
        }
    }

    fun dramaticNewsReel(headlines: List<String>, output: String) {
        if (guardBusy()) return
        _state.value = processing()
        FFmpegHelper.dramaticNewsReel(ctx, headlines, output, onProgress = ::updateProgress) { ok ->
            _state.value = if (ok) done(output) else failed("News reel generation failed")
        }
    }
}
