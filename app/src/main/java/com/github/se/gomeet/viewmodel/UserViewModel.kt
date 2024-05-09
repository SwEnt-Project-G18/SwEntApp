package com.github.se.gomeet.viewmodel

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.gomeet.model.repository.UserRepository
import com.github.se.gomeet.model.user.GoMeetUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the user. The viewModel is responsible for handling the logic that comes from the
 * UI and the repository.
 */
class UserViewModel : ViewModel() {
  private val currentUser = mutableStateOf<GoMeetUser?>(null)
  private val userRepository = UserRepository(Firebase.firestore)

  /**
   * Create a new user if the user is new.
   *
   * @param uid the user id
   * @param username the username
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @param phoneNumber the phone number
   * @param country the country
   */
  fun createUserIfNew(
      uid: String,
      username: String,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      country: String
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      if (getUser(uid) == null) {
        try {
          val user =
              GoMeetUser(
                  uid = uid,
                  username = username,
                  following = emptyList(),
                  followers = emptyList(),
                  pendingRequests = emptyList(),
                  firstName = firstName,
                  lastName = lastName,
                  email = email,
                  phoneNumber = phoneNumber,
                  country = country,
                  joinedEvents = emptyList(),
                  myEvents = emptyList(),
                  myFavorites = emptyList())
          currentUser.value = user
          userRepository.addUser(user)
        } catch (e: Exception) {
          Log.w(ContentValues.TAG, "Error adding user", e)
        }
      }
    }
  }

  suspend fun getFollowers(uid: String): List<GoMeetUser> {
    val followers = mutableListOf<GoMeetUser>()
    userRepository.getAllUsers { users ->
      for (user in users) {
        if (user.uid != uid && user.following.contains(uid)) {
          followers.add(user)
        }
      }
    }
    return followers
  }

  fun uploadImageAndGetUrl(
      userId: String,
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onError: (Exception) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val imageUrl = userRepository.uploadUserProfileImageAndGetUrl(userId, imageUri)
        onSuccess(imageUrl)
      } catch (e: Exception) {
        onError(e)
      }
    }
  }

  /**
   * Get the user with its id.
   *
   * @param uid the user id
   * @return the user
   */
  suspend fun getUser(uid: String): GoMeetUser? {
    return try {
      Log.d("UID IS", "User id is $uid")
      val event = CompletableDeferred<GoMeetUser?>()
      userRepository.getUser(uid) { t -> event.complete(t) }
      event.await()
    } catch (e: Exception) {
      null
    }
  }

  // TODO: fix the following method
  fun getCurrentUser(): GoMeetUser? {
    return currentUser.value
  }

  /**
   * Edit the user globally.
   *
   * @param user the user to edit
   */
  fun editUser(user: GoMeetUser) {
    userRepository.updateUser(user)
  }

  /**
   * Delete the user.
   *
   * @param uid the user id
   */
  fun deleteUser(uid: String) {
    userRepository.removeUser(uid)
  }

  /**
   * Join an event.
   *
   * @param eventId The id of the event to join.
   * @param userId The id of the user joining the event.
   */
  suspend fun joinEvent(eventId: String, userId: String) {
    try {
      val goMeetUser = getUser(userId)!!
      editUser(goMeetUser.copy(myEvents = goMeetUser.myEvents.plus(eventId)))
    } catch (e: Exception) {
      Log.w(ContentValues.TAG, "Couldn't join the event", e)
    }
  }

  /** TODO */
  suspend fun gotTicket(eventId: String, userId: String) {
    val goMeetUser = getUser(userId)!!
    editUser(goMeetUser.copy(joinedEvents = goMeetUser.joinedEvents.plus(eventId)))
  }

  /**
   * Follow a user.
   *
   * @param uid The uid of the user to follow.
   */
  fun follow(uid: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val senderUid = Firebase.auth.currentUser!!.uid
      val sender = getUser(senderUid)
      val receiver = getUser(uid)
      if (!sender!!.following.contains(uid) && !receiver!!.following.contains(senderUid)) {
        editUser(sender.copy(following = sender.following.plus(uid)))
        editUser(receiver.copy(followers = receiver.followers.plus(senderUid)))
      }
    }
  }

  /**
   * Unfollow a user.
   *
   * @param uid The uid of the user to unfollow.
   */
  fun unfollow(uid: String) {
    CoroutineScope(Dispatchers.IO).launch {
      val senderUid = Firebase.auth.currentUser!!.uid
      val sender = getUser(senderUid)
      val receiver = getUser(uid)
      editUser(sender!!.copy(following = sender.following.minus(uid)))
      editUser(receiver!!.copy(followers = receiver.followers.minus(senderUid)))
    }
  }

  /**
   * Get the username of a user.
   *
   * @param uid The uid of the user.
   * @return The username of the user.
   */
  suspend fun getUsername(uid: String): String? {
    return try {
      val user = getUser(uid)
      user?.username
    } catch (e: Exception) {
      Log.e("GetUserError", "Error retrieving user: ${e.message}")
      null
    }
  }
}
