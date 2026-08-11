package com.prody.prashant.ui.navigation

/**
 * Contract metadata for every navigation destination.
 *
 * This acts as a single source of truth for route argument expectations
 * and expected back-stack behavior.
 */
data class RouteArgumentContract(
    val name: String,
    val type: String,
    val required: Boolean,
    val defaultValue: String? = null,
    val notes: String? = null
)

data class DestinationContract(
    val screen: Screen,
    val arguments: List<RouteArgumentContract> = emptyList(),
    val backStackBehavior: String
)

object ProdyNavigationContracts {
    val destinationContracts: List<DestinationContract> = listOf(
        DestinationContract(Screen.Onboarding, backStackBehavior = "Replace root with Home after completion (inclusive popUpTo)."),
        DestinationContract(Screen.Home, backStackBehavior = "Root destination for authenticated journeys."),
        DestinationContract(Screen.JournalList, backStackBehavior = "Standard push from Home; back returns to previous screen."),
        DestinationContract(Screen.JournalHistory, backStackBehavior = "Push from Journal list; back returns to Journal list."),
        DestinationContract(
            Screen.NewJournalEntry,
            backStackBehavior = "Modal-like creation flow; save/back pops one level."
        ),
        DestinationContract(
            Screen.JournalDetail,
            arguments = listOf(RouteArgumentContract("entryId", "Long", required = true)),
            backStackBehavior = "Push detail; back returns to source list/history/search."
        ),

        DestinationContract(Screen.FutureMessageList, backStackBehavior = "Push from Home and return with back."),
        DestinationContract(Screen.WriteMessage, backStackBehavior = "Creation flow; save/back pops one level."),
        DestinationContract(Screen.Stats, backStackBehavior = "Standalone push; back returns to source."),

        DestinationContract(Screen.Profile, backStackBehavior = "Push from Home or tab; serves as profile hub."),
        DestinationContract(Screen.EditProfile, backStackBehavior = "Push from profile; back/save returns to profile."),
        DestinationContract(Screen.BannerSelection, backStackBehavior = "Push from edit profile; back returns to edit profile."),
        DestinationContract(Screen.AchievementsCollection, backStackBehavior = "Push from profile; back returns to profile."),
        DestinationContract(Screen.Settings, backStackBehavior = "Push from profile; back returns to profile."),

        DestinationContract(Screen.VocabularyList, backStackBehavior = "Push from Home/missions; back returns to source."),
        DestinationContract(
            Screen.VocabularyDetail,
            arguments = listOf(RouteArgumentContract("wordId", "Long", required = true)),
            backStackBehavior = "Push detail; back returns to vocabulary/search source."
        ),

        DestinationContract(
            Screen.Quotes,
            arguments = listOf(RouteArgumentContract("tab", "String", required = false, defaultValue = "quotes")),
            backStackBehavior = "Push from Home; back returns to source while preserving selected tab."
        ),
        DestinationContract(
            Screen.IdiomDetail,
            arguments = listOf(RouteArgumentContract("idiomId", "Long", required = true)),
            backStackBehavior = "Push from quotes/home; back returns to source list."
        ),

        DestinationContract(Screen.Meditation, backStackBehavior = "Push calming session; back exits to previous screen."),
        DestinationContract(Screen.Challenges, backStackBehavior = "Push from Home; back returns to source."),
        DestinationContract(Screen.Search, backStackBehavior = "Overlay-like push; back dismisses search."),

        DestinationContract(Screen.WisdomCollection, backStackBehavior = "Push utility screen; back returns to source."),
        DestinationContract(Screen.MicroJournal, backStackBehavior = "Push utility screen; back returns to source."),
        DestinationContract(Screen.DailyRitual, backStackBehavior = "Push ritual flow; back returns to source."),
        DestinationContract(Screen.WeeklyDigest, backStackBehavior = "Push digest flow; back returns to source."),
        DestinationContract(
            Screen.FutureMessageReply,
            arguments = listOf(RouteArgumentContract("messageId", "Long", required = true)),
            backStackBehavior = "Creation-like reply flow; back/save pops one level."
        ),
        DestinationContract(
            Screen.TimeCapsuleReveal,
            arguments = listOf(RouteArgumentContract("messageId", "Long", required = true)),
            backStackBehavior = "Immersive detail flow; back pops to previous screen."
        ),

        DestinationContract(Screen.HavenHome, backStackBehavior = "Push from Home; primary Haven entry point."),
        DestinationContract(
            Screen.HavenChat,
            arguments = listOf(
                RouteArgumentContract("sessionType", "SessionType enum", required = true),
                RouteArgumentContract("sessionId", "Long", required = false, defaultValue = "null")
            ),
            backStackBehavior = "Back from active chat pops to HavenHome (safety-first)."
        ),
        DestinationContract(
            Screen.HavenExercise,
            arguments = listOf(RouteArgumentContract("exerciseType", "ExerciseType enum", required = true)),
            backStackBehavior = "Push from Haven chat/home; back returns to prior Haven context."
        ),

        DestinationContract(Screen.LearningHome, backStackBehavior = "Push learning hub; back returns to source."),
        DestinationContract(
            Screen.PathDetail,
            arguments = listOf(RouteArgumentContract("pathId", "String", required = true)),
            backStackBehavior = "Push from learning home; back returns to learning home."
        ),
        DestinationContract(
            Screen.Lesson,
            arguments = listOf(
                RouteArgumentContract("pathId", "String", required = true),
                RouteArgumentContract("lessonId", "String", required = true)
            ),
            backStackBehavior = "Push from path detail; lesson completion/back pops one level."
        ),

        DestinationContract(Screen.DeepDiveHome, backStackBehavior = "Push deep dive hub; back returns to source."),
        DestinationContract(
            Screen.DeepDiveSession,
            arguments = listOf(RouteArgumentContract("deepDiveId", "Long", required = true)),
            backStackBehavior = "Session flow; completion/back pops one level."
        ),

        DestinationContract(Screen.Missions, backStackBehavior = "Push missions dashboard; back returns to source."),

        DestinationContract(Screen.CollaborativeHome, backStackBehavior = "Push social messaging hub; back returns to source."),
        DestinationContract(
            Screen.ComposeMessage,
            arguments = listOf(
                RouteArgumentContract("contactId", "String", required = false, defaultValue = "null"),
                RouteArgumentContract("occasion", "String", required = false, defaultValue = "null")
            ),
            backStackBehavior = "Composer flow; send/back pops one level."
        ),
        DestinationContract(
            Screen.SentMessageDetail,
            arguments = listOf(RouteArgumentContract("messageId", "String", required = true)),
            backStackBehavior = "Push detail from sent tab; back returns to collaborative home."
        ),
        DestinationContract(
            Screen.ReceivedMessageDetail,
            arguments = listOf(RouteArgumentContract("messageId", "String", required = true)),
            backStackBehavior = "Push detail from inbox tab; back returns to collaborative home."
        )
    )
}
