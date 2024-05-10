package com.github.se.gomeet.ui.authscreens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.model.user.GoMeetUser
import com.github.se.gomeet.ui.theme.Black
import com.github.se.gomeet.ui.theme.Cyan
import com.github.se.gomeet.ui.theme.DarkCyan
import com.github.se.gomeet.ui.theme.DarkGrey
import com.github.se.gomeet.ui.theme.DarkerCyan
import com.github.se.gomeet.ui.theme.Purple80
import com.github.se.gomeet.ui.theme.PurpleGrey40
import com.github.se.gomeet.ui.theme.TranslucentCyan
import com.github.se.gomeet.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterUsernameEmail(callback: (String, String) -> Unit,
                          userViewModel: UserViewModel,
                          textFieldColors: TextFieldColors) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(false) }
    var isValidUsername by remember { mutableStateOf(false) }
    var firstClick by remember { mutableStateOf(true) }
    var charactersExceeded by remember { mutableStateOf(false) }
    var allUsers by remember { mutableStateOf<List<GoMeetUser>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            allUsers = userViewModel.getAllUser()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround) {

        Text(
            text = "Welcome to GoMeet !\nPlease enter a username and an email.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(screenHeight/20))

        TextField(
            value = username,
            onValueChange = {
                if (it.length < 26) {
                    charactersExceeded = false
                    username = it
                } else{
                    charactersExceeded = true
                    username = it
                }
            },
            colors = textFieldColors,
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        if (charactersExceeded){
            Text(text = "Your Username Should Not Exceed 26 characters", color = Color.Red)
        }

        if (!firstClick && !isValidUsername){
            Text(text = "The Username is not valid or already taken", color = Color.Red)
        }
        Spacer(modifier = Modifier.size(16.dp))

        TextField(
            value = email,
            onValueChange = {
                    email = it
                    isValidEmail = validateEmail(email)
                },
            colors = textFieldColors,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isValidEmail && !firstClick) {
            Text("The Email is not valid or already taken", color = Color.Red)
        }

        Spacer(modifier = Modifier.size(screenHeight/15))


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
            LinearProgressIndicator(
                modifier = Modifier.padding(top = 20.dp, end = 25.dp),
                progress = { 0.2f },
                color = DarkGrey,
                trackColor = Color.LightGray,
                strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
            )
            IconButton(
                modifier = Modifier.padding(bottom = 2.5.dp, end = 3.dp).size(screenHeight/19),
                colors = IconButtonDefaults.outlinedIconButtonColors(),
                onClick = {
                    firstClick = false
                    isValidUsername = !(allUsers!!.any { u -> u.username == username }) && username.isNotBlank()
                    isValidEmail = isValidEmail && !(allUsers!!.any { u -> u.email == username })
                    if (isValidUsername && isValidEmail) {
                        callback(username, email)
                    }
                }){
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = DarkGrey,
                    modifier = Modifier.size(60.dp)
                )
            }



        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewRegisterUsername() {
    RegisterUsernameEmail (callback = { username, email ->
        println("Preview Username: $username, Preview Email: $email")
    }, userViewModel = UserViewModel(), TextFieldDefaults.colors(
        focusedTextColor = DarkCyan,
        unfocusedTextColor = DarkCyan,
        unfocusedContainerColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        cursorColor = DarkCyan,
        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
        focusedIndicatorColor = MaterialTheme.colorScheme.tertiary))
}

fun validateEmail(email: String): Boolean {
    return email.isNotEmpty() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
}