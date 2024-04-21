package com.github.se.gomeet.ui.mainscreens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventsTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val maxEventsDisplayedOnScreen = 3 // small number so the test passes on the CI

  @Test
  fun uiElementsDisplayed() {
    lateinit var navController: NavHostController

    rule.setContent {
      navController = rememberNavController()
      Events(NavigationActions(navController), eventViewModel = EventViewModel())
    }

    rule.onAllNodesWithTag("Card").apply {
      fetchSemanticsNodes().forEachIndexed { i, _ ->
        if (i < maxEventsDisplayedOnScreen) get(i).assertIsDisplayed() else get(i).assertExists()
      }
    }
    rule.onAllNodesWithTag("EventName").apply {
      fetchSemanticsNodes().forEachIndexed { i, _ ->
        if (i < maxEventsDisplayedOnScreen) get(i).assertIsDisplayed() else get(i).assertExists()
      }
    }
    rule.onAllNodesWithTag("UserName").apply {
      fetchSemanticsNodes().forEachIndexed { i, _ ->
        if (i < maxEventsDisplayedOnScreen) get(i).assertIsDisplayed() else get(i).assertExists()
      }
    }
    rule.onAllNodesWithTag("EventDate").apply {
      fetchSemanticsNodes().forEachIndexed { i, _ ->
        if (i < maxEventsDisplayedOnScreen) get(i).assertIsDisplayed() else get(i).assertExists()
      }
    }
    rule.onAllNodesWithTag("EventPicture").apply {
      fetchSemanticsNodes().forEachIndexed { i, _ ->
        if (i < maxEventsDisplayedOnScreen) get(i).assertIsDisplayed() else get(i).assertExists()
      }
    }
  }
}
