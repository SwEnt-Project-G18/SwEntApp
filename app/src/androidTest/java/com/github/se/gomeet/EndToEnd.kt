package com.github.se.gomeet

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.screens.CreateEventScreen
import com.github.se.gomeet.screens.CreateScreen
import com.github.se.gomeet.screens.LoginScreen
import com.github.se.gomeet.screens.WelcomeScreenScreen
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This end to end test tests that a user can log in with email and password and then create an
 * event
 */
@RunWith(AndroidJUnit4::class)
class EndToEndTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @After
  fun tearDown() {
    // Clean up the event
    runBlocking { eventVM.getAllEvents()?.forEach { eventVM.removeEvent(it.uid) } }

    // Clean up the user
    Firebase.auth.currentUser?.delete()
    userVM.deleteUser(uid)
    TimeUnit.SECONDS.sleep(3)
  }

  @Test
  fun test() = run {
    ComposeScreen.onComposeScreen<WelcomeScreenScreen>(composeTestRule) {
      step("Click on the log in button") {
        logInButton {
          assertIsDisplayed()
          performClick()
        }
      }
    }

    ComposeScreen.onComposeScreen<LoginScreen>(composeTestRule) {
      step("Log in with email and password") {
        logInButton {
          assertIsDisplayed()
          assertIsNotEnabled()
        }
        emailField {
          assertIsDisplayed()
          performTextInput(email)
        }
        passwordField {
          assertIsDisplayed()
          performTextInput(pwd)
        }
        logInButton {
          assertIsEnabled()
          performClick()
          composeTestRule.waitForIdle()
          composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithTag("CreateUI").isDisplayed()
          }
        }
      }
    }

    ComposeScreen.onComposeScreen<CreateScreen>(composeTestRule) {
      step("Select which type of event to create") {
        createPublicEventButton {
          assertIsDisplayed()
          performClick()
        }
      }
    }

    composeTestRule.waitForIdle()

    ComposeScreen.onComposeScreen<CreateEventScreen>(composeTestRule) {
      step("Create an event") {
        title {
          assertIsDisplayed()
          performTextInput("Title")
        }
        description {
          assertIsDisplayed()
          performTextInput("Description")
        }
        location {
          assertIsDisplayed()
          performTextInput("test")
        }
        date {
          assertIsDisplayed()
          performTextInput(LocalDate.now().toString())
        }
        price {
          assertIsDisplayed()
          performTextInput("0.0")
        }
        link {
          assertIsDisplayed()
          performTextInput("https://example.com")
        }
        postButton {
          assertIsDisplayed()
          performClick()
        }
      }
    }
  }

  companion object {

    private const val email = "user@test.com"
    private const val pwd = "123456"
    private const val uid = "testuid"
    private const val username = "testuser"

    private lateinit var userVM: UserViewModel
    private lateinit var eventVM: EventViewModel

    @JvmStatic
    @BeforeClass
    fun setup() {
      // create a new user
      userVM = UserViewModel()
      userVM.createUserIfNew(uid, username)
      Firebase.auth.createUserWithEmailAndPassword(email, pwd)
      TimeUnit.SECONDS.sleep(2)

      eventVM = EventViewModel(uid)
    }
  }
}