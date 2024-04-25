package com.github.se.gomeet.ui.authscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.ui.theme.DarkCyan
import com.github.se.gomeet.viewmodel.AuthViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User

@Composable
fun RegisterScreen(
    client : ChatClient,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onNavToExplore: () -> Unit
) {
  val signInState = authViewModel.signInState.collectAsState()
  val isError = signInState.value.registerError != null
  val context = LocalContext.current
  val textFieldColors =
      TextFieldDefaults.colors(
          focusedTextColor = DarkCyan,
          unfocusedTextColor = DarkCyan,
          unfocusedContainerColor = Color.Transparent,
          focusedContainerColor = Color.Transparent,
          cursorColor = DarkCyan,
          focusedLabelColor = MaterialTheme.colorScheme.tertiary,
          focusedIndicatorColor = MaterialTheme.colorScheme.tertiary)

  Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize().padding(25.dp)) {
        Image(
            painter = painterResource(id = R.drawable.gomeet_text),
            contentDescription = "Go Meet",
            modifier = Modifier.padding(top = 40.dp),
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary))

        Spacer(modifier = Modifier.size(40.dp))

        Text(
            text = "Create account",
            modifier = Modifier.padding(bottom = 16.dp).testTag("register_title"),
            color = DarkCyan,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.size(110.dp))

        if (isError) {
          Text(
              text = signInState.value.registerError!!,
              modifier = Modifier.padding(bottom = 16.dp),
              color = Color.Red,
              textAlign = TextAlign.Center)
        }

        TextField(
            value = signInState.value.emailRegister,
            onValueChange = { newValue -> authViewModel.onEmailRegisterChange(newValue) },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors)

        Spacer(modifier = Modifier.size(16.dp))

        TextField(
            value = signInState.value.passwordRegister,
            onValueChange = { newValue -> authViewModel.onPasswordRegisterChange(newValue) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors)

        Spacer(modifier = Modifier.size(16.dp))

        TextField(
            value = signInState.value.confirmPasswordRegister,
            onValueChange = { newValue -> authViewModel.onConfirmPasswordRegisterChange(newValue) },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors)

        Spacer(modifier = Modifier.size(50.dp))

        Button(
            onClick = { authViewModel.signUpWithEmailPassword(context) },
            modifier = Modifier.fillMaxWidth().testTag("register_button"),
            colors =
                ButtonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    containerColor = DarkCyan,
                    disabledContentColor = Color.White,
                    contentColor = Color.White),
            enabled =
                signInState.value.emailRegister.isNotEmpty() &&
                    signInState.value.passwordRegister.isNotEmpty() &&
                    signInState.value.confirmPasswordRegister.isNotEmpty()) {
              Text("Create account")
            }

        if (signInState.value.isLoading) {
          CircularProgressIndicator()
        }

        if (signInState.value.isSignInSuccessful) {
          userViewModel.createUserIfNew(
              Firebase.auth.currentUser!!.uid,
              Firebase.auth.currentUser!!.email!!){ goMeetUser ->
              val user = User(
                  id = goMeetUser.uid,
                  name = goMeetUser.username,
                  //TODO: change image by profile picture
                  image = "https://bit.ly/2TIt8NR"
              )

              client.connectUser(
                  user = user,
                  //TODO: Generate Token, see https://getstream.io/tutorials/android-chat/
                  token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.WwfBzU1GZr0brt_fXnqKdKhz3oj0rbDUm2DqJO_SS5U"
              ).enqueue()} // TODO: currently username = email
          onNavToExplore()
        }
      }
}


