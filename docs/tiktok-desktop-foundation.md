# TikTok Music Desktop foundation (Windows/macOS)

This PR introduces **desktop JVM-only** source-aware music contracts, capability validation, and a deterministic metadata-only classifier. It does **not** connect to TikTok, request cookies, search TikTok, play TikTok audio, or change the current YouTube Music implementation.

## Source audit

- `composeApp/src/jvmMain/.../DesktopApp.kt` initializes the desktop media stack.
- `composeApp/src/jvmMain/.../expect/ui/Cookies.jvm.kt` uses a Java CookieHandler; the desktop PlatformWebView is a help placeholder, not an embedded browser. Resetting the global CookieHandler is not provider-isolated.
- `composeApp/src/jvmMain/.../expect/ui/MediaPlayerView.jvm.kt` delegates to media_jvm_ui; an embed page is not a native media URL.
- `composeApp/src/commonMain/.../viewModel/BrowserLoginViewModel.kt` already supports browser profile cookie import, initially targeting YouTube.

## TikTok feasibility gates

1. Official Login Kit may provide consented account authorization, subject to app review, scopes and redirect support.
2. Display API is not a general TikTok video search or consumer music streaming API.
3. Research API is restricted to approved research use, not a consumer app discovery backend.
4. Cookie-based private web endpoints are undocumented, unstable and may violate platform rules; do not ship access-control bypasses.
5. Native audio, background playback and offline download must remain disabled unless explicitly licensed and technically supported. Embedded video must use supported official methods.

## Next steps

- Complete #1 source audit and #2 capability matrix with dated documentation and actual Windows/macOS evidence.
- Design provider DI and a feature-flagged TikTok adapter in #3.
- Validate the classifier on a consented labeled dataset before showing music-only recommendations (#5).
- Integrate auth, discovery and player only after capability gates (#4, #6, #7).
- Complete playlist migration and cross-platform QA (#8).

## Validation

The JVM-only unit tests cover music, comedy with background music, ambiguous sound-only clips, dance challenges, source ID collisions and unsupported native audio. No production network calls or TikTok credentials are required.
