package com.github.se.gomeet.ui

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

  private val emailDaniel = "iamdanielsspam@gmail.com"
  private val pwdDaniel = "123456"

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  @SuppressLint("StateFlowValueCalledInComposition")
  @Test fun testLoginScreen() {
    val authViewModel = AuthViewModel()

    lateinit var nav: NavHostController

    rule.setContent {
      nav = rememberNavController()
      LoginScreen(authViewModel) {
        NavigationActions(nav).navigateTo(TOP_LEVEL_DESTINATIONS[1])
      }
    }

    // Test the UI elements
    rule.onNodeWithContentDescription("GoMeet").assertIsDisplayed()
    rule.onNodeWithText("Login").assertIsDisplayed()

    rule.onNodeWithText("Email").assertIsDisplayed()
    rule.onNodeWithText("Password").assertIsDisplayed()
    rule.onNodeWithText("Log in")
      .assertIsNotEnabled().assertHasClickAction().assertIsDisplayed()

    // Enter email and password
    rule.onNodeWithText("Email").performTextInput(emailDaniel)
    rule.onNodeWithText("Password").performTextInput(pwdDaniel)

    // Wait for the Compose framework to recompose the UI
    rule.waitForIdle()

    // Click on the "Log in" button
    rule.onNodeWithText("Log in").assertIsEnabled().assertHasClickAction()
    rule.onNodeWithText("Log in").performClick()

    if (authViewModel.signInState.value.signInError != null) {
      rule.onNodeWithText(authViewModel.signInState.value.signInError!!).assertIsDisplayed()
    } else {
      rule.waitForIdle()
      assert(nav.currentDestination?.route == Route.TRENDS)
    }

  }
}
