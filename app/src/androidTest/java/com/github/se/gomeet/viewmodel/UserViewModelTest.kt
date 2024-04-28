package com.github.se.gomeet.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.model.user.GoMeetUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserViewModelTest {
  private val uid = "testuid"
  private val username = "testuser"

  @Test
  fun test() = runTest {
    // Create user and wait for it to be created
    userViewModel.createUserIfNew(uid, username)
    TimeUnit.SECONDS.sleep(2)

    // test getUser and createUser
    var user = userViewModel.getUser(uid)

    assert(user != null)
    assert(user!!.uid == uid)
    assert(user.username == username)

    assert(userViewModel.getUser("this_user_does_not_exist") == null)

    // test editUser
    val newUsername = "newtestuser"
    val newUser = GoMeetUser(user.uid, newUsername, emptyList(), emptyList(), emptyList())

    userViewModel.editUser(newUser)
    user = userViewModel.getUser(uid)

    assert(user != null)
    assert(user!!.uid == uid)
    assert(user.username == newUsername)

    // test deleteUser
    userViewModel.deleteUser(uid)
    user = userViewModel.getUser(uid)
    assert(user == null)
  }

  companion object{

    private lateinit var userViewModel: UserViewModel
    @BeforeClass
    @JvmStatic
    fun setup() {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      userViewModel = UserViewModel()
    }
  }


}
