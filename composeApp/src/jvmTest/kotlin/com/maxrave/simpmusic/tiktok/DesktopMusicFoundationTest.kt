package com.maxrave.simpmusic.tiktok

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopMusicFoundationTest {
    @Test fun strongMusicVideoIsIncluded() {
        val result = TikTokMusicClassifier.classify(
            TikTokVideoMetadata("Official MV | New release", soundTitle = "Song")
        )
        assertEquals(MusicIntent.MUSIC, result.intent)
    }

    @Test fun comedyWithMusicIsExcluded() {
        val result = TikTokMusicClassifier.classify(
            TikTokVideoMetadata("Funny comedy skit", soundTitle = "Popular song")
        )
        assertEquals(MusicIntent.NON_MUSIC, result.intent)
    }

    @Test fun soundAloneDoesNotImplyMusic() {
        val result = TikTokMusicClassifier.classify(
            TikTokVideoMetadata("A random clip", soundTitle = "Popular song")
        )
        assertEquals(MusicIntent.UNCERTAIN, result.intent)
    }

    @Test fun danceChallengeIsNotMusicPerformance() {
        val result = TikTokMusicClassifier.classify(
            TikTokVideoMetadata("Dance challenge #trending", soundTitle = "Song")
        )
        assertEquals(MusicIntent.NON_MUSIC, result.intent)
    }

    @Test fun sourceIdsCannotCollide() {
        val a = DesktopMusicItem(DesktopMusicSource.TIKTOK, "123", "Song", originalUrl = "https://www.tiktok.com/video/123")
        val b = DesktopMusicItem(DesktopMusicSource.YOUTUBE_MUSIC, "123", "Song", originalUrl = "https://music.youtube.com/watch?v=123")
        assertTrue(a.qualifiedId != b.qualifiedId)
    }

    @Test fun unsupportedNativeAudioIsRejected() {
        val provider = object : DesktopMusicProvider {
            override val source = DesktopMusicSource.TIKTOK
            override val capabilities = setOf(DesktopPlaybackCapability.EMBEDDED_VIDEO)
            override suspend fun search(query: String, limit: Int) = emptyList<DesktopMusicItem>()
            override suspend fun resolvePlayback(item: DesktopMusicItem): DesktopPlayback =
                DesktopPlayback.Unavailable("Not implemented")
        }
        val item = DesktopMusicItem(
            DesktopMusicSource.TIKTOK, "123", "Video", originalUrl = "https://www.tiktok.com/video/123",
            capabilities = setOf(DesktopPlaybackCapability.EMBEDDED_VIDEO),
        )
        assertIs<DesktopPlayback.Unavailable>(
            validatePlayback(provider, item, DesktopPlayback.NativeAudio("https://example.org/audio"))
        )
    }
}
