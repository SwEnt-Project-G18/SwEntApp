package com.github.se.gomeet.ui.mainscreens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import io.github.kakaocup.kakao.common.utilities.getResourceString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrendsTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun testTrends() {
    val uid = "TrendsTestUser"

    composeTestRule.setContent {
      Trends(
          nav = NavigationActions(rememberNavController()),
          userViewModel = UserViewModel(uid),
          eventViewModel = EventViewModel(uid))
    }

    // Wait for the page to load
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithText(getResourceString(R.string.sort)).isDisplayed()
    }

    // Verify that the ui is correctly displayed
    composeTestRule
        .onNodeWithText(getResourceString(R.string.sort))
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithText(getResourceString(R.string.tag_sort))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithText(getResourceString(R.string.sort)).performClick()
    composeTestRule
        .onNodeWithText(getResourceString(R.string.name))
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithText(getResourceString(R.string.sort)).performClick()
    composeTestRule
        .onNodeWithText(getResourceString(R.string.date))
        .assertIsDisplayed()
        .performClick()
  }
}
