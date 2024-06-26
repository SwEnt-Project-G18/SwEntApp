package com.github.se.gomeet.ui.mainscreens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.user.GoMeetUser
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.android.gms.maps.model.LatLng

/**
 * This composable is used to display the invitations notifications widgets.
 *
 * @param user the user that invited
 * @param event the event to display
 * @param userViewModel the user view model
 * @param eventViewModel the event view model
 * @param initialClicked the initial state of the buttons
 * @param nav the navigation action
 */
@Composable
fun InvitationsNotificationsWidget(
    user: GoMeetUser,
    event: Event,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    initialClicked: Boolean,
    nav: NavigationActions
) {
  var clicked by rememberSaveable { mutableStateOf(initialClicked) }
  var imageUrl by remember { mutableStateOf<String?>(null) }
  val screenWidth = LocalConfiguration.current.screenWidthDp

  LaunchedEffect(Unit) { imageUrl = eventViewModel.getEventImageUrl(event.eventID) }
  Column(
      modifier =
          Modifier.clickable {
                nav.navigateToEventInfo(
                    event.eventID,
                    event.title,
                    event.getDateString(),
                    event.getTimeString(),
                    event.url,
                    event.creator,
                    0L,
                    event.description,
                    LatLng(event.location.latitude, event.location.longitude))
              }
              .padding(horizontal = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                  horizontalAlignment = Alignment.Start,
                  verticalArrangement = Arrangement.Top) {
                    var username by remember { mutableStateOf<String?>("Loading...") }
                    LaunchedEffect(event.creator) {
                      username = userViewModel.getUsername(event.creator)
                    }

                    username?.let {
                      Text(
                          it + " invited you to join " + event.title,
                          color = MaterialTheme.colorScheme.tertiary,
                          style = MaterialTheme.typography.titleMedium,
                          modifier = Modifier.padding(top = 5.dp).testTag("UserName"))
                    }

                    Text(
                        event.momentToString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium)
                  }
            }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          Button(
              onClick = {
                clicked = true
                userViewModel.userAcceptsInvitation(event, user, eventViewModel)
              },
              shape = RoundedCornerShape(10.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.outlineVariant,
                      contentColor = Color.White),
              enabled = !clicked,
              modifier = Modifier.width((screenWidth / 3).dp).testTag("AcceptButton")) {
                Text("Accept")
              }

          Spacer(modifier = Modifier.width(10.dp))

          Button(
              onClick = {
                clicked = true
                userViewModel.userRefusesInvitation(event, user, eventViewModel)
              },
              shape = RoundedCornerShape(10.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primaryContainer,
                      contentColor = MaterialTheme.colorScheme.tertiary),
              enabled = !clicked,
              modifier = Modifier.width((screenWidth / 3).dp).testTag("DeclineButton")) {
                Text("Decline")
              }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primaryContainer)
      }
}
