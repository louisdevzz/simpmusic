package com.maxrave.simpmusic.tiktok

/**
 * Desktop-only music catalog contracts. No network transport or platform-specific
 * media extraction is assumed; provider capabilities must be explicitly declared.
 */
enum class DesktopMusicSource { YOUTUBE_MUSIC, TIKTOK }

enum class DesktopPlaybackCapability { NATIVE_AUDIO, EMBEDDED_VIDEO, BACKGROUND_AUDIO, OFFLINE }

data class DesktopMusicItem(
    val source: DesktopMusicSource,
    val sourceId: String,
    val title: String,
    val artist: String? = null,
    val originalUrl: String,
    val capabilities: Set<DesktopPlaybackCapability> = emptySet(),
) {
    init {
        require(sourceId.isNotBlank()) { "sourceId cannot be blank" }
        require(originalUrl.startsWith("https://")) { "originalUrl must be HTTPS" }
    }

    val qualifiedId: String get() = "${source.name.lowercase()}:$sourceId"
}

sealed interface DesktopPlayback {
    data class NativeAudio(val url: String) : DesktopPlayback
    data class EmbeddedVideo(val url: String) : DesktopPlayback
    data class Unavailable(val reason: String) : DesktopPlayback
}

interface DesktopMusicProvider {
    val source: DesktopMusicSource
    val capabilities: Set<DesktopPlaybackCapability>

    suspend fun search(query: String, limit: Int = 20): List<DesktopMusicItem>
    suspend fun resolvePlayback(item: DesktopMusicItem): DesktopPlayback
}

/**
 * Prevents a provider from advertising playback it does not support.
 * TikTok URLs must never be assumed to be native media stream URLs.
 */
fun validatePlayback(
    provider: DesktopMusicProvider,
    item: DesktopMusicItem,
    playback: DesktopPlayback,
): DesktopPlayback {
    if (provider.source != item.source) return DesktopPlayback.Unavailable("Source mismatch")
    val needed = when (playback) {
        is DesktopPlayback.NativeAudio -> DesktopPlaybackCapability.NATIVE_AUDIO
        is DesktopPlayback.EmbeddedVideo -> DesktopPlaybackCapability.EMBEDDED_VIDEO
        is DesktopPlayback.Unavailable -> return playback
    }
    if (needed !in provider.capabilities || needed !in item.capabilities) {
        return DesktopPlayback.Unavailable("Playback capability not supported")
    }
    val url = when (playback) {
        is DesktopPlayback.NativeAudio -> playback.url
        is DesktopPlayback.EmbeddedVideo -> playback.url
        is DesktopPlayback.Unavailable -> return playback
    }
    if (!url.startsWith("https://")) return DesktopPlayback.Unavailable("Non-HTTPS playback URL")
    return playback
}
