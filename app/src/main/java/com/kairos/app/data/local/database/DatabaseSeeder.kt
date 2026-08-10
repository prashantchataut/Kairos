package com.kairos.app.data.local.database

import android.content.Context
import android.util.Log
import com.kairos.app.data.content.ExpandedQuoteContent
import com.kairos.app.data.content.ExpandedVocabularyContent
import com.kairos.app.data.local.entity.AchievementEntity
import com.kairos.app.data.local.entity.IdiomEntity

import com.kairos.app.data.local.entity.PhraseEntity
import com.kairos.app.data.local.entity.ProverbEntity
import com.kairos.app.data.local.entity.QuoteEntity
import com.kairos.app.data.local.entity.UserProfileEntity
import com.kairos.app.data.local.entity.UserStatsEntity
import com.kairos.app.data.local.entity.VocabularyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Database Seeder for Kairos App
 *
 * Seeds initial wisdom content into the database on first app launch.
 * This ensures users have immediate access to quotes, proverbs, idioms, and phrases
 * without requiring network connectivity.
 *
 * Content is carefully curated for:
 * - Personal growth and development
 * - Mindfulness and self-awareness
 * - Motivation and resilience
 * - Vocabulary enrichment
 */
object DatabaseSeeder {

    private const val TAG = "DatabaseSeeder"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Seeds the database with initial content.
     * Called from database callback on first creation.
     */
    fun seedDatabase(database: KairosDatabase) {
        scope.launch {
            try {

                // Seed all content types in parallel for faster initialization
                launch { seedQuotes(database) }
                launch { seedProverbs(database) }
                launch { seedIdioms(database) }
                launch { seedPhrases(database) }
                launch { seedVocabulary(database) }
                
                launch { seedUserProfile(database) }
                launch { seedAchievements(database) }

            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
            }
        }
    }

    private suspend fun seedQuotes(database: KairosDatabase) {
        try {
            val quoteDao = database.quoteDao()
            quoteDao.insertQuotes(getInitialQuotes())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding quotes", e)
        }
    }

    private suspend fun seedProverbs(database: KairosDatabase) {
        try {
            val proverbDao = database.proverbDao()
            proverbDao.insertProverbs(getInitialProverbs())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding proverbs", e)
        }
    }

    private suspend fun seedIdioms(database: KairosDatabase) {
        try {
            val idiomDao = database.idiomDao()
            idiomDao.insertIdioms(getInitialIdioms())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding idioms", e)
        }
    }

    private suspend fun seedPhrases(database: KairosDatabase) {
        try {
            val phraseDao = database.phraseDao()
            phraseDao.insertPhrases(getInitialPhrases())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding phrases", e)
        }
    }

    // =========================================================================
    // QUOTES - Carefully curated wisdom from various sources
    // =========================================================================

    private fun getInitialQuotes(): List<QuoteEntity> = listOf(
        // Growth & Progress
        QuoteEntity(
            content = "Small steps still move you forward.",
            author = "Kairos Wisdom",
            category = "growth",
            tags = "progress,motivation,daily",
            reflectionPrompt = "What small step can you take today toward your biggest goal?"
        ),
        QuoteEntity(
            content = "You don't have to be great to start, but you have to start to be great.",
            author = "Zig Ziglar",
            category = "growth",
            tags = "beginning,motivation,action",
            reflectionPrompt = "What have you been putting off that you could start today?"
        ),
        QuoteEntity(
            content = "The only person you should try to be better than is who you were yesterday.",
            author = "Unknown",
            category = "growth",
            tags = "self-improvement,comparison,daily",
            reflectionPrompt = "In what way are you better today than you were yesterday?"
        ),
        QuoteEntity(
            content = "Every expert was once a beginner.",
            author = "Helen Hayes",
            category = "growth",
            tags = "learning,patience,progress",
            reflectionPrompt = "What skill are you currently a beginner at that you want to master?"
        ),
        QuoteEntity(
            content = "The journey of a thousand miles begins with a single step.",
            author = "Lao Tzu",
            category = "growth",
            tags = "beginning,journey,wisdom",
            reflectionPrompt = "What journey are you currently on, and what's your next step?"
        ),
        QuoteEntity(
            content = "Growth and comfort do not coexist.",
            author = "Ginni Rometty",
            category = "growth",
            tags = "discomfort,progress,challenge",
            reflectionPrompt = "Where in your life do you need to step outside your comfort zone?"
        ),
        QuoteEntity(
            content = "What got you here won't get you there.",
            author = "Marshall Goldsmith",
            category = "growth",
            tags = "change,evolution,success",
            reflectionPrompt = "What old habits do you need to let go of to reach your next level?"
        ),
        QuoteEntity(
            content = "A year from now you'll wish you had started today.",
            author = "Karen Lamb",
            category = "growth",
            tags = "action,regret,future",
            reflectionPrompt = "What will you be grateful you started today, one year from now?"
        ),

        // Resilience
        QuoteEntity(
            content = "Tough times don't last. Tough people do.",
            author = "Robert Schuller",
            category = "resilience",
            tags = "strength,perseverance,hardship",
            reflectionPrompt = "What challenge are you currently facing that you know you'll overcome?"
        ),
        QuoteEntity(
            content = "The obstacle is the way.",
            author = "Marcus Aurelius",
            category = "stoic",
            tags = "obstacles,stoicism,wisdom",
            reflectionPrompt = "How can your current obstacle become your greatest teacher?"
        ),
        QuoteEntity(
            content = "Fall seven times, stand up eight.",
            author = "Japanese Proverb",
            category = "resilience",
            tags = "perseverance,failure,strength",
            reflectionPrompt = "What setback have you overcome that made you stronger?"
        ),
        QuoteEntity(
            content = "Rock bottom became the solid foundation on which I rebuilt my life.",
            author = "J.K. Rowling",
            category = "resilience",
            tags = "comeback,strength,transformation",
            reflectionPrompt = "How have your lowest moments shaped who you are today?"
        ),
        QuoteEntity(
            content = "The bamboo that bends is stronger than the oak that resists.",
            author = "Japanese Proverb",
            category = "resilience",
            tags = "flexibility,strength,adaptation",
            reflectionPrompt = "Where in your life do you need to be more flexible?"
        ),
        QuoteEntity(
            content = "In the middle of difficulty lies opportunity.",
            author = "Albert Einstein",
            category = "resilience",
            tags = "opportunity,challenge,wisdom",
            reflectionPrompt = "What opportunity might be hidden in your current difficulty?"
        ),
        QuoteEntity(
            content = "A smooth sea never made a skilled sailor.",
            author = "Franklin D. Roosevelt",
            category = "resilience",
            tags = "challenges,growth,strength",
            reflectionPrompt = "What storm in your life made you a better person?"
        ),

        // Mindfulness & Presence
        QuoteEntity(
            content = "Be where you are, not where you think you should be.",
            author = "Unknown",
            category = "mindfulness",
            tags = "presence,acceptance,now",
            reflectionPrompt = "How can you be more fully present in this moment?"
        ),
        QuoteEntity(
            content = "The present moment is the only moment available to us, and it is the door to all moments.",
            author = "Thich Nhat Hanh",
            category = "mindfulness",
            tags = "presence,meditation,awareness",
            reflectionPrompt = "What is happening right now that you're grateful for?"
        ),
        QuoteEntity(
            content = "Wherever you are, be all there.",
            author = "Jim Elliot",
            category = "mindfulness",
            tags = "presence,focus,awareness",
            reflectionPrompt = "When was the last time you were fully present in a conversation?"
        ),
        QuoteEntity(
            content = "The mind is everything. What you think you become.",
            author = "Buddha",
            category = "mindfulness",
            tags = "thoughts,mind,transformation",
            reflectionPrompt = "What thoughts have been occupying your mind lately?"
        ),
        QuoteEntity(
            content = "Realize deeply that the present moment is all you have.",
            author = "Eckhart Tolle",
            category = "mindfulness",
            tags = "presence,now,awareness",
            reflectionPrompt = "What are you missing by focusing on the past or future?"
        ),
        QuoteEntity(
            content = "Almost everything will work again if you unplug it for a few minutes, including you.",
            author = "Anne Lamott",
            category = "mindfulness",
            tags = "rest,reset,self-care",
            reflectionPrompt = "When was the last time you truly unplugged and rested?"
        ),
        QuoteEntity(
            content = "Your calm mind is the ultimate weapon against your challenges.",
            author = "Bryant McGill",
            category = "mindfulness",
            tags = "calm,peace,strength",
            reflectionPrompt = "How do you cultivate calmness in stressful situations?"
        ),

        // Gratitude
        QuoteEntity(
            content = "Gratitude turns what we have into enough.",
            author = "Melody Beattie",
            category = "gratitude",
            tags = "thankfulness,abundance,contentment",
            reflectionPrompt = "What do you have right now that is more than enough?"
        ),
        QuoteEntity(
            content = "When you arise in the morning, think of what a precious privilege it is to be alive.",
            author = "Marcus Aurelius",
            category = "gratitude",
            tags = "morning,life,stoicism",
            reflectionPrompt = "What made you feel alive today?"
        ),
        QuoteEntity(
            content = "Enjoy the little things, for one day you may look back and realize they were the big things.",
            author = "Robert Brault",
            category = "gratitude",
            tags = "appreciation,moments,life",
            reflectionPrompt = "What little thing brought you joy today?"
        ),
        QuoteEntity(
            content = "It is not happy people who are thankful. It is thankful people who are happy.",
            author = "Unknown",
            category = "gratitude",
            tags = "happiness,thankfulness,wisdom",
            reflectionPrompt = "What are three things you're grateful for right now?"
        ),
        QuoteEntity(
            content = "The secret to having it all is knowing you already do.",
            author = "Unknown",
            category = "gratitude",
            tags = "abundance,contentment,wisdom",
            reflectionPrompt = "In what ways do you already have everything you need?"
        ),

        // Action & Discipline
        QuoteEntity(
            content = "Discipline is choosing between what you want now and what you want most.",
            author = "Abraham Lincoln",
            category = "discipline",
            tags = "choice,willpower,goals",
            reflectionPrompt = "What do you want most, and what are you willing to sacrifice for it?"
        ),
        QuoteEntity(
            content = "Action is the foundational key to all success.",
            author = "Pablo Picasso",
            category = "action",
            tags = "success,doing,achievement",
            reflectionPrompt = "What action have you been avoiding that could change everything?"
        ),
        QuoteEntity(
            content = "The secret of getting ahead is getting started.",
            author = "Mark Twain",
            category = "action",
            tags = "beginning,progress,motivation",
            reflectionPrompt = "What's one thing you can start right now?"
        ),
        QuoteEntity(
            content = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            author = "Aristotle",
            category = "action",
            tags = "habits,excellence,consistency",
            reflectionPrompt = "What habit are you building that will lead to excellence?"
        ),
        QuoteEntity(
            content = "Your life doesn't get better by chance, it gets better by change.",
            author = "Jim Rohn",
            category = "action",
            tags = "change,improvement,choice",
            reflectionPrompt = "What change do you need to make to improve your life?"
        ),
        QuoteEntity(
            content = "Success is the sum of small efforts, repeated day in and day out.",
            author = "Robert Collier",
            category = "action",
            tags = "consistency,effort,success",
            reflectionPrompt = "What small effort are you committed to repeating daily?"
        ),

        // Self-Compassion
        QuoteEntity(
            content = "Be gentle with yourself. You're doing the best you can.",
            author = "Kairos Wisdom",
            category = "self-compassion",
            tags = "kindness,self-care,acceptance",
            reflectionPrompt = "How can you be kinder to yourself today?"
        ),
        QuoteEntity(
            content = "You're allowed to be both a masterpiece and a work in progress.",
            author = "Sophia Bush",
            category = "self-compassion",
            tags = "growth,acceptance,self-love",
            reflectionPrompt = "What makes you a masterpiece right now, as you are?"
        ),
        QuoteEntity(
            content = "Talk to yourself like you would to someone you love.",
            author = "Brené Brown",
            category = "self-compassion",
            tags = "self-talk,kindness,love",
            reflectionPrompt = "What would you say to a friend facing your current situation?"
        ),
        QuoteEntity(
            content = "You yourself, as much as anybody in the entire universe, deserve your love and affection.",
            author = "Buddha",
            category = "self-compassion",
            tags = "self-love,worthiness,wisdom",
            reflectionPrompt = "In what ways do you deserve your own love today?"
        ),
        QuoteEntity(
            content = "Forgive yourself for not knowing what you didn't know before you learned it.",
            author = "Maya Angelou",
            category = "self-compassion",
            tags = "forgiveness,growth,wisdom",
            reflectionPrompt = "What do you need to forgive yourself for?"
        ),

        // Perspective
        QuoteEntity(
            content = "It's not what happens to you, but how you react to it that matters.",
            author = "Epictetus",
            category = "perspective",
            tags = "stoicism,reaction,choice",
            reflectionPrompt = "How can you choose a better reaction to something challenging?"
        ),
        QuoteEntity(
            content = "Between stimulus and response there is a space. In that space is our power to choose.",
            author = "Viktor Frankl",
            category = "perspective",
            tags = "choice,freedom,response",
            reflectionPrompt = "How can you expand the space between stimulus and response?"
        ),
        QuoteEntity(
            content = "Change the way you look at things and the things you look at change.",
            author = "Wayne Dyer",
            category = "perspective",
            tags = "perception,change,mindset",
            reflectionPrompt = "What situation could you see differently?"
        ),
        QuoteEntity(
            content = "Everything can be taken from a man but one thing: the last of the human freedoms—to choose one's attitude.",
            author = "Viktor Frankl",
            category = "perspective",
            tags = "freedom,choice,attitude",
            reflectionPrompt = "What attitude are you choosing today?"
        ),
        QuoteEntity(
            content = "In life, pain is inevitable but suffering is optional.",
            author = "Buddhist Proverb",
            category = "perspective",
            tags = "suffering,choice,wisdom",
            reflectionPrompt = "Where might you be adding unnecessary suffering to inevitable pain?"
        ),

        // Stoic Wisdom
        QuoteEntity(
            content = "We suffer more often in imagination than in reality.",
            author = "Seneca",
            category = "stoic",
            tags = "worry,imagination,wisdom",
            reflectionPrompt = "What worry might be worse in your mind than in reality?"
        ),
        QuoteEntity(
            content = "The happiness of your life depends upon the quality of your thoughts.",
            author = "Marcus Aurelius",
            category = "stoic",
            tags = "thoughts,happiness,mind",
            reflectionPrompt = "What thoughts do you need to improve to improve your life?"
        ),
        QuoteEntity(
            content = "Waste no more time arguing about what a good man should be. Be one.",
            author = "Marcus Aurelius",
            category = "stoic",
            tags = "action,virtue,character",
            reflectionPrompt = "How can you embody your values through action today?"
        ),
        QuoteEntity(
            content = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            author = "Marcus Aurelius",
            category = "stoic",
            tags = "control,mind,strength",
            reflectionPrompt = "What external event are you trying to control that you should let go?"
        ),
        QuoteEntity(
            content = "No man is free who is not master of himself.",
            author = "Epictetus",
            category = "stoic",
            tags = "self-mastery,freedom,discipline",
            reflectionPrompt = "In what area of your life do you need more self-mastery?"
        ),
        QuoteEntity(
            content = "It is not that we have a short time to live, but that we waste a lot of it.",
            author = "Seneca",
            category = "stoic",
            tags = "time,life,priorities",
            reflectionPrompt = "How can you use your time more wisely today?"
        ),
        QuoteEntity(
            content = "Luck is what happens when preparation meets opportunity.",
            author = "Seneca",
            category = "stoic",
            tags = "luck,preparation,opportunity",
            reflectionPrompt = "What opportunity are you preparing for?"
        ),

        // Success & Achievement
        QuoteEntity(
            content = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            author = "Winston Churchill",
            category = "success",
            tags = "courage,perseverance,failure",
            reflectionPrompt = "What do you need courage to continue doing?"
        ),
        QuoteEntity(
            content = "The only limit to our realization of tomorrow is our doubts of today.",
            author = "Franklin D. Roosevelt",
            category = "success",
            tags = "doubt,future,potential",
            reflectionPrompt = "What doubts are limiting your potential?"
        ),
        QuoteEntity(
            content = "Don't watch the clock; do what it does. Keep going.",
            author = "Sam Levenson",
            category = "success",
            tags = "persistence,time,motivation",
            reflectionPrompt = "What goal do you need to keep pursuing regardless of time?"
        ),
        QuoteEntity(
            content = "The difference between ordinary and extraordinary is that little extra.",
            author = "Jimmy Johnson",
            category = "success",
            tags = "effort,excellence,achievement",
            reflectionPrompt = "Where can you give a little extra today?"
        ),
        QuoteEntity(
            content = "It does not matter how slowly you go as long as you do not stop.",
            author = "Confucius",
            category = "success",
            tags = "persistence,progress,patience",
            reflectionPrompt = "What progress have you made recently, no matter how small?"
        )
    ) + ExpandedQuoteContent.quotes
        .distinctBy { it.content.trim().lowercase() }

    // =========================================================================
    // PROVERBS - Wisdom from cultures around the world
    // =========================================================================

    private fun getInitialProverbs(): List<ProverbEntity> = listOf(
        ProverbEntity(
            content = "A journey of a thousand miles begins with a single step.",
            meaning = "Great achievements start with small actions. Don't be overwhelmed by the magnitude of your goals.",
            origin = "Chinese",
            usage = "When someone is hesitant to start a large project or feels overwhelmed by a big goal.",
            category = "motivation"
        ),
        ProverbEntity(
            content = "The best time to plant a tree was twenty years ago. The second best time is now.",
            meaning = "While it's ideal to start things early, it's never too late to begin. Take action now rather than dwelling on missed opportunities.",
            origin = "Chinese",
            usage = "When encouraging someone who feels they've missed their chance or are too old to start something new.",
            category = "action"
        ),
        ProverbEntity(
            content = "Fall down seven times, stand up eight.",
            meaning = "Perseverance and resilience are key to success. No matter how many times you fail, keep getting back up.",
            origin = "Japanese",
            usage = "When someone has experienced setbacks and needs encouragement to continue.",
            category = "resilience"
        ),
        ProverbEntity(
            content = "The nail that sticks out gets hammered down.",
            meaning = "Those who stand out or act differently may face pressure to conform.",
            origin = "Japanese",
            usage = "When discussing conformity, individuality, or social pressure.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "A smooth sea never made a skilled sailor.",
            meaning = "Challenges and difficulties are necessary for growth and developing expertise.",
            origin = "English",
            usage = "When comforting someone going through hard times or encouraging them to embrace challenges.",
            category = "growth"
        ),
        ProverbEntity(
            content = "When the student is ready, the teacher will appear.",
            meaning = "Knowledge and wisdom come when you're truly open and prepared to receive them.",
            origin = "Buddhist",
            usage = "When discussing readiness for learning or when the right mentor appears unexpectedly.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "The bamboo that bends is stronger than the oak that resists.",
            meaning = "Flexibility and adaptability are often more effective than rigid resistance.",
            origin = "Japanese",
            usage = "When advising someone to be more flexible or adaptable in difficult situations.",
            category = "adaptability"
        ),
        ProverbEntity(
            content = "What you seek is seeking you.",
            meaning = "Your desires and goals are also drawn to you when you actively pursue them.",
            origin = "Persian (Rumi)",
            usage = "When encouraging someone to pursue their dreams or trust the process.",
            category = "motivation"
        ),
        ProverbEntity(
            content = "He who asks is a fool for five minutes. He who does not ask remains a fool forever.",
            meaning = "It's better to ask and learn than to stay ignorant out of fear of looking foolish.",
            origin = "Chinese",
            usage = "When encouraging someone to ask questions or seek help.",
            category = "learning"
        ),
        ProverbEntity(
            content = "A gem cannot be polished without friction, nor a man perfected without trials.",
            meaning = "Difficulties and challenges are necessary for personal growth and refinement.",
            origin = "Chinese",
            usage = "When comforting someone going through hardship or explaining the value of struggles.",
            category = "growth"
        ),
        ProverbEntity(
            content = "Vision without action is a daydream. Action without vision is a nightmare.",
            meaning = "You need both clear goals and concrete actions to achieve success.",
            origin = "Japanese",
            usage = "When advising someone to balance planning with execution.",
            category = "action"
        ),
        ProverbEntity(
            content = "The frog in the well knows nothing of the great ocean.",
            meaning = "Limited experience leads to a narrow worldview. Seek broader perspectives.",
            origin = "Japanese",
            usage = "When encouraging someone to expand their horizons or consider different viewpoints.",
            category = "perspective"
        ),
        ProverbEntity(
            content = "Time is money, but money cannot buy time.",
            meaning = "While time is valuable like money, it's actually more precious because it cannot be recovered.",
            origin = "Modern",
            usage = "When discussing priorities or the importance of time management.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "The tongue has no bones, but it can break hearts.",
            meaning = "Words can cause more damage than physical force. Speak thoughtfully.",
            origin = "Turkish",
            usage = "When advising someone about the power of words or after hurtful words were spoken.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "Where there is love, there is no darkness.",
            meaning = "Love illuminates and overcomes negativity and hardship.",
            origin = "African (Burundian)",
            usage = "When discussing the power of love to overcome difficulties.",
            category = "love"
        ),
        ProverbEntity(
            content = "A calm mind is a fortress.",
            meaning = "Inner peace provides protection against life's troubles.",
            origin = "Roman (Stoic)",
            usage = "When advising someone to cultivate calmness in stressful situations.",
            category = "mindfulness"
        ),
        ProverbEntity(
            content = "The heaviest burden is a promise unfulfilled.",
            meaning = "Breaking your word weighs heavily on the conscience. Honor your commitments.",
            origin = "Swedish",
            usage = "When discussing integrity, commitments, or broken promises.",
            category = "integrity"
        ),
        ProverbEntity(
            content = "In the middle of difficulty lies opportunity.",
            meaning = "Challenges often contain hidden opportunities for growth or success.",
            origin = "Greek",
            usage = "When someone is facing challenges and needs to see the potential upside.",
            category = "opportunity"
        ),
        ProverbEntity(
            content = "Knowledge speaks, but wisdom listens.",
            meaning = "True wisdom involves more listening than speaking.",
            origin = "African",
            usage = "When discussing the difference between knowledge and wisdom, or the importance of listening.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "The wound is the place where the light enters you.",
            meaning = "Our pain and struggles are often where we grow the most and find enlightenment.",
            origin = "Persian (Rumi)",
            usage = "When comforting someone going through pain or helping them see growth in suffering.",
            category = "growth"
        ),
        ProverbEntity(
            content = "A society grows great when old men plant trees whose shade they know they shall never sit in.",
            meaning = "True greatness comes from thinking beyond ourselves and investing in future generations.",
            origin = "Greek",
            usage = "When discussing legacy, long-term thinking, or generosity.",
            category = "wisdom"
        ),
        ProverbEntity(
            content = "The obstacle is the path.",
            meaning = "Our challenges are not detours but the very route to growth and success.",
            origin = "Zen Buddhist",
            usage = "When someone sees obstacles as problems rather than opportunities.",
            category = "growth"
        ),
        ProverbEntity(
            content = "If you want to go fast, go alone. If you want to go far, go together.",
            meaning = "Collaboration and teamwork are essential for lasting, significant achievements.",
            origin = "African",
            usage = "When discussing teamwork, collaboration, or the value of community.",
            category = "teamwork"
        ),
        ProverbEntity(
            content = "The river that forgets its source will dry up.",
            meaning = "Never forget your roots, origins, or the people who helped you succeed.",
            origin = "African (Yoruba)",
            usage = "When discussing gratitude, heritage, or staying grounded.",
            category = "gratitude"
        ),
        ProverbEntity(
            content = "A bird does not sing because it has an answer. It sings because it has a song.",
            meaning = "Express yourself for the joy of expression, not just to prove something.",
            origin = "Chinese",
            usage = "When encouraging self-expression or authentic living.",
            category = "authenticity"
        )
    )

    // =========================================================================
    // IDIOMS - Common expressions and their meanings
    // =========================================================================

    private fun getInitialIdioms(): List<IdiomEntity> = listOf(
        IdiomEntity(
            phrase = "Break the ice",
            meaning = "To initiate social interaction or conversation in an awkward situation",
            origin = "Refers to breaking the ice to allow ships to pass, or making a path for others to follow",
            exampleSentence = "She told a funny joke to break the ice at the beginning of the meeting.",
            category = "social"
        ),
        IdiomEntity(
            phrase = "Bite the bullet",
            meaning = "To endure a painful or difficult situation with courage",
            origin = "From the practice of having patients bite on a bullet during surgery before anesthesia",
            exampleSentence = "I decided to bite the bullet and have that difficult conversation with my boss.",
            category = "courage"
        ),
        IdiomEntity(
            phrase = "Burning the midnight oil",
            meaning = "To work late into the night",
            origin = "From the days when oil lamps were used for light, working late meant using more oil",
            exampleSentence = "She's been burning the midnight oil to finish the project before the deadline.",
            category = "work"
        ),
        IdiomEntity(
            phrase = "Beat around the bush",
            meaning = "To avoid getting to the main point, to be indirect",
            origin = "From hunting, where beaters would disturb bushes to drive out game birds",
            exampleSentence = "Stop beating around the bush and tell me what you really think.",
            category = "communication"
        ),
        IdiomEntity(
            phrase = "Hit the nail on the head",
            meaning = "To describe exactly what is causing a situation or problem",
            origin = "From carpentry, referring to the precision needed to drive a nail correctly",
            exampleSentence = "You hit the nail on the head when you said we need better communication.",
            category = "accuracy"
        ),
        IdiomEntity(
            phrase = "A blessing in disguise",
            meaning = "Something that seems bad at first but turns out to be good",
            origin = "The idea that good fortune sometimes comes in unexpected or unrecognizable forms",
            exampleSentence = "Losing that job was a blessing in disguise—it led me to find my true calling.",
            category = "perspective"
        ),
        IdiomEntity(
            phrase = "The ball is in your court",
            meaning = "It's your decision or responsibility to act next",
            origin = "From tennis, where the player must hit the ball back when it's on their side",
            exampleSentence = "I've made my offer. The ball is in your court now.",
            category = "responsibility"
        ),
        IdiomEntity(
            phrase = "Cost an arm and a leg",
            meaning = "To be very expensive",
            origin = "Possibly from portrait painting, where including limbs cost extra",
            exampleSentence = "That new car must have cost an arm and a leg!",
            category = "money"
        ),
        IdiomEntity(
            phrase = "Get out of hand",
            meaning = "To become uncontrollable",
            origin = "From horsemanship, when a rider loses control of the reins",
            exampleSentence = "The party started getting out of hand, so we had to end it early.",
            category = "control"
        ),
        IdiomEntity(
            phrase = "Let the cat out of the bag",
            meaning = "To reveal a secret accidentally",
            origin = "From the practice of selling piglets in bags, sometimes replacing them with cats",
            exampleSentence = "She let the cat out of the bag about the surprise party.",
            category = "secrets"
        ),
        IdiomEntity(
            phrase = "Once in a blue moon",
            meaning = "Very rarely, almost never",
            origin = "A blue moon (second full moon in a month) is a rare occurrence",
            exampleSentence = "I only eat fast food once in a blue moon.",
            category = "frequency"
        ),
        IdiomEntity(
            phrase = "Piece of cake",
            meaning = "Something very easy to do",
            origin = "From the ease of eating cake, or from cakewalk competitions",
            exampleSentence = "The exam was a piece of cake after all that studying.",
            category = "difficulty"
        ),
        IdiomEntity(
            phrase = "Spill the beans",
            meaning = "To reveal secret information",
            origin = "Possibly from ancient Greek voting with beans",
            exampleSentence = "Come on, spill the beans! What happened at the meeting?",
            category = "secrets"
        ),
        IdiomEntity(
            phrase = "Under the weather",
            meaning = "Feeling ill or unwell",
            origin = "From sailors going below deck during bad weather to feel better",
            exampleSentence = "I'm feeling a bit under the weather today, so I'll stay home.",
            category = "health"
        ),
        IdiomEntity(
            phrase = "When pigs fly",
            meaning = "Something that will never happen",
            origin = "An obvious impossibility used for humorous effect",
            exampleSentence = "He'll clean his room when pigs fly.",
            category = "impossibility"
        ),
        IdiomEntity(
            phrase = "Actions speak louder than words",
            meaning = "What you do is more important than what you say",
            origin = "First recorded in the 1600s, the concept dates back to ancient times",
            exampleSentence = "You keep promising to help, but actions speak louder than words.",
            category = "behavior"
        ),
        IdiomEntity(
            phrase = "Barking up the wrong tree",
            meaning = "Pursuing a mistaken or misguided course of action",
            origin = "From hunting dogs barking at a tree where they think prey has escaped",
            exampleSentence = "If you think I took your book, you're barking up the wrong tree.",
            category = "mistakes"
        ),
        IdiomEntity(
            phrase = "Cut to the chase",
            meaning = "Get to the point, skip the unnecessary details",
            origin = "From film editing, cutting to the exciting chase scenes",
            exampleSentence = "Let's cut to the chase—are you interested in the job or not?",
            category = "communication"
        ),
        IdiomEntity(
            phrase = "Devil's advocate",
            meaning = "Someone who argues an opposing viewpoint for the sake of debate",
            origin = "From the Catholic Church's process of canonization",
            exampleSentence = "Let me play devil's advocate here—what if we're wrong about this?",
            category = "debate"
        ),
        IdiomEntity(
            phrase = "Every cloud has a silver lining",
            meaning = "There's something positive in every negative situation",
            origin = "From John Milton's poem 'Comus' (1634)",
            exampleSentence = "Losing the game was disappointing, but every cloud has a silver lining—we learned a lot.",
            category = "optimism"
        ),
        IdiomEntity(
            phrase = "Give someone the benefit of the doubt",
            meaning = "Trust someone's word without proof",
            origin = "Legal term meaning to favor the accused when evidence is uncertain",
            exampleSentence = "I'll give him the benefit of the doubt and assume he had a good reason for being late.",
            category = "trust"
        ),
        IdiomEntity(
            phrase = "Keep your chin up",
            meaning = "Stay positive and cheerful during difficult times",
            origin = "The physical posture of keeping one's head high suggests confidence",
            exampleSentence = "Keep your chin up—things will get better soon.",
            category = "encouragement"
        ),
        IdiomEntity(
            phrase = "Leave no stone unturned",
            meaning = "Search thoroughly, explore every possibility",
            origin = "From an ancient Greek legend about finding treasure",
            exampleSentence = "The detective promised to leave no stone unturned in the investigation.",
            category = "thoroughness"
        ),
        IdiomEntity(
            phrase = "Put all your eggs in one basket",
            meaning = "Risk everything on a single venture",
            origin = "The obvious risk of losing all eggs if the basket is dropped",
            exampleSentence = "Don't put all your eggs in one basket—diversify your investments.",
            category = "risk"
        ),
        IdiomEntity(
            phrase = "The elephant in the room",
            meaning = "An obvious problem that everyone ignores",
            origin = "The idea that something as large as an elephant couldn't be overlooked",
            exampleSentence = "We need to address the elephant in the room: our budget is running out.",
            category = "problems"
        )
    )

    // =========================================================================
    // PHRASES - Useful expressions for everyday communication
    // =========================================================================

    private fun getInitialPhrases(): List<PhraseEntity> = listOf(
        PhraseEntity(
            phrase = "I appreciate your perspective",
            meaning = "A polite way to acknowledge someone's viewpoint, even if you disagree",
            usage = "Use when you want to be respectful during a discussion or disagreement",
            exampleSentence = "I appreciate your perspective on this issue, but I see it differently.",
            formality = "formal",
            category = "professional"
        ),
        PhraseEntity(
            phrase = "Let's circle back on this",
            meaning = "To return to a topic later for further discussion",
            usage = "Use in meetings when you need to postpone a discussion",
            exampleSentence = "Let's circle back on this after we have more data.",
            formality = "formal",
            category = "business"
        ),
        PhraseEntity(
            phrase = "I'm all ears",
            meaning = "I'm listening carefully and am ready to hear what you have to say",
            usage = "Use when you want to show genuine interest in what someone is about to tell you",
            exampleSentence = "You wanted to talk? I'm all ears.",
            formality = "informal",
            category = "conversation"
        ),
        PhraseEntity(
            phrase = "That resonates with me",
            meaning = "I deeply connect with or understand what you're saying",
            usage = "Use when something said has emotional or intellectual impact on you",
            exampleSentence = "Your story about overcoming fear really resonates with me.",
            formality = "neutral",
            category = "emotional"
        ),
        PhraseEntity(
            phrase = "Let me play devil's advocate",
            meaning = "I'll argue the opposite position to test the strength of an argument",
            usage = "Use before presenting a counterargument in a constructive discussion",
            exampleSentence = "Let me play devil's advocate—what if the market changes?",
            formality = "neutral",
            category = "discussion"
        ),
        PhraseEntity(
            phrase = "I'd like to pick your brain",
            meaning = "I want to learn from your knowledge or experience",
            usage = "Use when asking someone knowledgeable for their insights",
            exampleSentence = "Could I pick your brain about your experience in marketing?",
            formality = "informal",
            category = "learning"
        ),
        PhraseEntity(
            phrase = "Let's touch base later",
            meaning = "Let's communicate again soon to discuss progress or updates",
            usage = "Use to arrange follow-up communication, especially in professional settings",
            exampleSentence = "Let's touch base later this week to see how the project is going.",
            formality = "formal",
            category = "business"
        ),
        PhraseEntity(
            phrase = "I'm on board with that",
            meaning = "I agree and am willing to participate or support",
            usage = "Use to express agreement and commitment to a plan or idea",
            exampleSentence = "That sounds like a great plan—I'm on board with that.",
            formality = "neutral",
            category = "agreement"
        ),
        PhraseEntity(
            phrase = "Can we table this for now?",
            meaning = "Can we postpone this discussion to a later time?",
            usage = "Use when you want to delay a discussion, often in meetings",
            exampleSentence = "Can we table this for now and focus on more urgent matters?",
            formality = "formal",
            category = "business"
        ),
        PhraseEntity(
            phrase = "I'll keep that in mind",
            meaning = "I'll remember and consider what you've said",
            usage = "A polite acknowledgment of advice or information",
            exampleSentence = "That's a good point. I'll keep that in mind.",
            formality = "neutral",
            category = "acknowledgment"
        ),
        PhraseEntity(
            phrase = "Let me get back to you on that",
            meaning = "I need more time or information before I can respond",
            usage = "Use when you can't give an immediate answer",
            exampleSentence = "That's a complex question. Let me get back to you on that tomorrow.",
            formality = "formal",
            category = "professional"
        ),
        PhraseEntity(
            phrase = "I hear you",
            meaning = "I understand and acknowledge your feelings or point of view",
            usage = "Use to show empathy and understanding, even if you can't solve the problem",
            exampleSentence = "I hear you—this situation is really frustrating.",
            formality = "informal",
            category = "empathy"
        ),
        PhraseEntity(
            phrase = "That's food for thought",
            meaning = "That's something worth considering carefully",
            usage = "Use when someone presents an interesting idea that deserves reflection",
            exampleSentence = "Your point about sustainability is definitely food for thought.",
            formality = "neutral",
            category = "consideration"
        ),
        PhraseEntity(
            phrase = "I'm giving it my all",
            meaning = "I'm putting in maximum effort",
            usage = "Use to express your commitment and hard work",
            exampleSentence = "This project is challenging, but I'm giving it my all.",
            formality = "neutral",
            category = "effort"
        ),
        PhraseEntity(
            phrase = "Let's not reinvent the wheel",
            meaning = "Let's use existing solutions instead of creating new ones from scratch",
            usage = "Use when suggesting efficiency over unnecessary innovation",
            exampleSentence = "There's already a good template for this. Let's not reinvent the wheel.",
            formality = "neutral",
            category = "efficiency"
        ),
        PhraseEntity(
            phrase = "I'm open to suggestions",
            meaning = "I welcome input and ideas from others",
            usage = "Use to invite collaboration and show flexibility",
            exampleSentence = "I have a draft plan, but I'm open to suggestions.",
            formality = "neutral",
            category = "collaboration"
        ),
        PhraseEntity(
            phrase = "Let's hit the ground running",
            meaning = "Let's start immediately with full energy and commitment",
            usage = "Use when beginning a project or task with enthusiasm",
            exampleSentence = "We have a tight deadline, so let's hit the ground running.",
            formality = "neutral",
            category = "action"
        ),
        PhraseEntity(
            phrase = "I stand corrected",
            meaning = "I acknowledge that I was wrong",
            usage = "A graceful way to admit an error",
            exampleSentence = "You're right—the meeting is on Tuesday, not Wednesday. I stand corrected.",
            formality = "formal",
            category = "admission"
        ),
        PhraseEntity(
            phrase = "At the end of the day",
            meaning = "When everything is considered, ultimately",
            usage = "Use to summarize or conclude a point",
            exampleSentence = "At the end of the day, what matters most is that we tried our best.",
            formality = "informal",
            category = "conclusion"
        ),
        PhraseEntity(
            phrase = "I'm curious to know more",
            meaning = "I'm interested and want additional information",
            usage = "Use to express genuine interest and encourage someone to elaborate",
            exampleSentence = "That sounds fascinating—I'm curious to know more.",
            formality = "neutral",
            category = "interest"
        ),
        PhraseEntity(
            phrase = "That's a fair point",
            meaning = "Your argument is valid and reasonable",
            usage = "Use to acknowledge the validity of someone's argument",
            exampleSentence = "That's a fair point. I hadn't considered that angle.",
            formality = "neutral",
            category = "acknowledgment"
        ),
        PhraseEntity(
            phrase = "Let's agree to disagree",
            meaning = "Let's accept that we have different opinions without further argument",
            usage = "Use to end a debate amicably when neither side will change their mind",
            exampleSentence = "We clearly see this differently. Let's agree to disagree and move on.",
            formality = "neutral",
            category = "resolution"
        ),
        PhraseEntity(
            phrase = "I'm doing my best under the circumstances",
            meaning = "I'm performing as well as possible given the difficulties",
            usage = "Use when facing challenges that affect your performance",
            exampleSentence = "With the reduced budget, I'm doing my best under the circumstances.",
            formality = "neutral",
            category = "effort"
        ),
        PhraseEntity(
            phrase = "That's water under the bridge",
            meaning = "That's in the past and no longer relevant or worth discussing",
            usage = "Use to indicate forgiveness or that past issues are forgotten",
            exampleSentence = "Don't worry about what happened last year—that's water under the bridge.",
            formality = "informal",
            category = "forgiveness"
        ),
        PhraseEntity(
            phrase = "I'm cautiously optimistic",
            meaning = "I'm hopeful but also aware of potential problems",
            usage = "Use when expressing measured hope about an uncertain outcome",
            exampleSentence = "The results look promising—I'm cautiously optimistic.",
            formality = "formal",
            category = "outlook"
        )
    )

    // =========================================================================
    // VOCABULARY - Curated words for vocabulary building
    // =========================================================================

    private suspend fun seedVocabulary(database: KairosDatabase) {
        try {
            val vocabularyDao = database.vocabularyDao()
            vocabularyDao.insertWords(getInitialVocabulary())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding vocabulary", e)
        }
    }

    private fun getInitialVocabulary(): List<VocabularyEntity> = listOf(
        // Personal Growth & Development
        VocabularyEntity(
            word = "Resilience",
            definition = "The capacity to recover quickly from difficulties; toughness and mental fortitude",
            pronunciation = "ri-ZIL-yuhns",
            partOfSpeech = "noun",
            exampleSentence = "Her resilience in the face of adversity inspired everyone around her.",
            synonyms = "toughness, flexibility, adaptability, strength, grit",
            antonyms = "fragility, weakness, rigidity, brittleness",
            origin = "From Latin 'resilire' meaning 'to spring back'",
            difficulty = 2,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Equanimity",
            definition = "Mental calmness and composure, especially in difficult situations",
            pronunciation = "ee-kwuh-NIM-i-tee",
            partOfSpeech = "noun",
            exampleSentence = "The monk faced both praise and criticism with remarkable equanimity.",
            synonyms = "composure, calmness, serenity, poise, tranquility",
            antonyms = "anxiety, agitation, distress, turbulence",
            origin = "From Latin 'aequanimitas', from 'aequus' (equal) + 'animus' (mind)",
            difficulty = 4,
            category = "mindfulness"
        ),
        VocabularyEntity(
            word = "Perseverance",
            definition = "Persistence in doing something despite difficulty or delay in achieving success",
            pronunciation = "pur-suh-VEER-uhns",
            partOfSpeech = "noun",
            exampleSentence = "Through sheer perseverance, she completed the marathon despite her injury.",
            synonyms = "persistence, determination, tenacity, steadfastness",
            antonyms = "laziness, idleness, weakness, irresolution",
            origin = "From Latin 'perseverare' meaning 'to persist'",
            difficulty = 2,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Introspection",
            definition = "The examination of one's own thoughts, feelings, and mental processes",
            pronunciation = "in-truh-SPEK-shuhn",
            partOfSpeech = "noun",
            exampleSentence = "Regular introspection helps us understand ourselves better and grow.",
            synonyms = "self-examination, reflection, contemplation, soul-searching",
            antonyms = "distraction, thoughtlessness, inattention",
            origin = "From Latin 'introspicere' meaning 'to look into'",
            difficulty = 3,
            category = "self_awareness"
        ),
        VocabularyEntity(
            word = "Serendipity",
            definition = "The occurrence of events by chance in a happy or beneficial way",
            pronunciation = "ser-uhn-DIP-i-tee",
            partOfSpeech = "noun",
            exampleSentence = "Finding that old book at the garage sale was pure serendipity.",
            synonyms = "chance, fortune, luck, fate, providence",
            antonyms = "misfortune, design, intention",
            origin = "Coined by Horace Walpole in 1754, from the Persian fairy tale 'The Three Princes of Serendip'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Ephemeral",
            definition = "Lasting for a very short time; transient and fleeting",
            pronunciation = "ih-FEM-er-uhl",
            partOfSpeech = "adjective",
            exampleSentence = "The ephemeral beauty of cherry blossoms reminds us to appreciate the present moment.",
            synonyms = "fleeting, transient, momentary, brief, short-lived",
            antonyms = "permanent, lasting, enduring, eternal",
            origin = "From Greek 'ephemeros' meaning 'lasting only a day'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Sanguine",
            definition = "Optimistic, especially in difficult situations; cheerfully confident",
            pronunciation = "SANG-gwin",
            partOfSpeech = "adjective",
            exampleSentence = "Despite the setbacks, she remained sanguine about the project's success.",
            synonyms = "optimistic, hopeful, confident, positive, buoyant",
            antonyms = "pessimistic, gloomy, despairing, morose",
            origin = "From Latin 'sanguineus' meaning 'blood-red', related to the medieval belief in humors",
            difficulty = 3,
            category = "emotional"
        ),
        VocabularyEntity(
            word = "Magnanimous",
            definition = "Generous or forgiving, especially toward a rival or less powerful person",
            pronunciation = "mag-NAN-uh-muhs",
            partOfSpeech = "adjective",
            exampleSentence = "The magnanimous leader pardoned his defeated opponents.",
            synonyms = "generous, noble, benevolent, charitable, gracious",
            antonyms = "petty, mean-spirited, vindictive, spiteful",
            origin = "From Latin 'magnus' (great) + 'animus' (soul)",
            difficulty = 3,
            category = "stoic"
        ),
        VocabularyEntity(
            word = "Perspicacious",
            definition = "Having a ready insight into things; shrewd and discerning",
            pronunciation = "pur-spi-KAY-shuhs",
            partOfSpeech = "adjective",
            exampleSentence = "Her perspicacious analysis of the market trends saved the company millions.",
            synonyms = "astute, shrewd, perceptive, insightful, sharp",
            antonyms = "obtuse, dense, unperceptive, dull",
            origin = "From Latin 'perspicax' meaning 'sharp-sighted'",
            difficulty = 5,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Eudaimonia",
            definition = "A state of flourishing; human flourishing, prosperity, and well-being",
            pronunciation = "yoo-dy-MOH-nee-uh",
            partOfSpeech = "noun",
            exampleSentence = "The philosopher argued that eudaimonia, not pleasure, should be our ultimate goal.",
            synonyms = "flourishing, well-being, fulfillment, thriving",
            antonyms = "misery, suffering, languishing, wretchedness",
            origin = "Ancient Greek concept central to Aristotelian ethics",
            difficulty = 5,
            category = "philosophical"
        ),
        VocabularyEntity(
            word = "Stoic",
            definition = "A person who can endure pain or hardship without showing feelings or complaining",
            pronunciation = "STOH-ik",
            partOfSpeech = "noun/adjective",
            exampleSentence = "He remained stoic throughout the difficult ordeal.",
            synonyms = "impassive, unemotional, calm, phlegmatic",
            antonyms = "emotional, expressive, demonstrative",
            origin = "From the ancient Greek philosophical school of Stoicism, founded in Athens",
            difficulty = 2,
            category = "stoic"
        ),
        VocabularyEntity(
            word = "Mellifluous",
            definition = "Sweet-sounding; pleasant and smooth to hear",
            pronunciation = "muh-LIF-loo-uhs",
            partOfSpeech = "adjective",
            exampleSentence = "The singer's mellifluous voice captivated the entire audience.",
            synonyms = "dulcet, harmonious, melodious, honeyed, sweet",
            antonyms = "harsh, grating, discordant, jarring",
            origin = "From Latin 'mel' (honey) + 'fluere' (to flow)",
            difficulty = 4,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Laconic",
            definition = "Using very few words; brief and to the point",
            pronunciation = "luh-KON-ik",
            partOfSpeech = "adjective",
            exampleSentence = "His laconic response of 'If.' became legendary in Spartan history.",
            synonyms = "concise, terse, succinct, pithy, brief",
            antonyms = "verbose, wordy, loquacious, garrulous",
            origin = "From Laconia, the region of Greece around Sparta, whose people were known for brevity",
            difficulty = 3,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Zeitgeist",
            definition = "The defining spirit or mood of a particular period of history",
            pronunciation = "ZITE-gyst",
            partOfSpeech = "noun",
            exampleSentence = "The music of the 1960s perfectly captured the zeitgeist of social revolution.",
            synonyms = "spirit of the age, ethos, mood, atmosphere, climate",
            antonyms = "N/A",
            origin = "German: 'Zeit' (time) + 'Geist' (spirit)",
            difficulty = 3,
            category = "philosophical"
        ),
        VocabularyEntity(
            word = "Ineffable",
            definition = "Too great or extreme to be expressed in words; indescribable",
            pronunciation = "in-EF-uh-buhl",
            partOfSpeech = "adjective",
            exampleSentence = "The beauty of the sunset was simply ineffable.",
            synonyms = "indescribable, inexpressible, unspeakable, unutterable",
            antonyms = "describable, expressible, definable",
            origin = "From Latin 'ineffabilis' meaning 'unutterable'",
            difficulty = 4,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Sonder",
            definition = "The realization that each passerby has a life as vivid and complex as your own",
            pronunciation = "SON-der",
            partOfSpeech = "noun",
            exampleSentence = "Sitting in the busy cafe, she experienced a profound sense of sonder.",
            synonyms = "empathy, awareness, realization, epiphany",
            antonyms = "self-absorption, obliviousness, narcissism",
            origin = "Neologism coined in 2012 by John Koenig for The Dictionary of Obscure Sorrows",
            difficulty = 3,
            category = "philosophical"
        ),
        VocabularyEntity(
            word = "Efficacious",
            definition = "Successful in producing a desired result; effective",
            pronunciation = "ef-i-KAY-shuhs",
            partOfSpeech = "adjective",
            exampleSentence = "The new treatment proved highly efficacious in clinical trials.",
            synonyms = "effective, productive, successful, potent, powerful",
            antonyms = "ineffective, useless, futile, impotent",
            origin = "From Latin 'efficax' meaning 'powerful, effectual'",
            difficulty = 4,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Prosaic",
            definition = "Lacking poetic beauty; commonplace and unromantic",
            pronunciation = "proh-ZAY-ik",
            partOfSpeech = "adjective",
            exampleSentence = "He described their adventure in prosaic terms that failed to capture its magic.",
            synonyms = "mundane, ordinary, pedestrian, banal, dull",
            antonyms = "poetic, imaginative, romantic, inspiring",
            origin = "From Latin 'prosa' meaning 'straightforward discourse'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Vicissitude",
            definition = "A change of circumstances or fortune, typically one that is unwelcome",
            pronunciation = "vih-SIS-ih-tood",
            partOfSpeech = "noun",
            exampleSentence = "The vicissitudes of life have taught me to be adaptable.",
            synonyms = "change, alteration, shift, fluctuation, variation",
            antonyms = "stability, constancy, permanence, steadiness",
            origin = "From Latin 'vicissitudo' meaning 'change, alternation'",
            difficulty = 4,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Paradigm",
            definition = "A typical example or pattern of something; a model",
            pronunciation = "PAIR-uh-dime",
            partOfSpeech = "noun",
            exampleSentence = "The discovery shifted the scientific paradigm entirely.",
            synonyms = "model, pattern, example, standard, prototype",
            antonyms = "anomaly, exception, deviation",
            origin = "From Greek 'paradeigma' meaning 'pattern, example'",
            difficulty = 3,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Catharsis",
            definition = "The process of releasing strong emotions through a particular activity or experience",
            pronunciation = "kuh-THAR-sis",
            partOfSpeech = "noun",
            exampleSentence = "Writing in her journal provided a much-needed catharsis after the stressful week.",
            synonyms = "release, purging, cleansing, purification, relief",
            antonyms = "suppression, repression, bottling up",
            origin = "From Greek 'katharsis' meaning 'purification, cleansing'",
            difficulty = 3,
            category = "emotional"
        ),
        VocabularyEntity(
            word = "Ubiquitous",
            definition = "Present, appearing, or found everywhere",
            pronunciation = "yoo-BIK-wi-tuhs",
            partOfSpeech = "adjective",
            exampleSentence = "Smartphones have become ubiquitous in modern society.",
            synonyms = "omnipresent, everywhere, universal, pervasive",
            antonyms = "rare, scarce, uncommon, localized",
            origin = "From Latin 'ubique' meaning 'everywhere'",
            difficulty = 3,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Ephemerality",
            definition = "The quality of lasting for only a short time; impermanence",
            pronunciation = "ih-FEM-er-AL-ih-tee",
            partOfSpeech = "noun",
            exampleSentence = "The ephemerality of the season made every warm evening feel precious.",
            synonyms = "impermanence, transience, fleetingness, brevity",
            antonyms = "permanence, endurance, eternity",
            origin = "From Greek 'ephemeros' meaning 'lasting only a day'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Sycophant",
            definition = "A person who acts obsequiously toward someone important to gain advantage",
            pronunciation = "SIK-uh-fant",
            partOfSpeech = "noun",
            exampleSentence = "The office sycophant was always agreeing with everything the boss said.",
            synonyms = "flatterer, toady, yes-man, bootlicker",
            antonyms = "critic, detractor, opponent",
            origin = "From Greek 'sykophantes', originally meaning 'informer'",
            difficulty = 4,
            category = "social"
        ),
        VocabularyEntity(
            word = "Pragmatic",
            definition = "Dealing with things sensibly and realistically based on practical considerations",
            pronunciation = "prag-MAT-ik",
            partOfSpeech = "adjective",
            exampleSentence = "We need a pragmatic approach to solve this problem.",
            synonyms = "practical, realistic, sensible, matter-of-fact",
            antonyms = "idealistic, impractical, unrealistic, theoretical",
            origin = "From Greek 'pragmatikos' meaning 'fit for business'",
            difficulty = 2,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Altruistic",
            definition = "Showing a selfless concern for the well-being of others",
            pronunciation = "al-troo-IS-tik",
            partOfSpeech = "adjective",
            exampleSentence = "Her altruistic nature led her to volunteer at the homeless shelter.",
            synonyms = "selfless, unselfish, philanthropic, charitable",
            antonyms = "selfish, self-centered, egotistic, narcissistic",
            origin = "From French 'altruisme', coined by Auguste Comte from Latin 'alter' (other)",
            difficulty = 3,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Tenacious",
            definition = "Tending to keep a firm hold of something; determined and persistent",
            pronunciation = "tuh-NAY-shuhs",
            partOfSpeech = "adjective",
            exampleSentence = "Her tenacious pursuit of justice eventually paid off.",
            synonyms = "persistent, determined, resolute, steadfast, dogged",
            antonyms = "weak, irresolute, yielding, fickle",
            origin = "From Latin 'tenax' meaning 'holding fast'",
            difficulty = 3,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Eloquent",
            definition = "Fluent or persuasive in speaking or writing",
            pronunciation = "EL-uh-kwuhnt",
            partOfSpeech = "adjective",
            exampleSentence = "His eloquent speech moved the entire audience to tears.",
            synonyms = "articulate, fluent, persuasive, expressive",
            antonyms = "inarticulate, tongue-tied, halting, stumbling",
            origin = "From Latin 'eloquens' meaning 'speaking out'",
            difficulty = 2,
            category = "communication"
        ),
        VocabularyEntity(
            word = "Empathy",
            definition = "The ability to understand and share the feelings of another",
            pronunciation = "EM-puh-thee",
            partOfSpeech = "noun",
            exampleSentence = "Her empathy for the suffering of others made her an excellent counselor.",
            synonyms = "understanding, compassion, sensitivity, insight",
            antonyms = "apathy, indifference, coldness, callousness",
            origin = "From Greek 'empatheia' meaning 'passion, state of emotion'",
            difficulty = 2,
            category = "emotional"
        ),
        VocabularyEntity(
            word = "Verbose",
            definition = "Using or expressed in more words than are needed",
            pronunciation = "ver-BOHS",
            partOfSpeech = "adjective",
            exampleSentence = "His verbose explanations often confused rather than clarified.",
            synonyms = "wordy, long-winded, prolix, garrulous",
            antonyms = "concise, succinct, brief, terse, laconic",
            origin = "From Latin 'verbosus' meaning 'full of words'",
            difficulty = 2,
            category = "communication"
        ),
        VocabularyEntity(
            word = "Candid",
            definition = "Truthful and straightforward; frank",
            pronunciation = "KAN-did",
            partOfSpeech = "adjective",
            exampleSentence = "I appreciate your candid feedback on my presentation.",
            synonyms = "frank, open, honest, sincere, forthright",
            antonyms = "guarded, evasive, deceitful, dishonest",
            origin = "From Latin 'candidus' meaning 'white, pure'",
            difficulty = 2,
            category = "communication"
        ),
        VocabularyEntity(
            word = "Ambivalent",
            definition = "Having mixed feelings or contradictory ideas about something",
            pronunciation = "am-BIV-uh-luhnt",
            partOfSpeech = "adjective",
            exampleSentence = "She felt ambivalent about accepting the job offer in another city.",
            synonyms = "uncertain, unsure, undecided, conflicted, torn",
            antonyms = "certain, decisive, sure, resolved",
            origin = "From Latin 'ambi' (both) + 'valere' (be strong)",
            difficulty = 3,
            category = "emotional"
        ),
        VocabularyEntity(
            word = "Prudent",
            definition = "Acting with or showing care and thought for the future",
            pronunciation = "PROO-duhnt",
            partOfSpeech = "adjective",
            exampleSentence = "It would be prudent to save money for unexpected expenses.",
            synonyms = "wise, sensible, careful, judicious, cautious",
            antonyms = "reckless, imprudent, careless, foolish",
            origin = "From Latin 'prudens' meaning 'foreseeing, skilled'",
            difficulty = 2,
            category = "wisdom"
        ),
        VocabularyEntity(
            word = "Gregarious",
            definition = "Fond of company; sociable",
            pronunciation = "grih-GAIR-ee-uhs",
            partOfSpeech = "adjective",
            exampleSentence = "Her gregarious personality made her the life of every party.",
            synonyms = "sociable, outgoing, friendly, convivial",
            antonyms = "introverted, unsociable, antisocial, reclusive",
            origin = "From Latin 'gregarius' meaning 'of a flock'",
            difficulty = 3,
            category = "social"
        ),
        VocabularyEntity(
            word = "Frugal",
            definition = "Sparing or economical with regard to money or food",
            pronunciation = "FROO-guhl",
            partOfSpeech = "adjective",
            exampleSentence = "His frugal habits allowed him to retire early.",
            synonyms = "thrifty, economical, sparing, careful",
            antonyms = "extravagant, wasteful, profligate, lavish",
            origin = "From Latin 'frugalis' meaning 'economical'",
            difficulty = 2,
            category = "lifestyle"
        ),
        VocabularyEntity(
            word = "Enigmatic",
            definition = "Difficult to interpret or understand; mysterious",
            pronunciation = "en-ig-MAT-ik",
            partOfSpeech = "adjective",
            exampleSentence = "The Mona Lisa's enigmatic smile has fascinated viewers for centuries.",
            synonyms = "mysterious, puzzling, inscrutable, cryptic",
            antonyms = "straightforward, clear, obvious, transparent",
            origin = "From Greek 'ainigma' meaning 'riddle'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Astute",
            definition = "Having or showing an ability to accurately assess situations and turn to advantage",
            pronunciation = "uh-STOOT",
            partOfSpeech = "adjective",
            exampleSentence = "Her astute observations helped identify the problem quickly.",
            synonyms = "shrewd, sharp, clever, intelligent, perceptive",
            antonyms = "stupid, naive, foolish, obtuse",
            origin = "From Latin 'astutus' meaning 'crafty'",
            difficulty = 3,
            category = "intelligence"
        ),
        VocabularyEntity(
            word = "Diligent",
            definition = "Having or showing care and conscientiousness in one's work or duties",
            pronunciation = "DIL-ih-juhnt",
            partOfSpeech = "adjective",
            exampleSentence = "Her diligent work ethic earned her a promotion within a year.",
            synonyms = "industrious, hardworking, assiduous, conscientious",
            antonyms = "lazy, careless, negligent, idle",
            origin = "From Latin 'diligere' meaning 'to value highly'",
            difficulty = 2,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Meticulous",
            definition = "Showing great attention to detail; very careful and precise",
            pronunciation = "muh-TIK-yuh-luhs",
            partOfSpeech = "adjective",
            exampleSentence = "His meticulous planning ensured the event's success.",
            synonyms = "careful, precise, thorough, painstaking, scrupulous",
            antonyms = "careless, sloppy, negligent, haphazard",
            origin = "From Latin 'meticulosus' meaning 'fearful'",
            difficulty = 2,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Conundrum",
            definition = "A confusing and difficult problem or question",
            pronunciation = "kuh-NUN-drum",
            partOfSpeech = "noun",
            exampleSentence = "The ethical conundrum kept the committee debating for hours.",
            synonyms = "puzzle, riddle, enigma, mystery, dilemma",
            antonyms = "solution, answer, clarity",
            origin = "Origin uncertain, possibly pseudo-Latin wordplay from 16th century",
            difficulty = 3,
            category = "academic"
        ),
        VocabularyEntity(
            word = "Gratitude",
            definition = "The quality of being thankful; readiness to show appreciation",
            pronunciation = "GRAT-ih-tood",
            partOfSpeech = "noun",
            exampleSentence = "Practicing gratitude daily has been shown to improve mental health.",
            synonyms = "thankfulness, appreciation, gratefulness, recognition",
            antonyms = "ingratitude, ungratefulness, thanklessness",
            origin = "From Latin 'gratitudo' meaning 'thankfulness'",
            difficulty = 2,
            category = "mindfulness"
        ),
        VocabularyEntity(
            word = "Mindfulness",
            definition = "A mental state achieved by focusing awareness on the present moment",
            pronunciation = "MIND-fuhl-nis",
            partOfSpeech = "noun",
            exampleSentence = "Mindfulness meditation has helped her manage stress effectively.",
            synonyms = "awareness, attention, consciousness, presence",
            antonyms = "distraction, inattention, absent-mindedness",
            origin = "From Pali 'sati', a Buddhist concept translated as 'mindfulness'",
            difficulty = 2,
            category = "mindfulness"
        ),
        VocabularyEntity(
            word = "Humility",
            definition = "A modest view of one's own importance; humbleness",
            pronunciation = "hyoo-MIL-ih-tee",
            partOfSpeech = "noun",
            exampleSentence = "Despite his success, he approached life with genuine humility.",
            synonyms = "modesty, humbleness, unpretentiousness, meekness",
            antonyms = "arrogance, pride, conceit, vanity",
            origin = "From Latin 'humilitas' meaning 'lowness, small stature'",
            difficulty = 2,
            category = "stoic"
        ),
        VocabularyEntity(
            word = "Integrity",
            definition = "The quality of being honest and having strong moral principles",
            pronunciation = "in-TEG-rih-tee",
            partOfSpeech = "noun",
            exampleSentence = "Her integrity made her someone everyone could trust.",
            synonyms = "honesty, honor, probity, righteousness, virtue",
            antonyms = "dishonesty, corruption, deceitfulness",
            origin = "From Latin 'integer' meaning 'whole, complete'",
            difficulty = 2,
            category = "personal_growth"
        ),
        VocabularyEntity(
            word = "Auspicious",
            definition = "Conducive to success; favorable",
            pronunciation = "aw-SPISH-uhs",
            partOfSpeech = "adjective",
            exampleSentence = "The sunny weather seemed an auspicious start to their wedding day.",
            synonyms = "favorable, propitious, promising, fortunate",
            antonyms = "inauspicious, unfavorable, ominous, unpromising",
            origin = "From Latin 'auspicium' meaning 'divination by observing birds'",
            difficulty = 3,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Quintessential",
            definition = "Representing the most perfect or typical example of a quality or class",
            pronunciation = "kwin-tuh-SEN-shuhl",
            partOfSpeech = "adjective",
            exampleSentence = "She is the quintessential example of a dedicated teacher.",
            synonyms = "typical, classic, perfect, ideal, archetypal",
            antonyms = "atypical, unusual, unrepresentative",
            origin = "From Latin 'quinta essentia' meaning 'fifth essence'",
            difficulty = 4,
            category = "literary"
        ),
        VocabularyEntity(
            word = "Benevolent",
            definition = "Well meaning and kindly; charitable",
            pronunciation = "buh-NEV-uh-luhnt",
            partOfSpeech = "adjective",
            exampleSentence = "The benevolent donor funded scholarships for underprivileged students.",
            synonyms = "kind, charitable, generous, compassionate, philanthropic",
            antonyms = "malevolent, unkind, cruel, selfish",
            origin = "From Latin 'bene' (well) + 'volens' (wishing)",
            difficulty = 3,
            category = "personal_growth"
        )
    ) + ExpandedVocabularyContent.allWords
        .distinctBy { it.word.trim().lowercase() }

    

    // =========================================================================
    // USER PROFILE - Initial profile setup
    // =========================================================================

    private suspend fun seedUserProfile(database: KairosDatabase) {
        try {
            val userDao = database.userDao()
            // Only seed if no profile exists
            val existingProfile = userDao.getUserProfileSync()
            if (existingProfile == null) {
                userDao.insertUserProfile(getInitialUserProfile())
                userDao.insertUserStats(getInitialUserStats())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding user profile", e)
        }
    }

    private fun getInitialUserProfile(): UserProfileEntity = UserProfileEntity(
        id = 1,
        displayName = "Kairos User",
        avatarId = "avatar_default",
        bannerId = "banner_default",
        titleId = "newcomer",
        totalPoints = 0,
        currentStreak = 0,
        longestStreak = 0,
        journalEntriesCount = 0,
        wordsLearned = 0,
        futureMessagesCount = 0,
        lastActiveDate = System.currentTimeMillis(),
        joinedAt = System.currentTimeMillis()
    )

    private fun getInitialUserStats(): UserStatsEntity = UserStatsEntity(
        id = 1,
        dailyPointsEarned = 0,
        weeklyPointsEarned = 0,
        monthlyPointsEarned = 0,
        lastResetDate = System.currentTimeMillis()
    )

    // =========================================================================
    // ACHIEVEMENTS - Comprehensive achievement definitions
    // =========================================================================

    private suspend fun seedAchievements(database: KairosDatabase) {
        try {
            val userDao = database.userDao()
            userDao.insertAchievements(getInitialAchievements())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding achievements", e)
        }
    }

    private fun getInitialAchievements(): List<AchievementEntity> = listOf(
        // Streak Achievements - Consistency Category
        AchievementEntity(
            id = "streak_3",
            name = "Getting Started",
            description = "Maintain a 3-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "common",
            rewardValue = "25",
            requirement = 3
        ),
        AchievementEntity(
            id = "streak_7",
            name = "Week Warrior",
            description = "Maintain a 7-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "uncommon",
            rewardValue = "50",
            requirement = 7
        ),
        AchievementEntity(
            id = "streak_14",
            name = "Fortnight Focus",
            description = "Maintain a 14-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "rare",
            rewardValue = "100",
            requirement = 14
        ),
        AchievementEntity(
            id = "streak_30",
            name = "Month Master",
            description = "Maintain a 30-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "epic",
            rewardValue = "200",
            requirement = 30
        ),
        AchievementEntity(
            id = "streak_60",
            name = "Two Month Champion",
            description = "Maintain a 60-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "epic",
            rewardValue = "350",
            requirement = 60
        ),
        AchievementEntity(
            id = "streak_100",
            name = "Century Club",
            description = "Maintain a 100-day streak",
            iconId = "fire",
            category = "consistency",
            rarity = "legendary",
            rewardValue = "500",
            requirement = 100
        ),
        AchievementEntity(
            id = "streak_365",
            name = "Year of Dedication",
            description = "Maintain a 365-day streak",
            iconId = "crown",
            category = "consistency",
            rarity = "legendary",
            rewardValue = "2000",
            requirement = 365
        ),

        // Journal Achievements - Reflection Category
        AchievementEntity(
            id = "first_entry",
            name = "First Words",
            description = "Write your first journal entry",
            iconId = "edit",
            category = "reflection",
            rarity = "common",
            rewardValue = "20",
            requirement = 1
        ),
        AchievementEntity(
            id = "journal_10",
            name = "Regular Reflector",
            description = "Write 10 journal entries",
            iconId = "edit",
            category = "reflection",
            rarity = "uncommon",
            rewardValue = "50",
            requirement = 10
        ),
        AchievementEntity(
            id = "journal_25",
            name = "Thoughtful Writer",
            description = "Write 25 journal entries",
            iconId = "edit",
            category = "reflection",
            rarity = "rare",
            rewardValue = "100",
            requirement = 25
        ),
        AchievementEntity(
            id = "journal_50",
            name = "Prolific Journaler",
            description = "Write 50 journal entries",
            iconId = "book_open",
            category = "reflection",
            rarity = "rare",
            rewardValue = "150",
            requirement = 50
        ),
        AchievementEntity(
            id = "journal_100",
            name = "Journaling Master",
            description = "Write 100 journal entries",
            iconId = "book_open",
            category = "reflection",
            rarity = "epic",
            rewardValue = "300",
            requirement = 100
        ),
        AchievementEntity(
            id = "journal_250",
            name = "Chronicler",
            description = "Write 250 journal entries",
            iconId = "library",
            category = "reflection",
            rarity = "legendary",
            rewardValue = "600",
            requirement = 250
        ),

        // Word Count Achievements - Reflection Category
        AchievementEntity(
            id = "words_1000",
            name = "Wordsmith",
            description = "Write 1,000 words total in your journal",
            iconId = "text",
            category = "reflection",
            rarity = "uncommon",
            rewardValue = "50",
            requirement = 1000
        ),
        AchievementEntity(
            id = "words_5000",
            name = "Storyteller",
            description = "Write 5,000 words total in your journal",
            iconId = "text",
            category = "reflection",
            rarity = "rare",
            rewardValue = "150",
            requirement = 5000
        ),
        AchievementEntity(
            id = "words_10000",
            name = "Author",
            description = "Write 10,000 words total in your journal",
            iconId = "text",
            category = "reflection",
            rarity = "epic",
            rewardValue = "300",
            requirement = 10000
        ),
        AchievementEntity(
            id = "words_25000",
            name = "Novelist",
            description = "Write 25,000 words total in your journal",
            iconId = "text",
            category = "reflection",
            rarity = "legendary",
            rewardValue = "750",
            requirement = 25000
        ),

        // Vocabulary Achievements - Mastery Category
        AchievementEntity(
            id = "vocab_10",
            name = "Word Collector",
            description = "Learn 10 vocabulary words",
            iconId = "book",
            category = "mastery",
            rarity = "common",
            rewardValue = "30",
            requirement = 10
        ),
        AchievementEntity(
            id = "vocab_25",
            name = "Wordsmith Apprentice",
            description = "Learn 25 vocabulary words",
            iconId = "book",
            category = "mastery",
            rarity = "uncommon",
            rewardValue = "75",
            requirement = 25
        ),
        AchievementEntity(
            id = "vocab_50",
            name = "Linguist",
            description = "Learn 50 vocabulary words",
            iconId = "book",
            category = "mastery",
            rarity = "rare",
            rewardValue = "150",
            requirement = 50
        ),
        AchievementEntity(
            id = "vocab_100",
            name = "Lexicon Master",
            description = "Learn 100 vocabulary words",
            iconId = "brain",
            category = "mastery",
            rarity = "epic",
            rewardValue = "300",
            requirement = 100
        ),
        AchievementEntity(
            id = "vocab_200",
            name = "Dictionary",
            description = "Learn 200 vocabulary words",
            iconId = "brain",
            category = "mastery",
            rarity = "legendary",
            rewardValue = "600",
            requirement = 200
        ),

        // Level Achievements - Wisdom Category
        AchievementEntity(
            id = "level_5",
            name = "Awakening",
            description = "Reach level 5",
            iconId = "star",
            category = "wisdom",
            rarity = "common",
            rewardValue = "25",
            requirement = 5
        ),
        AchievementEntity(
            id = "level_10",
            name = "Rising Star",
            description = "Reach level 10",
            iconId = "star",
            category = "wisdom",
            rarity = "uncommon",
            rewardValue = "100",
            requirement = 10
        ),
        AchievementEntity(
            id = "level_25",
            name = "Dedicated Seeker",
            description = "Reach level 25",
            iconId = "star",
            category = "wisdom",
            rarity = "rare",
            rewardValue = "250",
            requirement = 25
        ),
        AchievementEntity(
            id = "level_50",
            name = "Elite",
            description = "Reach level 50",
            iconId = "crown",
            category = "wisdom",
            rarity = "legendary",
            rewardValue = "500",
            requirement = 50
        ),

        // Future Messages - Temporal Category
        AchievementEntity(
            id = "future_first",
            name = "Time Traveler",
            description = "Send your first message to your future self",
            iconId = "mail",
            category = "temporal",
            rarity = "common",
            rewardValue = "25",
            requirement = 1
        ),
        AchievementEntity(
            id = "future_5",
            name = "Temporal Writer",
            description = "Send 5 messages to your future self",
            iconId = "mail",
            category = "temporal",
            rarity = "uncommon",
            rewardValue = "75",
            requirement = 5
        ),
        AchievementEntity(
            id = "future_10",
            name = "Chronologist",
            description = "Send 10 messages to your future self",
            iconId = "mail",
            category = "temporal",
            rarity = "rare",
            rewardValue = "150",
            requirement = 10
        ),

        // Special Achievements - Presence Category
        AchievementEntity(
            id = "comeback",
            name = "Comeback Kid",
            description = "Return after 7+ days away",
            iconId = "refresh",
            category = "presence",
            rarity = "rare",
            rewardValue = "75",
            requirement = 1
        ),
        AchievementEntity(
            id = "night_owl",
            name = "Night Owl",
            description = "Journal after midnight 5 times",
            iconId = "moon",
            category = "presence",
            rarity = "uncommon",
            rewardValue = "50",
            requirement = 5
        ),
        AchievementEntity(
            id = "early_bird",
            name = "Early Bird",
            description = "Journal before 6 AM 5 times",
            iconId = "sun",
            category = "presence",
            rarity = "uncommon",
            rewardValue = "50",
            requirement = 5
        ),
        AchievementEntity(
            id = "weekend_warrior",
            name = "Weekend Warrior",
            description = "Journal every day of a weekend",
            iconId = "calendar",
            category = "presence",
            rarity = "common",
            rewardValue = "30",
            requirement = 2
        ),

        // XP Milestones - Wisdom Category
        AchievementEntity(
            id = "xp_1000",
            name = "Point Collector",
            description = "Earn 1,000 total XP",
            iconId = "star",
            category = "wisdom",
            rarity = "common",
            rewardValue = "50",
            requirement = 1000
        ),
        AchievementEntity(
            id = "xp_5000",
            name = "Point Hoarder",
            description = "Earn 5,000 total XP",
            iconId = "star",
            category = "wisdom",
            rarity = "uncommon",
            rewardValue = "150",
            requirement = 5000
        ),
        AchievementEntity(
            id = "xp_10000",
            name = "Point Master",
            description = "Earn 10,000 total XP",
            iconId = "star",
            category = "wisdom",
            rarity = "rare",
            rewardValue = "300",
            requirement = 10000
        ),
        AchievementEntity(
            id = "xp_50000",
            name = "Point Legend",
            description = "Earn 50,000 total XP",
            iconId = "crown",
            category = "wisdom",
            rarity = "legendary",
            rewardValue = "1000",
            requirement = 50000
        )
    )
}
