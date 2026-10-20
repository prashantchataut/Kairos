package com.kairos.app.domain.play

/** Original Kairos game: grow a flower by recalling a word. No timers or lost lives. */
data class GardenWord(val word: String, val meaning: String, val choices: List<String>)

object WordGarden {
    val words = listOf(
        GardenWord("Serendipity", "A happy discovery made by chance", listOf("A happy discovery made by chance", "A carefully planned journey", "A feeling of deep tiredness")),
        GardenWord("Resilient", "Able to recover after difficulty", listOf("Unable to change direction", "Able to recover after difficulty", "Moving with great speed")),
        GardenWord("Savor", "Enjoy something slowly and fully", listOf("Put something away for later", "Enjoy something slowly and fully", "Explain something briefly")),
        GardenWord("Luminous", "Giving off or reflecting light", listOf("Giving off or reflecting light", "Hidden from view", "Soft to the touch")),
        GardenWord("Kinship", "A feeling of connection and belonging", listOf("A long distance to travel", "A feeling of connection and belonging", "A sudden change in weather")),
        GardenWord("Meander", "Follow a winding course", listOf("Follow a winding course", "Stand completely still", "Speak very loudly")),
        GardenWord("Solace", "Comfort in a time of sadness", listOf("A reason to hurry", "An unexpected question", "Comfort in a time of sadness")),
        GardenWord("Flourish", "Grow and develop well", listOf("Grow and develop well", "Disappear gradually", "Wait without moving")),
        GardenWord("Ephemeral", "Lasting for a short time", listOf("Lasting for a short time", "Repeated forever", "Difficult to understand"))
    )
    fun question(index: Int): GardenWord = words[Math.floorMod(index, words.size)]
    fun isCorrect(index: Int, answer: String): Boolean = question(index).meaning == answer
}
