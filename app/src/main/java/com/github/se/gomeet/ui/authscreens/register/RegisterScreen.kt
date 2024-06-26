package com.github.se.gomeet.ui.authscreens.register

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.mainscreens.LoadingText
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.AuthViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "RegisterScreen"

/**
 * This composable function represents the Register Screen, where users can sign up for a new
 * account. It navigates through multiple registration steps, each handling different parts of the
 * registration process, including entering username, email, password, personal details, and
 * uploading a profile picture.
 *
 * @param nav Navigation actions for handling back navigation and successful registration
 *   completion.
 * @param authViewModel ViewModel to manage and observe authentication related data.
 * @param userViewModel ViewModel to manage user related data and operations.
 * @param onNavToExplore Function to execute once registration completes successfully and user
 *   navigates to Explore Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    nav: NavigationActions,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onNavToExplore: () -> Unit
) {
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  val signInState = authViewModel.signInState.collectAsState()
  val textFieldColors =
      TextFieldDefaults.colors(
          focusedTextColor = MaterialTheme.colorScheme.onBackground,
          unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
          unfocusedContainerColor = Color.Transparent,
          focusedContainerColor = Color.Transparent,
          cursorColor = MaterialTheme.colorScheme.outlineVariant,
          focusedLabelColor = MaterialTheme.colorScheme.tertiary,
          unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,
          focusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
          unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary)

  var state by remember { mutableIntStateOf(1) }

  Column(modifier = Modifier.fillMaxSize().testTag("RegisterScreen")) {
    TopAppBar(
        modifier = Modifier.testTag("TopBar"),
        colors =
            TopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground),
        title = {},
        navigationIcon = {
          IconButton(
              onClick = {
                if (state == 1) {
                  nav.goBack()
                } else {
                  state -= 1
                }
              }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground)
              }
        })

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(25.dp).verticalScroll(ScrollState(0))) {
          Spacer(modifier = Modifier.height(screenHeight / 10))
          Image(
              painter = painterResource(id = R.drawable.gomeet_text),
              contentDescription = "GoMeet",
              modifier = Modifier.padding(top = 0.dp).width(200.dp),
              alignment = Alignment.Center,
              colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary))

          Spacer(modifier = Modifier.height(screenHeight / 10))

          fun nextState() {
            state += 1 // Simplified state increment
          }

          when (state) {
            1 ->
                RegisterUsernameEmail(
                    callback = { username, email ->
                      authViewModel.onUsernameRegisterChange(username)
                      authViewModel.onEmailRegisterChange(email)
                      nextState()
                    },
                    userViewModel = userViewModel,
                    textFieldColors = textFieldColors)
            2 ->
                RegisterPassword(
                    callback = { password ->
                      authViewModel.onPasswordRegisterChange(password)
                      nextState()
                    },
                    textFieldColors = textFieldColors)
            3 ->
                RegisterNameCountryPhone(
                    callback = { firstname, lastname, country, phone ->
                      authViewModel.onFirstNameRegisterChange(firstname)
                      authViewModel.onLastNameRegisterChange(lastname)
                      authViewModel.onCountryRegisterChange(country)
                      authViewModel.onPhoneNumberRegisterChange(phone)
                      nextState()
                    },
                    textFieldColors = textFieldColors)
            4 ->
                RegisterPfp(
                    callback = { pfp ->
                      authViewModel.onPfpRegisterChange(pfp)
                      nextState()
                    },
                    signInState.value.firstNameRegister)
            5 -> {
              authViewModel.signUpWithEmailPassword(LocalContext.current)
              nextState()
            }
            6 -> {}
          }
          if (signInState.value.isLoading) {
            LoadingText()
          }

          if (signInState.value.isSignInSuccessful) {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
              val uid = currentUser.uid
              val email = signInState.value.emailRegister
              val firstName = signInState.value.firstNameRegister
              val lastName = signInState.value.lastNameRegister
              val phoneNumber = signInState.value.phoneNumberRegister
              val country = signInState.value.countryRegister
              val username = signInState.value.usernameRegister
              val pfp = signInState.value.pfp
              if (pfp != null) {
                userViewModel.uploadImageAndGetUrl(
                    userId = uid,
                    imageUri = pfp,
                    onSuccess = { imageUrl ->
                      userViewModel.createUserIfNew(
                          uid, username, firstName, lastName, email, phoneNumber, country, imageUrl)
                    },
                    onError = { exception -> Log.e(TAG, "Failed to upload new image", exception) })
              } else {
                userViewModel.createUserIfNew(
                    uid, username, firstName, lastName, email, phoneNumber, country, "")
              }
            }
            onNavToExplore()
          }
        }
  }
}
