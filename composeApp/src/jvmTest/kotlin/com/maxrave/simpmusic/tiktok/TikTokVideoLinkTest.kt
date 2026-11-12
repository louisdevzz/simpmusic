package com.maxrave.simpmusic.tiktok

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TikTokVideoLinkTest {
    @Test fun acceptsCanonicalVideoUrlAndDropsTrackingQuery() {
        val link = TikTokVideoLinks.parse("https://www.tiktok.com/@music.artist/video/123456789?is_from_webapp=1")
        assertIs<TikTokVideoLink.Video>(link)
        assertEquals("123456789", link.videoId)
        assertEquals("https://www.tiktok.com/@music.artist/video/123456789", link.url)
    }

    @Test fun shortLinkRemainsExternalOnly() {
        val link = TikTokVideoLinks.parse("https://vm.tiktok.com/ZMabcdef/")
        assertIs<TikTokVideoLink.ShortLink>(link)
    }

    @Test fun rejectsImpersonationAndUnsafeSchemes() {
        assertNull(TikTokVideoLinks.parse("https://www.tiktok.com.evil.example/@a/video/123"))
        assertNull(TikTokVideoLinks.parse("http://www.tiktok.com/@a/video/123"))
        assertNull(TikTokVideoLinks.parse("https://user@www.tiktok.com/@a/video/123"))
        assertNull(TikTokVideoLinks.parse("https://www.tiktok.com:8443/@a/video/123"))
        assertNull(TikTokVideoLinks.parse("https://www.tiktok.com/@a/video/not-an-id"))
    }

    @Test fun rejectsUnrelatedTikTokPages() {
        assertNull(TikTokVideoLinks.parse("https://www.tiktok.com/foryou"))
        assertNull(TikTokVideoLinks.parse("https://www.tiktok.com/@creator"))
    }
}
