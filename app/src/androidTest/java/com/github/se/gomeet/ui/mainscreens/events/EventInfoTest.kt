package com.github.se.gomeet.ui.mainscreens.events

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.model.repository.EventRepository
import com.github.se.gomeet.model.repository.UserRepository
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventInfoTest {
  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    private val eventTitle = "Event Title"
    private val eventId = "eventid"
    private val eventDate = "2024-05-01"
    private val organiserId = "organiserid"
    private val eventTime = "23:40"
    private val eventDescription = "Event Description"
    private val eventLocation = LatLng(0.0, 0.0)
    private val userVM = UserViewModel(UserRepository(Firebase.firestore))
    private val eventVM = EventViewModel(organiserId, EventRepository(Firebase.firestore))
    private val eventRating = 4.5
    private lateinit var uid: String

    private val usr = "eventinfo@test.com"
    private val pwd = "123456"

    @BeforeClass
    @JvmStatic
    fun setUp() {
      runBlocking {
        // Create a new user and sign in
        var result = Firebase.auth.createUserWithEmailAndPassword(usr, pwd)
        while (!result.isComplete) {
          TimeUnit.SECONDS.sleep(1)
        }
        result = Firebase.auth.signInWithEmailAndPassword(usr, pwd)
        while (!result.isComplete) {
          TimeUnit.SECONDS.sleep(1)
        }

        // Add the user to the view model and add a second user who created the event
        uid = Firebase.auth.currentUser!!.uid
        userVM.createUserIfNew(
            organiserId, "testorganiser", "test", "name", "test@email.com", "0123", "Afghanistan")
        while (userVM.getUser(organiserId) == null) {
          TimeUnit.SECONDS.sleep(1)
        }
        userVM.createUserIfNew(uid, "a", "b", "c", usr, "4567", "Angola")
        while (userVM.getUser(uid) == null) {
          TimeUnit.SECONDS.sleep(1)
        }
      }
    }

    @AfterClass
    @JvmStatic
    fun tearDown() {
      runBlocking {
        // Clean up the users
        Firebase.auth.currentUser!!.delete()
        userVM.deleteUser(organiserId)
        userVM.deleteUser(uid)
      }
    }
  }

  @Test
  fun testEventInfo() {
    composeTestRule.setContent {
      MyEventInfo(
          nav = NavigationActions(rememberNavController()),
          title = eventTitle,
          eventId = eventId,
          date = eventDate,
          time = eventTime,
          organizerId = organiserId,
          rating = eventRating,
          description = eventDescription,
          loc = eventLocation,
          userViewModel = userVM,
          eventViewModel = eventVM)
    }

    composeTestRule.waitForIdle()

    // Wait until the page is loaded
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("EventHeader").isDisplayed()
    }

    // Test the ui of the EventInfo screen
    composeTestRule.onNodeWithTag("TopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EventHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EventImage").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("EventDescription")
        .assertTextContains(eventDescription)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag("EventButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MapView").assertIsDisplayed()
  }
}
