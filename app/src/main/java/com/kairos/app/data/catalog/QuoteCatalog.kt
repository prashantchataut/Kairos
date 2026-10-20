package com.kairos.app.data.catalog

/**
 * The reading room.
 *
 * Two kinds of text live here: lines from writers worth returning to, and short notes written
 * for Kairos itself. Notes are labelled `original = true` and are never attributed to anyone
 * else — a small app should not put words in a dead poet's mouth.
 */
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val theme: String,
    val original: Boolean = false,
)

object QuoteCatalog {

    const val THEME_STILLNESS = "stillness"
    const val THEME_COURAGE = "courage"
    const val THEME_WONDER = "wonder"
    const val THEME_CRAFT = "craft"
    const val THEME_KINDNESS = "kindness"
    const val THEME_TIME = "time"
    const val THEME_GROWTH = "growth"
    const val THEME_PLAY = "play"

    val themes = listOf(
        THEME_STILLNESS to "Stillness",
        THEME_COURAGE to "Courage",
        THEME_WONDER to "Wonder",
        THEME_CRAFT to "Craft",
        THEME_KINDNESS to "Kindness",
        THEME_TIME to "Time",
        THEME_GROWTH to "Growth",
        THEME_PLAY to "Play",
    )

    val all: List<Quote> = listOf(
        // ---------------------------------------------------------------- stillness
        Quote("st-01", "You have power over your mind — not outside events. Realize this, and you will find strength.", "Marcus Aurelius", THEME_STILLNESS),
        Quote("st-02", "We suffer more often in imagination than in reality.", "Seneca", THEME_STILLNESS),
        Quote("st-03", "Let everything happen to you: beauty and terror. Just keep going. No feeling is final.", "Rainer Maria Rilke", THEME_STILLNESS),
        Quote("st-04", "The quieter you become, the more you can hear.", "Ram Dass", THEME_STILLNESS),
        Quote("st-05", "Nothing is so much to be feared as fear.", "Henry David Thoreau", THEME_STILLNESS),
        Quote("st-06", "Within you there is a stillness and a sanctuary to which you can retreat at any time.", "Hermann Hesse", THEME_STILLNESS),
        Quote("st-07", "Do not let the behaviour of others destroy your inner peace.", "Dalai Lama", THEME_STILLNESS),
        Quote("st-08", "Rest is not idleness, and to lie sometimes on the grass under trees on a summer's day is by no means a waste of time.", "John Lubbock", THEME_STILLNESS),
        Quote("st-09", "Give your attention to the experience of seeing rather than to the object seen.", "Simone Weil", THEME_STILLNESS),
        Quote("st-10", "The present moment is filled with joy and happiness. If you are attentive, you will see it.", "Thich Nhat Hanh", THEME_STILLNESS),

        // ---------------------------------------------------------------- courage
        Quote("co-01", "Courage is not the absence of fear, but the triumph over it.", "Nelson Mandela", THEME_COURAGE),
        Quote("co-02", "It is not because things are difficult that we do not dare; it is because we do not dare that they are difficult.", "Seneca", THEME_COURAGE),
        Quote("co-03", "You gain strength, courage and confidence by every experience in which you really stop to look fear in the face.", "Eleanor Roosevelt", THEME_COURAGE),
        Quote("co-04", "The wound is the place where the light enters you.", "Rumi", THEME_COURAGE),
        Quote("co-05", "Life shrinks or expands in proportion to one's courage.", "Anaïs Nin", THEME_COURAGE),
        Quote("co-06", "Do the thing you fear to do and keep on doing it. That is the quickest and surest way ever yet discovered to conquer fear.", "Dale Carnegie", THEME_COURAGE),
        Quote("co-07", "Perhaps all the dragons in our lives are princesses who are only waiting to see us act, just once, with beauty and courage.", "Rainer Maria Rilke", THEME_COURAGE),
        Quote("co-08", "The cave you fear to enter holds the treasure you seek.", "Joseph Campbell", THEME_COURAGE),
        Quote("co-09", "Heroism is not only in the man, but in the occasion.", "Calvin Coolidge", THEME_COURAGE),
        Quote("co-10", "You are not required to win today. You are only required to begin.", "Kairos", THEME_COURAGE, original = true),

        // ---------------------------------------------------------------- wonder
        Quote("wo-01", "The world is full of magical things patiently waiting for our wits to grow sharper.", "Eden Phillpotts", THEME_WONDER),
        Quote("wo-02", "The most beautiful thing we can experience is the mysterious. It is the source of all true art and science.", "Albert Einstein", THEME_WONDER),
        Quote("wo-03", "There are two ways to live your life. One is as though nothing is a miracle. The other is as though everything is.", "Albert Einstein", THEME_WONDER),
        Quote("wo-04", "To see a world in a grain of sand and a heaven in a wild flower.", "William Blake", THEME_WONDER),
        Quote("wo-05", "The real voyage of discovery consists not in seeking new landscapes, but in having new eyes.", "Marcel Proust", THEME_WONDER),
        Quote("wo-06", "Wonder is the beginning of wisdom.", "Socrates", THEME_WONDER),
        Quote("wo-07", "In every walk with nature one receives far more than he seeks.", "John Muir", THEME_WONDER),
        Quote("wo-08", "Those who contemplate the beauty of the earth find reserves of strength that will endure as long as life lasts.", "Rachel Carson", THEME_WONDER),
        Quote("wo-09", "The world is a book, and those who do not travel read only one page.", "Augustine of Hippo", THEME_WONDER),
        Quote("wo-10", "Look closely. The ordinary is only ordinary because you have stopped looking.", "Kairos", THEME_WONDER, original = true),

        // ---------------------------------------------------------------- craft
        Quote("cr-01", "Amateurs sit and wait for inspiration. The rest of us just get up and go to work.", "Stephen King", THEME_CRAFT),
        Quote("cr-02", "Inspiration exists, but it has to find you working.", "Pablo Picasso", THEME_CRAFT),
        Quote("cr-03", "The master has failed more times than the beginner has even tried.", "Stephen McCranie", THEME_CRAFT),
        Quote("cr-04", "We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Will Durant", THEME_CRAFT),
        Quote("cr-05", "Have no fear of perfection — you'll never reach it.", "Salvador Dalí", THEME_CRAFT),
        Quote("cr-06", "Start where you are. Use what you have. Do what you can.", "Arthur Ashe", THEME_CRAFT),
        Quote("cr-07", "A year from now you may wish you had started today.", "Karen Lamb", THEME_CRAFT),
        Quote("cr-08", "The scariest moment is always just before you start.", "Stephen King", THEME_CRAFT),
        Quote("cr-09", "One day of doing is worth a week of planning it perfectly.", "Kairos", THEME_CRAFT, original = true),
        Quote("cr-10", "Make something small today. Small things are how large things learn to exist.", "Kairos", THEME_CRAFT, original = true),

        // ---------------------------------------------------------------- kindness
        Quote("ki-01", "Be kind, for everyone you meet is fighting a hard battle.", "Ian Maclaren", THEME_KINDNESS),
        Quote("ki-02", "If you want others to be happy, practice compassion. If you want to be happy, practice compassion.", "Dalai Lama", THEME_KINDNESS),
        Quote("ki-03", "No one has ever become poor by giving.", "Anne Frank", THEME_KINDNESS),
        Quote("ki-04", "The best way to find yourself is to lose yourself in the service of others.", "Mahatma Gandhi", THEME_KINDNESS),
        Quote("ki-05", "Kindness is a language which the deaf can hear and the blind can see.", "Mark Twain", THEME_KINDNESS),
        Quote("ki-06", "You cannot do a kindness too soon, for you never know how soon it will be too late.", "Ralph Waldo Emerson", THEME_KINDNESS),
        Quote("ki-07", "There is no exercise better for the heart than reaching down and lifting people up.", "John Holmes", THEME_KINDNESS),
        Quote("ki-08", "Be gentle with yourself. You are a child of the universe, no less than the trees and the stars.", "Max Ehrmann", THEME_KINDNESS),
        Quote("ki-09", "The people around you are carrying things you cannot see. Go a little softer.", "Kairos", THEME_KINDNESS, original = true),
        Quote("ki-10", "You do not have to fix someone to be kind to them. You only have to stay for a minute.", "Kairos", THEME_KINDNESS, original = true),

        // ---------------------------------------------------------------- time
        Quote("ti-01", "How we spend our days is, of course, how we spend our lives.", "Annie Dillard", THEME_TIME),
        Quote("ti-02", "It is not that we have a short time to live, but that we waste a lot of it.", "Seneca", THEME_TIME),
        Quote("ti-03", "The trouble is, you think you have time.", "Jack Kornfield", THEME_TIME),
        Quote("ti-04", "Yesterday is gone. Tomorrow has not yet come. We have only today.", "Mother Teresa", THEME_TIME),
        Quote("ti-05", "Time is the coin of your life. It is the only coin you have, and only you can determine how it will be spent.", "Carl Sandburg", THEME_TIME),
        Quote("ti-06", "For the great doesn't happen through impulse alone, and is a succession of little things that are brought together.", "Vincent van Gogh", THEME_TIME),
        Quote("ti-07", "The future is something which everyone reaches at the rate of sixty minutes an hour.", "C. S. Lewis", THEME_TIME),
        Quote("ti-08", "One day you will look back and see that this quiet, ordinary week was the good part.", "Kairos", THEME_TIME, original = true),

        // ---------------------------------------------------------------- growth
        Quote("gr-01", "The only way out is through.", "Robert Frost", THEME_GROWTH),
        Quote("gr-02", "What you seek is seeking you.", "Rumi", THEME_GROWTH),
        Quote("gr-03", "Growth is never by mere chance; it is the result of forces working together.", "James Cash Penney", THEME_GROWTH),
        Quote("gr-04", "We cannot solve our problems with the same thinking we used when we created them.", "Albert Einstein", THEME_GROWTH),
        Quote("gr-05", "The bamboo that bends is stronger than the oak that resists.", "Japanese proverb", THEME_GROWTH),
        Quote("gr-06", "Every flower must grow through dirt.", "Laurie Jean Sennott", THEME_GROWTH),
        Quote("gr-07", "You don't have to see the whole staircase. Just take the first step.", "Martin Luther King Jr.", THEME_GROWTH),
        Quote("gr-08", "Nothing that grows does so in a straight line. Neither do you.", "Kairos", THEME_GROWTH, original = true),
        Quote("gr-09", "The point is not to arrive. The point is to keep becoming.", "Kairos", THEME_GROWTH, original = true),

        // ---------------------------------------------------------------- play
        Quote("pl-01", "We don't stop playing because we grow old; we grow old because we stop playing.", "George Bernard Shaw", THEME_PLAY),
        Quote("pl-02", "Creativity is intelligence having fun.", "Albert Einstein", THEME_PLAY),
        Quote("pl-03", "The creation of something new is not accomplished by the intellect but by the play instinct.", "Carl Jung", THEME_PLAY),
        Quote("pl-04", "A little nonsense now and then is relished by the wisest men.", "Roald Dahl", THEME_PLAY),
        Quote("pl-05", "You are never too old to set another goal or to dream a new dream.", "C. S. Lewis", THEME_PLAY),
        Quote("pl-06", "Play is the highest form of research.", "Albert Einstein", THEME_PLAY),
        Quote("pl-07", "Make the thing badly. Then make it again. That is the whole secret.", "Kairos", THEME_PLAY, original = true),
        Quote("pl-08", "If it isn't a little bit fun, you are probably doing it for the wrong reason.", "Kairos", THEME_PLAY, original = true),
    )

    val byId: Map<String, Quote> = all.associateBy { it.id }

    fun ofTheme(theme: String): List<Quote> = all.filter { it.theme == theme }
}
