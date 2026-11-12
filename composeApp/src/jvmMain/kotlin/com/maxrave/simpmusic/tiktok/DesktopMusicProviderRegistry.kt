package com.maxrave.simpmusic.tiktok

/**
 * Explicit desktop provider registry. Duplicate source registration is an error.
 * This does not automatically register TikTok or change the existing YouTube stack.
 */
class DesktopMusicProviderRegistry(providers: Iterable<DesktopMusicProvider>) {
    private val providersBySource: Map<DesktopMusicSource, DesktopMusicProvider>

    init {
        val entries = providers.toList()
        require(entries.map { it.source }.distinct().size == entries.size) {
            "Duplicate desktop music provider source"
        }
        providersBySource = entries.associateBy { it.source }
    }

    fun find(source: DesktopMusicSource): DesktopMusicProvider? = providersBySource[source]

    fun availableSources(): Set<DesktopMusicSource> = providersBySource.keys

    suspend fun search(
        source: DesktopMusicSource,
        query: String,
        limit: Int = 20,
    ): List<DesktopMusicItem> {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        if (query.isBlank()) return emptyList()
        val provider = find(source) ?: return emptyList()
        return provider.search(query.trim(), limit).take(limit).filter { it.source == source }
    }

    suspend fun resolvePlayback(item: DesktopMusicItem): DesktopPlayback {
        val provider = find(item.source)
            ?: return DesktopPlayback.Unavailable("Source not registered")
        return validatePlayback(provider, item, provider.resolvePlayback(item))
    }
}
