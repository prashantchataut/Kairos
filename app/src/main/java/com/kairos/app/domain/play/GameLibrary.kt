package com.kairos.app.domain.play

/** Explicit catalog: creator releases only; no scraped mirrors or inferred Android support. */
data class GameRelease(val title: String, val description: String, val creatorUrl: String, val downloadUrl: String?, val filename: String?)

object GameLibrary {
    val releases = listOf(
        GameRelease("Shattered Pixel Dungeon", "An open-source pixel adventure for Android. GPL-3.0; created by Evan Debenham and contributors.", "https://shatteredpixel.com/shatteredpd/",
            "https://github.com/00-Evan/shattered-pixel-dungeon/releases/download/v4.0.2/ShatteredPD-v4.0.2-Android.apk", "ShatteredPD-v4.0.2-Android.apk"),
        GameRelease("Pokémon fan adventures", "Browse creator-published releases. Compatibility varies; these are not bundled Android games.", "https://www.pokecommunity.com/forums/released-games.184/", null, null)
    )
}
