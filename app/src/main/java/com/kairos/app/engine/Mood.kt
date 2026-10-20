package com.kairos.app.engine

import com.kairos.app.ui.theme.Wash

/** Five honest moods. Each one tints the mascot and nudges which quote themes surface. */
enum class Mood(
    val label: String,
    val wash: Wash,
    val reply: String,
    val favouredThemes: Set<String>,
) {
    Radiant("Radiant", Wash.Butter, "Look at you glow. Let's keep this going.", setOf("joy", "wonder", "growth")),
    Good("Good", Wash.Mint, "A good day to learn something lovely.", setOf("joy", "growth", "courage")),
    Okay("Okay", Wash.Cerulean, "Okay is a perfectly fine place to be.", setOf("calm", "hope", "wonder")),
    Low("Low", Wash.Lilac, "Thanks for telling me. Let's go gently today.", setOf("hope", "kindness", "rest")),
    Rough("Rough", Wash.Rose, "That sounds heavy. One small kind thing, that's all.", setOf("kindness", "rest", "hope"));

    companion object {
        fun of(name: String?) = entries.firstOrNull { it.name == name }
    }
}
