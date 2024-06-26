package com.github.se.gomeet.ui.mainscreens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.mainscreens.explore.Explore
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import io.github.kakaocup.kakao.common.utilities.getResourceString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExploreTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule
  var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

  @Test
  fun testExplore() {
    composeTestRule.setContent {
      Explore(
          nav = NavigationActions(rememberNavController()),
          eventViewModel = EventViewModel("ExploreTestUser"))
    }

    // Wait for the page to load
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("Map").isDisplayed()
    }

    // Verify that the ui is correctly displayed
    composeTestRule.onNodeWithTag("Map").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(getResourceString(R.string.search_bar_placeholder))
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("CurrentLocationButton").assertIsDisplayed().performClick()
  }
}
