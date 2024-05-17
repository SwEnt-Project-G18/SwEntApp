package com.github.se.gomeet.ui.mainscreens.events

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.github.se.gomeet.R
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.user.GoMeetUser
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

/**
 * EventHeader is a composable that displays the header of an event.
 *
 * @param title Title of the event
 * @param organizer Organizer of the event
 * @param rating Rating of the event
 * @param nav NavigationActions object to handle navigation
 * @param date Date of the event
 * @param time Time of the event
 */
@Composable
fun EventHeader(
    title: String,
    currentUser: GoMeetUser,
    organizer: GoMeetUser,
    rating: Double, // TODO: Implement rating system
    nav: NavigationActions,
    date: String,
    time: String
) {
  Row(
      modifier = Modifier.fillMaxWidth().testTag("EventHeader"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
          Text(
              text = title,
              style =
                  TextStyle(
                      fontSize = 24.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.tertiary,
                      letterSpacing = 0.5.sp))
          Spacer(modifier = Modifier.height(5.dp))
          Text(
              modifier =
                  Modifier.clickable {
                        if (organizer.uid == currentUser.uid) {
                          nav.navigateToScreen(Route.PROFILE)
                        } else {
                          nav.navigateToScreen(Route.OTHERS_PROFILE.replace("{uid}", organizer.uid))
                        }
                      }
                      .testTag("Username"),
              text = organizer.username,
              style =
                  TextStyle(
                      fontSize = 16.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color.Gray,
                      fontFamily = FontFamily(Font(R.font.roboto)),
                      letterSpacing = 0.5.sp))
          // Add other details like rating here
        }
        // Icon for settings or more options, assuming using Material Icons
        EventDateTime(day = date, time = time)
      }
}

/**
 * EventDateTime is a composable that displays the date and time of an event.
 *
 * @param day Day of the event
 * @param time Time of the event
 */
@Composable
fun EventDateTime(day: String, time: String) {
  Log.d(day, "This is the day")
  Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 15.dp)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          text = day,
          style =
              TextStyle(
                  fontSize = 24.sp,
                  color = MaterialTheme.colorScheme.onBackground,
                  fontWeight = FontWeight.SemiBold,
                  textAlign = TextAlign.Center))
      Text(
          text = time,
          style =
              TextStyle(
                  fontSize = 16.sp,
                  color = MaterialTheme.colorScheme.onBackground,
                  textAlign = TextAlign.Center))
    }
  }
}

/**
 * EventImage is a composable that displays the image of an event.
 *
 * @param imageUrl URL of the image
 */
@Composable
fun EventImage(imageUrl: String?) {
  val defaultImagePainter = painterResource(id = R.drawable.gomeet_logo)
  val imagePainter =
      if (imageUrl != null) {
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = imageUrl)
                .apply(
                    block =
                        fun ImageRequest.Builder.() {
                          placeholder(R.drawable.gomeet_logo)
                        })
                .build())
      } else defaultImagePainter

  Column(modifier = Modifier.fillMaxWidth().testTag("EventImage")) {
    Image(
        painter = imagePainter,
        contentDescription = "Event Image",
        contentScale = ContentScale.Crop,
        modifier = Modifier.aspectRatio(3f / 1.75f).clip(RoundedCornerShape(20.dp)))
  }
}

/**
 * EventDescription is a composable that displays the description of an event.
 *
 * @param text Description of the event
 */
@Composable
fun EventDescription(text: String) {
  Text(
      text = text,
      style =
          TextStyle(
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onBackground,
              fontFamily = FontFamily(Font(R.font.roboto)),
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.5.sp),
      modifier = Modifier.testTag("EventDescription"))
}

/**
 * Renders actionable buttons for an event. This includes the ability to join or leave an event,
 * edit an event (if the current user is the organizer), and navigate to messaging with the
 * organizer. Favorite toggling is also managed here.
 *
 * @param currentUser The currently logged-in user, as a GoMeetUser object.
 * @param organiser The organizer of the event, also a GoMeetUser object.
 * @param eventId The unique identifier of the event.
 * @param userViewModel An instance of UserViewModel for performing operations like editing user
 *   details.
 * @param nav An instance of NavigationActions for handling navigation events.
 */
@Composable
fun EventButtons(
    currentUser: GoMeetUser,
    organiser: GoMeetUser,
    eventId: String,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    nav: NavigationActions
) {
  val coroutineScope = rememberCoroutineScope()
  val isFavourite = remember { mutableStateOf(currentUser.myFavorites.contains(eventId)) }
  val currentEvent = remember { mutableStateOf<Event?>(null) }
  val isJoined = remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      currentEvent.value = eventViewModel.getEvent(eventId)
      isJoined.value =
          currentUser.joinedEvents.contains(eventId) &&
              currentEvent.value!!.participants.contains(currentUser.uid)
    }
  }

  Row(
      modifier = Modifier.fillMaxWidth().testTag("EventButton"),
      horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(
            onClick = {
              eventAction(
                  currentUser,
                  organiser,
                  eventId,
                  userViewModel,
                  eventViewModel,
                  isJoined,
                  currentEvent.value!!)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary)) {
              if (organiser.uid == currentUser.uid) {
                Text("Edit My Event")
              } else {
                if (isJoined.value) {
                  Text("Leave Event")
                } else {
                  Text("Join Event")
                }
              }
            }
        if (organiser.uid == currentUser.uid) {
          Spacer(modifier = Modifier.width(5.dp))
          TextButton(
              onClick = {
                nav.navigateToScreen(Route.MANAGE_INVITES.replace("{eventId}", eventId))
              },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.weight(1f),
              colors =
                  ButtonDefaults.textButtonColors(
                      containerColor = MaterialTheme.colorScheme.primaryContainer,
                      contentColor = MaterialTheme.colorScheme.tertiary)) {
                Text("Add Participants")
              }
        } else {
          IconButton(
              onClick = {
                nav.navigateToScreen(Route.MESSAGE.replace("{id}", Uri.encode(organiser.uid)))
              }) {
                Icon(
                    imageVector =
                        ImageVector.vectorResource(id = R.drawable.baseline_chat_bubble_outline_24),
                    contentDescription = "Chat",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground)
              }
        }
        IconButton(
            onClick = { favouriteAction(currentUser, eventId, userViewModel, isFavourite) }) {
              FavouriteButton(isFavourite.value)
            }
      }
}

/**
 * MapViewComposable is a composable that displays the map view of an event.
 *
 * @param loc Location of the event
 * @param zoomLevel Zoom level of the map
 */
@Composable
fun MapViewComposable(
    loc: LatLng,
    zoomLevel: Float = 15f // Default zoom level for close-up of location
) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(loc, zoomLevel)
  }

  val markerState = rememberMarkerState(position = loc)

  // Set up the GoogleMap composable
  GoogleMap(
      modifier =
          Modifier.testTag("MapView").fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)),
      cameraPositionState = cameraPositionState) {
        Marker(
            state = markerState,
            title = "Marker in Location",
            snippet = "This is the selected location")
      }

  // Initialize the map position once and avoid resetting on recomposition
  DisposableEffect(loc) {
    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel))
    onDispose {}
  }

  DisposableEffect(loc) {
    markerState.position = loc
    onDispose {}
  }
}

/**
 * The action to perform when clicking the favourite button.
 *
 * @param currentUser Current user
 * @param eventId Event ID
 * @param userViewModel UserViewModel
 * @param isFavourite Whether the event is a favourite
 */
private fun favouriteAction(
    currentUser: GoMeetUser,
    eventId: String,
    userViewModel: UserViewModel,
    isFavourite: MutableState<Boolean>
) {
  if (!isFavourite.value) {
    currentUser.myFavorites = currentUser.myFavorites.plus(eventId)
  } else {
    currentUser.myFavorites = currentUser.myFavorites.minus(eventId)
  }
  userViewModel.editUser(currentUser)
  isFavourite.value = !isFavourite.value
}

/**
 * Favourite button composable.
 *
 * @param isFavourite Whether the event is a favourite.
 */
@Composable
private fun FavouriteButton(isFavourite: Boolean) {
  if (!isFavourite) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.heart),
        contentDescription = "Add to Favorites",
        modifier = Modifier.size(30.dp),
        tint = MaterialTheme.colorScheme.outlineVariant)
  } else {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.redheart),
        contentDescription = "Remove from favorites",
        modifier = Modifier.size(30.dp),
        tint = MaterialTheme.colorScheme.outlineVariant)
  }
}

private fun eventAction(
    currentUser: GoMeetUser,
    organiser: GoMeetUser,
    eventId: String,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    isJoined: MutableState<Boolean>,
    currentEvent: Event
) {

  if (organiser.uid == currentUser.uid) {
    // TODO: GO TO EDIT EVENT PARAMETERS SCREEN
  } else {

    if (!isJoined.value) {
      currentUser.joinedEvents = currentUser.joinedEvents.plus(eventId)
      currentEvent.participants = currentEvent.participants.plus(currentUser.uid)
    } else {
      currentUser.joinedEvents = currentUser.joinedEvents.minus(eventId)
      currentEvent.participants = currentEvent.participants.minus(currentUser.uid)
    }

    userViewModel.editUser(currentUser)
    eventViewModel.editEvent(currentEvent)
    isJoined.value = !isJoined.value
  }
}
