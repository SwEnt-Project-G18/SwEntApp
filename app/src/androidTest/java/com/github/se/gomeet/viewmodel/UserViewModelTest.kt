package com.github.se.gomeet.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.model.repository.UserRepository
import com.github.se.gomeet.model.user.GoMeetUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserViewModelTest {
  companion object {
    private const val uid = "UserViewModelTestUser"
    private const val username = "userviewmodel"
    private const val firstname = "testfirstname"
    private const val lastname = "testlastname"
    private const val email = "testemail"
    private const val phonenumber = "testphonenumber"
    private const val country = "testcountry"

    private val userVM = UserViewModel(UserRepository(Firebase.firestore))
    private lateinit var user: GoMeetUser

    @BeforeClass
    @JvmStatic
    fun setup() {
      // Assumes that getUser works...
      runBlocking {
        userVM.createUserIfNew(uid, username, firstname, lastname, email, phonenumber, country)
        while (userVM.getUser(uid) == null) {
          TimeUnit.SECONDS.sleep(1)
        }
        user = userVM.getUser(uid)!!
      }
    }

    @AfterClass
    @JvmStatic
    fun tearDown() {
      runBlocking { userVM.deleteUser(uid) }
      runBlocking { assert(userVM.getUser(uid) == null) }
    }
  }

  @Test
  fun getFollowersTest() {
    val otherUid = "AnotherUser1"

    // Add another user to the following list
    userVM.editUser(user.copy(following = listOf(otherUid)))

    // Get the other user's followers from the view model
    var followers: List<GoMeetUser>
    runBlocking { followers = userVM.getFollowers(otherUid) }

    // Make sure that it contains the main user
    while (!followers.any { it.uid == uid }) {
      TimeUnit.SECONDS.sleep(1)
    }
    assert(followers.any { it.uid == uid })
  }

  @Test
  fun getAllUsersTest() {
    // Get all the users from the view model
    var allUsers: List<GoMeetUser>?
    runBlocking { allUsers = userVM.getAllUsers() }

    // There should only be one user
    assert(allUsers != null)
    assert(allUsers!!.size == 1)
    assert(allUsers!![0].equals(user))
  }

  @Test
  fun joinEventTest() {
    val eventId = "event1"

    // Join an event
    runBlocking { userVM.joinEvent(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Verify that the user's joinedEvents list was correctly updated
    assert(user.joinedEvents.contains(eventId))
  }

  @Test
  fun userCreatesEventTest() {
    val eventId = "event2"

    // Create an event
    runBlocking { userVM.userCreatesEvent(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Verify that the user's myEvents list was correctly updated
    assert(user.myEvents.contains(eventId))
  }

  @Test
  fun gotInvitationTest() {
    val eventId = "event3"

    // Invite the user to the event
    runBlocking { userVM.gotInvitation(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Verify that the user's pendingRequests was correctly updated
    assert(user.pendingRequests.any { it.eventId == eventId })

    // Make sure that the user can't be invited twice to the same event
    runBlocking { userVM.gotInvitation(eventId, uid) }
    runBlocking { user = userVM.getUser(uid)!! }
    assert(user.pendingRequests.count { it.eventId == eventId } == 1)
  }

  @Test
  fun gotKickedFromEvent() {
    val eventId = "event4"

    // Join the event
    runBlocking { userVM.joinEvent(eventId, uid) }

    // Kick the user from the event
    runBlocking { userVM.gotKickedFromEvent(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Make sure that the event is no longer in the user's joinedEvents list
    assert(!user.joinedEvents.any { it == eventId })
  }

  @Test
  fun invitationCanceledTest() {
    val eventId = "event5"

    // Invite the user to the event
    runBlocking { userVM.gotInvitation(eventId, uid) }

    // Cancel the invite
    runBlocking { userVM.invitationCanceled(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Make sure that the user is no longer invited to the event
    assert(!user.pendingRequests.any { it.eventId == eventId })
  }

  @Test
  fun userAcceptsInvitationTest() {
    val eventId = "event6"

    // Invite the user to the event
    runBlocking { userVM.gotInvitation(eventId, uid) }

    // Make the user accept the invitation
    runBlocking { userVM.userAcceptsInvitation(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Verify that the event appears in the user's joinedEvents list
    assert(user.joinedEvents.contains(eventId))
  }

  @Test
  fun userRefusesInvitationTest() {
    val eventId = "event7"

    // Invite the user to the event
    runBlocking { userVM.gotInvitation(eventId, uid) }

    // Make the user refuse the invitation
    runBlocking { userVM.userRefusesInvitation(eventId, uid) }

    // Update the user variable
    runBlocking { user = userVM.getUser(uid)!! }

    // Verify that the invitation is no longer in pendingRequests
    assert(!user.pendingRequests.any { it.eventId == eventId })

    // Verify that the event doesn't appear in the user's joinedEvents list
    assert(!user.joinedEvents.contains(eventId))
  }

  @Test
  fun getUsernameTest() {
    var usrname: String?

    runBlocking { usrname = userVM.getUsername(uid) }

    assert(usrname != null)
    assert(usrname == username)
  }
}
