package com.kairos.app.engine.critters

import kotlin.random.Random

/**
 * Puddlings: tiny watercolor creatures that hatch from the dew you collect by doing kind,
 * curious things. Every creature is grown from a seed, so the pond holds thousands of
 * variations without shipping a single sprite.
 */
enum class Body { Blob, Drop, Mochi, Bean, Star, Bell }
enum class Topper { None, CatEars, BunnyEars, Sprout, Antenna, Horn, Crown, Petals }
enum class Pattern { Plain, Belly, Spots, Blush, Stripes }
enum class Eyes { Dots, Happy, Sparkle, Sleepy }
enum class Mouth { Smile, Cat, Oh, Grin }
enum class Rarity(val label: String, val weight: Int) {
    Common("Common", 70), Uncommon("Uncommon", 22), Rare("Rare", 7), Shiny("Shiny", 1),
}

data class Genome(
    val seed: Long,
    val body: Body,
    val topper: Topper,
    val pattern: Pattern,
    val eyes: Eyes,
    val mouth: Mouth,
    val hue: Int,
    val accentHue: Int,
    val rarity: Rarity,
    val trait: String,
    val habitat: String,
    val squish: Float,
)

object CritterGenetics {
    /** 12 pigment hues (index into the palette in the renderer). */
    const val HUES = 12

    private val traits = listOf(
        "hums when it's happy", "collects shiny pebbles", "naps in teacups", "loves rainy mornings",
        "is afraid of nothing except loud spoons", "remembers every word you learn", "waves at strangers",
        "glows a little when you write", "keeps secrets very well", "follows butterflies", "giggles in its sleep",
        "smells faintly of oranges", "likes to be read to", "counts stars before bed", "tries very hard at everything",
        "is learning to whistle", "hides behind lily pads", "gets the hiccups when excited",
    )
    private val habitats = listOf(
        "Lily shallows", "Mossy stones", "Rain-barrel", "Moonlit reeds", "Pebble cove", "Teacup springs",
        "Willow roots", "Morning mist", "Sunlit ripple", "Cloud puddle",
    )
    private val syllA = listOf("Mo", "Pi", "Lu", "Bo", "Ki", "Nu", "Fi", "Ru", "Po", "Mi", "Ta", "So", "Wi", "Ze", "Ba", "Ya")
    private val syllB = listOf("chi", "pip", "lo", "mi", "ri", "ppa", "bun", "zu", "ko", "lin", "mo", "tsu", "nee", "fu", "bi", "do")

    fun genome(seed: Long): Genome {
        val r = Random(seed)
        val rarity = pickRarity(r)
        val toppers = Topper.entries.filter { it != Topper.Crown || rarity >= Rarity.Rare }
        return Genome(
            seed = seed,
            body = Body.entries[r.nextInt(Body.entries.size)],
            topper = toppers[r.nextInt(toppers.size)],
            pattern = Pattern.entries[r.nextInt(Pattern.entries.size)],
            eyes = Eyes.entries[r.nextInt(Eyes.entries.size)],
            mouth = Mouth.entries[r.nextInt(Mouth.entries.size)],
            hue = r.nextInt(HUES),
            accentHue = r.nextInt(HUES),
            rarity = rarity,
            trait = traits[r.nextInt(traits.size)],
            habitat = habitats[r.nextInt(habitats.size)],
            squish = 0.9f + r.nextFloat() * 0.2f,
        )
    }

    fun name(seed: Long): String {
        val r = Random(seed xor 0x5EED)
        val first = syllA[r.nextInt(syllA.size)] + syllB[r.nextInt(syllB.size)]
        return if (r.nextInt(4) == 0) first + syllB[r.nextInt(syllB.size)] else first
    }

    private fun pickRarity(r: Random): Rarity {
        val total = Rarity.entries.sumOf { it.weight }
        var roll = r.nextInt(total)
        for (rarity in Rarity.entries) {
            roll -= rarity.weight
            if (roll < 0) return rarity
        }
        return Rarity.Common
    }
}

/** Dew: earned by showing up for yourself. Daily caps keep it about care, not grinding. */
enum class DewReason(val key: String, val amount: Int, val dailyCap: Int, val label: String) {
    Checkin("checkin", 5, 5, "Checked in with yourself"),
    WordMet("word", 3, 3, "Met today's word"),
    Review("review", 2, 24, "Practiced a word"),
    Session("session", 8, 16, "Finished a practice session"),
    Capsule("capsule", 12, 24, "Sealed a time capsule"),
    CapsuleOpened("opened", 15, 45, "Opened a letter from the past"),
    Kindness("kindness", 6, 18, "Did a kind little thing"),
    Breathe("breathe", 4, 8, "Took a slow breath"),
    Game("game", 1, 20, "Played in the arcade"),
    Saved("saved", 1, 5, "Kept something worth keeping"),
}

object Hatchery {
    const val EGG_COST = 60

    /** Eggs ready = lifetime dew converted to eggs, minus everyone already hatched (the first is free). */
    fun eggsReady(totalDew: Int, hatched: Int): Int = (totalDew / EGG_COST - (hatched - 1).coerceAtLeast(0)).coerceAtLeast(0)

    fun progressToNext(totalDew: Int): Float = (totalDew % EGG_COST) / EGG_COST.toFloat()
}
