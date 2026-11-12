package com.maxrave.simpmusic.tiktok

import java.net.URI

/**
 * Validates user-supplied TikTok links without fetching, resolving short URLs,
 * extracting streams, or reading cookies.
 *
 * A short link is kept as an external link until an approved service resolves it.
 */
sealed interface TikTokVideoLink {
    val url: String

    data class Video(override val url: String, val videoId: String) : TikTokVideoLink
    data class ShortLink(override val url: String) : TikTokVideoLink
}

object TikTokVideoLinks {
    private val videoPath = Regex("^/@[A-Za-z0-9._-]+/video/([0-9]+)/?$")
    private val allowedVideoHosts = setOf("www.tiktok.com", "tiktok.com", "m.tiktok.com")
    private val allowedShortHosts = setOf("vm.tiktok.com", "vt.tiktok.com")

    fun parse(raw: String): TikTokVideoLink? {
        val uri = try {
            URI(raw.trim())
        } catch (_: IllegalArgumentException) {
            return null
        }
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.userInfo != null || uri.port != -1 || uri.fragment != null) return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in allowedVideoHosts && host !in allowedShortHosts) return null
        val path = uri.path ?: return null
        val cleanUrl = "https://$host$path"
        if (host in allowedShortHosts) {
            if (!Regex("^/[A-Za-z0-9]+/?$").matches(path)) return null
            return TikTokVideoLink.ShortLink(cleanUrl)
        }
        val match = videoPath.matchEntire(path) ?: return null
        return TikTokVideoLink.Video(cleanUrl, match.groupValues[1])
    }
}
