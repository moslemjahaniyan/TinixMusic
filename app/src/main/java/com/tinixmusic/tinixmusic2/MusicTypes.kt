package com.tinixmusic.tinixmusic2

object MusicTypes {
    val TYPES = listOf(
        "Original" to "اصلی",
        "Slowed" to "آهسته",
        "Super Slowed" to "خیلی آهسته",
        "Ultra Slowed" to "فوق‌العاده آهسته",
        "Sped Up" to "سریع",
        "Super Sped Up" to "خیلی سریع",
        "Remix" to "ریمیکس",
        "Instrumental" to "بی‌کلام",
        "Nightcore" to "نایت‌کور",
        "Extreme Slowed" to "بسیار آهسته",
        "TikTok Version" to "ورژن تیک‌تاک",
        "Slowed Reverb" to "آهسته ریورب",
        "Super Slowed Reverb" to "خیلی آهسته ریورب",
        "Ultra Slowed Reverb" to "فوق‌العاده آهسته ریورب"
    )
    fun getPersianLabel(englishLabel: String): String {
        return TYPES.find { it.first == englishLabel }?.second ?: englishLabel
    }
}