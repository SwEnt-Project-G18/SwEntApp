package com.github.se.gomeet.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.MainActivity
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Initialize Intents before each test
        Intents.init()
    }

    @After
    fun tearDown() {
        // Release Intents after each test
        Intents.release()
    }

    @Test
    fun googleSignInButtonShouldLaunchIntent() {
        // No need to set content as MainActivity already sets it
        // Directly interact with the UI elements
        composeTestRule
            .onNodeWithText("Continue with Google", useUnmergedTree = true)
            .assertExists()
            .performClick()

        // Assert that an Intent to Google Mobile Services has been sent
        intended(toPackage("com.google.android.gms"))
    }
}
