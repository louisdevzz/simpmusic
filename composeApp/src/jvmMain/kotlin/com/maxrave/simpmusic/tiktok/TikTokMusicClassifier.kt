package com.maxrave.simpmusic.tiktok

/**
 * Conservative, explainable metadata-only music intent classifier.
 *
 * This is a deterministic baseline, not a trained model and not a guarantee
 * that a TikTok clip is music-focused. It must not be used to claim global
 * TikTok search or permission to download/stream media.
 */
enum class MusicIntent { MUSIC, NON_MUSIC, UNCERTAIN }

data class TikTokVideoMetadata(
    val caption: String,
    val creatorName: String? = null,
    val hashtags: List<String> = emptyList(),
    val soundTitle: String? = null,
)

data class MusicClassification(
    val intent: MusicIntent,
    val score: Int,
    val reasons: List<String>,
)

object TikTokMusicClassifier {
    private val strongMusic = listOf(
        "official music video", "official mv", "live performance", "live session",
        "acoustic cover", "cover song", "music video", "lyric video", "lyrics video",
        "piano cover", "guitar cover", "dj set", "bản cover", "trình diễn",
        "biểu diễn âm nhạc", "nhạc acoustic", "mv chính thức",
    )
    private val music = listOf(
        "cover", "remix", "acoustic", "karaoke", "singing", "vocal",
        "live music", "instrumental", "performance", "hát live",
        "ca khúc", "bài hát", "âm nhạc", "nhạc sống",
    )
    private val negatives = listOf(
        "comedy", "funny", "prank", "meme", "skit", "reaction",
        "vlog", "dance challenge", "storytime", "hài", "tấu hài",
        "thử thách nhảy", "trò đùa", "review đồ ăn",
    )

    fun classify(metadata: TikTokVideoMetadata): MusicClassification {
        val text = (listOf(metadata.caption, metadata.creatorName.orEmpty()) +
            metadata.hashtags.map { it.removePrefix("#") }).joinToString(" ").lowercase()
        val reasons = mutableListOf<String>()
        var score = 40

        if (strongMusic.any { it in text }) {
            score += 45
            reasons += "strong music-performance metadata"
        } else if (music.any { it in text }) {
            score += 30
            reasons += "music-related metadata"
        }

        if (negatives.any { it in text }) {
            score -= 55
            reasons += "non-music content indicator"
        }

        // Most TikTok videos contain a sound: sound metadata alone is weak
        // evidence and must never make a video MUSIC by itself.
        if (!metadata.soundTitle.isNullOrBlank()) {
            score += 5
            reasons += "sound metadata present (weak signal)"
        }
        score = score.coerceIn(0, 100)
        val intent = when {
            score >= 70 -> MusicIntent.MUSIC
            score <= 25 -> MusicIntent.NON_MUSIC
            else -> MusicIntent.UNCERTAIN
        }
        return MusicClassification(intent, score, reasons)
    }
}
