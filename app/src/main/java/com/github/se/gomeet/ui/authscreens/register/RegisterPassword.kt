package com.github.se.gomeet.ui.authscreens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * This composable function allows the user to input and confirm their password. It checks that the
 * passwords match and meet minimum security requirements before proceeding.
 *
 * @param callback Function to be called with the password when validation passes.
 * @param textFieldColors Custom colors for the TextField components used in this Composable.
 */
@Composable
fun RegisterPassword(callback: (String) -> Unit, textFieldColors: TextFieldColors) {
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var firstClick by remember { mutableStateOf(true) }
  var passwordsMatch by remember { mutableStateOf(false) }
  var lengthValid by remember { mutableStateOf(true) }
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround) {
        Text(
            text = "Please enter your password.",
            modifier = Modifier.fillMaxWidth().testTag("Text"),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.size(screenHeight / 20))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.size(screenHeight / 60))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth())

        if (!lengthValid && !firstClick) {
          Text(text = "Your Password should be at least 6 characters", color = Color.Red)
        }

        if (!passwordsMatch && !firstClick) {
          Text(text = "Your Passwords should match", color = Color.Red)
        }

        Spacer(modifier = Modifier.size(screenHeight / 15))

        Row(
            modifier = Modifier.fillMaxWidth().testTag("BottomRow"),
            horizontalArrangement = Arrangement.End) {
              LinearProgressIndicator(
                  modifier = Modifier.padding(top = 20.dp, end = 25.dp),
                  progress = { 0.4f },
                  color = MaterialTheme.colorScheme.tertiary,
                  trackColor = Color.LightGray,
                  strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap)
              IconButton(
                  modifier = Modifier.padding(bottom = 2.5.dp, end = 3.dp).size(screenHeight / 19),
                  colors = IconButtonDefaults.outlinedIconButtonColors(),
                  onClick = {
                    firstClick = false
                    passwordsMatch = password == confirmPassword
                    lengthValid = password.length >= 6
                    if (lengthValid && passwordsMatch) {
                      callback(password)
                    }
                  }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(60.dp))
                  }
            }
      }
}
