package com.maxrave.simpmusic.tiktok

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopMusicProviderRegistryTest {
    private fun fakeProvider(sourceValue: DesktopMusicSource) = object : DesktopMusicProvider {
        override val source = sourceValue
        override val capabilities = setOf(DesktopPlaybackCapability.EMBEDDED_VIDEO)
        override suspend fun search(query: String, limit: Int) = listOf(
            DesktopMusicItem(source, "1", query, originalUrl = "https://www.tiktok.com/@music/video/1")
        )
        override suspend fun resolvePlayback(item: DesktopMusicItem): DesktopPlayback =
            DesktopPlayback.NativeAudio("https://example.com/audio")
    }

    @Test fun duplicateProvidersRejected() {
        assertFailsWith<IllegalArgumentException> {
            DesktopMusicProviderRegistry(listOf(fakeProvider(DesktopMusicSource.TIKTOK), fakeProvider(DesktopMusicSource.TIKTOK)))
        }
    }

    @Test fun unknownSourceIsUnavailable() = runBlocking {
        val registry = DesktopMusicProviderRegistry(emptyList())
        val item = DesktopMusicItem(DesktopMusicSource.TIKTOK, "1", "Video", originalUrl = "https://www.tiktok.com/@a/video/1")
        assertIs<DesktopPlayback.Unavailable>(registry.resolvePlayback(item))
        assertTrue(registry.search(DesktopMusicSource.TIKTOK, "song").isEmpty())
    }

    @Test fun unsupportedPlaybackIsBlocked() = runBlocking {
        val registry = DesktopMusicProviderRegistry(listOf(fakeProvider(DesktopMusicSource.TIKTOK)))
        val item = DesktopMusicItem(DesktopMusicSource.TIKTOK, "1", "Video", originalUrl = "https://www.tiktok.com/@a/video/1")
        assertIs<DesktopPlayback.Unavailable>(registry.resolvePlayback(item))
    }

    @Test fun searchIsTrimmedAndValidated() = runBlocking {
        val registry = DesktopMusicProviderRegistry(listOf(fakeProvider(DesktopMusicSource.TIKTOK)))
        assertEquals("music", registry.search(DesktopMusicSource.TIKTOK, " music ").single().title)
        assertFailsWith<IllegalArgumentException> {
            registry.search(DesktopMusicSource.TIKTOK, "music", 0)
        }
    }
}
