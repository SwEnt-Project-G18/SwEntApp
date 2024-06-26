package com.github.se.gomeet.ui.mainscreens.create

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.navigation.NavigationActions
import io.github.kakaocup.kakao.common.utilities.getResourceString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun testCreate() {
    composeTestRule.setContent { Create(NavigationActions(rememberNavController())) }

    composeTestRule.waitForIdle()

    // Check that the text "Choose your audience" is displayed
    composeTestRule.onNodeWithText(getResourceString(R.string.create_text)).assertIsDisplayed()

    // Check that the "Public" and "Private" buttons are displayed
    composeTestRule
        .onNodeWithText(getResourceString(R.string.create_public_event_button))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText(getResourceString(R.string.create_private_event_button))
        .assertIsDisplayed()
  }
}
