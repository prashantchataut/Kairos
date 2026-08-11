package com.prody.prashant.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.prody.prashant.ui.components.ProdyNotificationBanner
import com.prody.prashant.ui.components.ProdyNotificationType
import com.prody.prashant.ui.components.quietmode.QuietModeSuggestionDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PrimaryJourneysComposeUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun notificationBanner_primaryJourneyAction_isClickable() {
        var actionTriggered = false

        composeRule.setContent {
            ProdyNotificationBanner(
                type = ProdyNotificationType.SUCCESS,
                title = "Sync completed",
                message = "All your entries are up to date.",
                isVisible = true,
                onDismiss = {},
                actionLabel = "Review",
                onActionClick = { actionTriggered = true }
            )
        }

        composeRule.onNodeWithText("Sync completed").assertIsDisplayed()
        composeRule.onNodeWithText("Review").performClick()

        assertTrue(actionTriggered)
    }

    @Test
    fun notificationBanner_errorPathDismiss_invokesCallback() {
        var dismissed = false

        composeRule.setContent {
            ProdyNotificationBanner(
                type = ProdyNotificationType.GENTLE_REMINDER,
                title = "Connection issue",
                message = "We will retry in the background.",
                isVisible = true,
                onDismiss = { dismissed = true }
            )
        }

        composeRule.onNodeWithText("Connection issue").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Dismiss notification").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun quietModeSuggestion_destructivePathDismiss_invokesCallback() {
        var dismissed = false

        composeRule.setContent {
            QuietModeSuggestionDialog(
                onAccept = {},
                onDismiss = { dismissed = true },
                analysisReason = "elevated_stress"
            )
        }

        composeRule.onNodeWithText("I'm okay, thanks").performClick()
        assertTrue(dismissed)
    }
}
