package com.github.se.gomeet.ui.mainscreens.profile

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.user.GoMeetUser
import com.github.se.gomeet.ui.mainscreens.LoadingText
import com.github.se.gomeet.ui.navigation.BottomNavigationMenu
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import kotlinx.coroutines.launch

private const val TAG = "OthersProfile"

/**
 * Composable function for the OthersProfile screen.
 *
 * @param nav The navigation actions for the OthersProfile screen.
 * @param viewedUID The user id of the user whose profile is being viewed.
 * @param userViewModel The user view model.
 * @param eventViewModel The event view model.
 */
@Composable
fun OthersProfile(
    nav: NavigationActions,
    viewedUID: String,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) { // TODO Add parameters to the function
  var isFollowing by remember { mutableStateOf(false) }
  var followerCount by remember { mutableIntStateOf(0) }
  val coroutineScope = rememberCoroutineScope()
  var isProfileLoaded by remember { mutableStateOf(false) }
  val joinedEventsList = remember { mutableListOf<Event>() }
  val myHistoryList = remember { mutableListOf<Event>() }
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  val viewedUser = remember { mutableStateOf<GoMeetUser?>(null) }
  val currentUID = userViewModel.currentUID!!

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      viewedUser.value = userViewModel.getUser(viewedUID)
      if (viewedUser.value == null) Log.e(TAG, "User $viewedUID not found")
      else Log.d(TAG, "Found user: $viewedUser")
      isFollowing = viewedUser.value?.followers?.contains(currentUID) ?: false
      followerCount = viewedUser.value?.followers?.size ?: 0

      val allEvents =
          (eventViewModel.getAllEvents() ?: emptyList()).filter { e ->
            viewedUser.value!!.joinedEvents.contains(e.eventID) && e.public
          }
      allEvents.forEach {
        if (!it.isPastEvent()) {
          joinedEventsList.add(it)
        } else {
          myHistoryList.add(it)
        }
      }
      isProfileLoaded = true
    }
  }

  Scaffold(
      modifier = Modifier.testTag("OtherProfile"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selectedTab ->
              nav.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == selectedTab })
            },
            tabList = TOP_LEVEL_DESTINATIONS,
            selectedItem = "")
      },
      topBar = {
        Row(modifier = Modifier.testTag("TopBar"), verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { nav.goBack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = MaterialTheme.colorScheme.onBackground)
          }
          Spacer(modifier = Modifier.weight(1F))
          MoreActionsButton(viewedUID, true)
        }
      }) { innerPadding ->
        if (isProfileLoaded) {
          Column(
              verticalArrangement = Arrangement.SpaceEvenly,
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState(0))) {
                Spacer(modifier = Modifier.height(screenHeight / 60))
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(start = screenWidth / 20)
                            .testTag("UserInfo")) {
                      ProfileImage(
                          userId = viewedUID,
                          modifier = Modifier.testTag("Profile Picture"),
                          size = 101.dp)
                      Column(modifier = Modifier.padding(start = screenWidth / 20)) {
                        Text(
                            "${(viewedUser.value?.firstName ?: "First")} ${(viewedUser.value?.lastName ?: "Last")}",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge)

                        Text(
                            text = "@${(viewedUser.value?.username ?: "username")}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(screenHeight / 180))
                        RatingStarWithText(rating = viewedUser.value!!.rating)
                      }
                    }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp)) {
                      // Edit Profile button
                      if (isFollowing) {
                        Button(
                            onClick = {
                              isFollowing = false
                              followerCount -= 1
                              userViewModel.unfollow(viewedUID)
                            },
                            modifier = Modifier.height(40.dp).width(180.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                              Text(text = "Unfollow", color = MaterialTheme.colorScheme.tertiary)
                            }
                      } else {
                        Button(
                            onClick = {
                              isFollowing = true
                              followerCount += 1
                              userViewModel.follow(viewedUID)
                            },
                            modifier = Modifier.height(40.dp).width(180.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                              Text(text = "Follow", color = MaterialTheme.colorScheme.tertiary)
                            }
                      }

                      Spacer(Modifier.width(5.dp))

                      Button(
                          onClick = {
                            nav.navigateToScreen(
                                Route.MESSAGE.replace("{id}", Uri.encode(viewedUID)))
                          },
                          modifier = Modifier.height(40.dp).width(180.dp),
                          shape = RoundedCornerShape(10.dp),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text(text = "Message", color = MaterialTheme.colorScheme.tertiary)
                          }
                    }

                Spacer(modifier = Modifier.height(screenHeight / 40))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                      Column(
                          modifier =
                              Modifier.clickable {
                                // TODO
                              }) {
                            Text(
                                text = joinedEventsList.size.toString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text(
                                text = "Events",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                          }
                      Column(
                          modifier =
                              Modifier.clickable {
                                nav.navigateToScreen(
                                    Route.FOLLOWERS.replace("{uid}", viewedUser.value!!.uid))
                              }) {
                            Text(
                                text = viewedUser.value?.followers?.size.toString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text(
                                text = "Followers",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                          }
                      Column(
                          modifier =
                              Modifier.clickable {
                                nav.navigateToScreen(
                                    Route.FOLLOWING.replace("{uid}", viewedUser.value!!.uid))
                              }) {
                            Text(
                                text = viewedUser.value?.following?.size.toString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text(
                                text = "Following",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally))
                          }
                    }

                Spacer(modifier = Modifier.fillMaxWidth().height(screenHeight / 50))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    contentPadding = PaddingValues(start = 15.dp, end = 15.dp)) {
                      items(viewedUser.value!!.tags.size) { index ->
                        Button(
                            onClick = {},
                            content = {
                              Text(
                                  text = viewedUser.value!!.tags[index],
                                  style = MaterialTheme.typography.labelLarge)
                            },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.outlineVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.padding(end = 8.dp))
                      }
                    }
                Spacer(modifier = Modifier.height(screenHeight / 40))
                ProfileEventsList(
                    (viewedUser.value!!.firstName) + "'s joined Events",
                    rememberLazyListState(),
                    joinedEventsList,
                    nav,
                    currentUID)
                Spacer(modifier = Modifier.height(screenHeight / 40))
                ProfileEventsList(
                    (viewedUser.value!!.firstName) + "'s History",
                    rememberLazyListState(),
                    myHistoryList,
                    nav,
                    currentUID)
              }
        } else {
          LoadingText()
        }
      }
}

/**
 * Composable function for the MoreActionsButton.
 *
 * @param uid The uid of the user whose profile is currently being viewed
 */
@Composable
fun MoreActionsButton(uid: String, isProfile: Boolean) {
  var showOptionsDialog by remember { mutableStateOf(false) }
  var showShareDialog by remember { mutableStateOf(false) }

  IconButton(onClick = { showOptionsDialog = true }) {
    Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = "More",
        modifier = Modifier.rotate(90f),
        tint = MaterialTheme.colorScheme.onBackground)
  }

  if (showOptionsDialog) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = { showOptionsDialog = false },
        title = { Text(text = "Options") },
        text = {
          Column {
            StyledTextButton(
                text = "Share Profile",
                onClick = {
                  showOptionsDialog = false
                  showShareDialog = true
                })
            StyledTextButton(
                text = "Block",
                onClick = {
                  // Handle Block logic here
                  showOptionsDialog = false
                })
          }
        },
        confirmButton = {
          StyledTextButton(text = "Cancel", onClick = { showOptionsDialog = false })
        })
  }

  if (showShareDialog) {
    Log.d("OthersProfile", uid)
    ShareDialog(type = "Profile", uid = uid, onDismiss = { showShareDialog = false })
  }
}

/**
 * Composable function for a styled text button
 *
 * @param text The text of the button
 * @param onClick The function to be called when the button is clicked
 */
@Composable
fun StyledTextButton(text: String, onClick: () -> Unit) {
  TextButton(
      onClick = onClick,
      modifier =
          Modifier.padding(top = 10.dp)
              .background(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(10.dp))
              .height(50.dp)
              .fillMaxWidth()) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }
}
