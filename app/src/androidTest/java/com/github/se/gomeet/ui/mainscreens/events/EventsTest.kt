package com.github.se.gomeet.ui.mainscreens.events

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.github.se.gomeet.model.event.location.Location
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class EventsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eventsScreen_RenderingCorrectness() {
    // Test rendering correctness with events available
    composeTestRule.setContent {
      Events(
          currentUser = currentUserId,
          nav = NavigationActions(rememberNavController()),
          userViewModel = UserViewModel(),
          eventViewModel = EventViewModel())
    }

    composeTestRule.onNode(hasText("My events")).assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Favourites")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Favourites")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Joined Events")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Joined Events")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithText("My events")[0].assertIsDisplayed()
  }

  @Test
  fun eventsScreen_FilterButtonClick() {
    // Test button click handling
    composeTestRule.setContent {
      Events(
          currentUser = currentUserId,
          nav = NavigationActions(rememberNavController()),
          userViewModel = UserViewModel(),
          eventViewModel = EventViewModel())
    }

    composeTestRule.onNodeWithText("JoinedEvents").performClick()
    composeTestRule.onNodeWithText("Favourites").performClick()
    composeTestRule.onNodeWithText("My events").performClick()
  }

  @Test
  fun eventsScreen_AsyncBehavior() {
    // Test asynchronous behavior of fetching events
    val eventViewModel = EventViewModel()
    runBlocking(Dispatchers.IO) {
      // Add a mock event to the view model
      eventViewModel.createEvent(
          title = "Test Event",
          description = "Test description",
          location = Location(46.5190557, 6.5555216, "EPFL Campus"), // Provide a valid location
          date = LocalDate.now(), // Provide a valid date
          price = 10.0,
          url = "",
          participants = emptyList(),
          visibleToIfPrivate = emptyList(),
          maxParticipants = 0,
          public = true,
          tags = emptyList(),
          images = emptyList(),
          imageUri = null,
          userViewModel = UserViewModel(),
          uid = "")
    }

    composeTestRule.setContent {
      Events(
          currentUser = currentUserId,
          nav = NavigationActions(rememberNavController()),
          userViewModel = UserViewModel(),
          eventViewModel = EventViewModel())
    }
  }

    companion object {
        private val userViewModel = UserViewModel()
        private lateinit var currentUserId: String

        private val usr = "u@eventstest.com"
        private val pwd = "123456"

        @BeforeClass
        @JvmStatic
        fun setUp() {
            Firebase.auth.createUserWithEmailAndPassword(usr, pwd)
            TimeUnit.SECONDS.sleep(2)
            Firebase.auth.signInWithEmailAndPassword(usr, pwd)
            TimeUnit.SECONDS.sleep(2)
            // Set up the user view model
            // Order is important here, since createUserIfNew sets current user to created user (so we
            // need to create the current user last)
            currentUserId = Firebase.auth.currentUser!!.uid
            userViewModel.createUserIfNew(currentUserId, "a", "b", "c", usr, "4567", "Angola")
            TimeUnit.SECONDS.sleep(2)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            // Clean up the user view model
            Firebase.auth.currentUser!!.delete()
            userViewModel.deleteUser(currentUserId)
        }
    }
}
