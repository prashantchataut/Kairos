package com.kairos.app.engine

/** Five honest moods. Each nudges which quote themes surface and what the curator writes next. */
enum class Mood(val label: String, val reply: String, val favouredThemes: Set<String>) {
    Radiant("Radiant", "Love that. Let's make something of it.", setOf("joy", "wonder", "growth")),
    Good("Good", "A good day to learn something lovely.", setOf("joy", "growth", "courage")),
    Okay("Okay", "Okay is a perfectly fine place to be.", setOf("calm", "hope", "wonder")),
    Low("Low", "Thanks for telling me. Gently, today.", setOf("hope", "kindness", "rest")),
    Rough("Rough", "That sounds heavy. One small kind thing is enough.", setOf("kindness", "rest", "hope"));

    companion object {
        fun of(name: String?) = entries.firstOrNull { it.name == name }
    }
}

/** The interest taxonomy shown in onboarding and settings, and given to the curator. */
object Topics {
    data class Topic(val key: String, val label: String)

    val words = listOf(
        Topic("feelings", "Feelings"), Topic("mind", "Mind"), Topic("nature", "Nature"),
        Topic("connection", "People"), Topic("craft", "Making"), Topic("wonder", "Wonder"),
        Topic("courage", "Growth"), Topic("science", "Science"), Topic("culture", "Culture"),
    )
    val themes = listOf(
        Topic("hope", "Hope"), Topic("courage", "Courage"), Topic("kindness", "Kindness"), Topic("growth", "Growth"),
        Topic("calm", "Calm"), Topic("joy", "Joy"), Topic("wonder", "Wonder"), Topic("rest", "Rest"),
    )
    val levels = listOf("Everyday", "Familiar", "Rich", "Rare", "Literary")

    fun wordLabel(key: String) = words.firstOrNull { it.key == key }?.label ?: key.replaceFirstChar { it.uppercase() }
    fun themeLabel(key: String) = themes.firstOrNull { it.key == key }?.label ?: key.replaceFirstChar { it.uppercase() }
}
