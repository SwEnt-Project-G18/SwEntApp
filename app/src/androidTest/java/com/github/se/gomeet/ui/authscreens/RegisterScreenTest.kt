package com.github.se.gomeet.ui.authscreens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.MainActivity
import com.github.se.gomeet.R
import com.github.se.gomeet.screens.RegisterScreenScreen
import com.github.se.gomeet.screens.WelcomeScreenScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.kakao.common.utilities.getResourceString
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest : TestCase() {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testRegisterScreen() = run {
    val email = "registerscreen@test.com"

    ComposeScreen.onComposeScreen<WelcomeScreenScreen>(composeTestRule) {
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText(getResourceString(R.string.sign_up_button)).performClick()
    }

    ComposeScreen.onComposeScreen<RegisterScreenScreen>(composeTestRule) {
      composeTestRule.waitForIdle()

      // Test the ui of RegisterUsernameEmail and fill in the fields
      composeTestRule.onNodeWithContentDescription("GoMeet").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Text").assertIsDisplayed()
      composeTestRule
          .onNodeWithText(getResourceString(R.string.username_text_field))
          .assertIsDisplayed()
          .performTextInput("RegisterScreenTestUser")
      composeTestRule
          .onNodeWithText(getResourceString(R.string.email_text_field))
          .assertIsDisplayed()
          .performTextInput(email)
      composeTestRule.onNodeWithTag("BottomRow").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Next").assertIsDisplayed().performClick()

      composeTestRule.waitForIdle()

      // Test the ui of RegisterPassword and fill in the fields
      composeTestRule.onNodeWithContentDescription("GoMeet").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Text").assertIsDisplayed()
      composeTestRule
          .onNodeWithText(getResourceString(R.string.password_text_field))
          .assertIsDisplayed()
          .performTextInput("123456")
      composeTestRule
          .onNodeWithText(getResourceString(R.string.confirm_password_text_field))
          .assertIsDisplayed()
          .performTextInput("123456")
      composeTestRule.onNodeWithTag("BottomRow").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Next").assertIsDisplayed().performClick()

      composeTestRule.waitForIdle()

      // Test the ui of RegisterNameCountryPhone and fill in the fields
      composeTestRule.onNodeWithContentDescription("GoMeet").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Text").assertIsDisplayed()
      composeTestRule
          .onNodeWithText(getResourceString(R.string.first_name_text_field))
          .assertIsDisplayed()
          .performTextInput("firstname")
      composeTestRule
          .onNodeWithText(getResourceString(R.string.last_name_text_field))
          .assertIsDisplayed()
          .performTextInput("lastname")
      composeTestRule
          .onNodeWithText(getResourceString(R.string.phone_number_text_field))
          .assertIsDisplayed()
          .performTextInput("+1234567890")
      composeTestRule.onNodeWithTag("Country").performScrollTo().assertIsDisplayed().performClick()
      composeTestRule.onAllNodesWithTag("CountryItem")[1].assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("BottomRow").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Next").assertIsDisplayed().performClick()

      composeTestRule.waitForIdle()

      // Test the ui of RegisterPfp
      composeTestRule.onNodeWithContentDescription("GoMeet").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Text").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Profile Picture").assertIsDisplayed()
      composeTestRule.onNodeWithTag("BottomRow").assertIsDisplayed()
      composeTestRule.onNodeWithContentDescription("Next").assertIsDisplayed().performClick()

      // Verify that the user was created
      composeTestRule.waitUntil(timeoutMillis = 10000) { Firebase.auth.currentUser != null }
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      // Clean up the user
      Firebase.auth.currentUser?.delete()
    }
  }
}
