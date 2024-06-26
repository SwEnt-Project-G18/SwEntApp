package com.github.se.gomeet.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.github.se.gomeet.R
import com.google.android.gms.maps.model.LatLng

private const val TAG = "NavigationAction"

/**
 * Data class representing a top level destination in the app.
 *
 * @param route The route to navigate to when this item is clicked.
 * @param icon The icon to display for this item.
 * @param textId The string resource ID for the text to display for this item.
 */
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val textId: String,
)

/** Constants for the routes in the app. */
object Route {
  const val WELCOME = "Welcome"
  const val LOGIN = "Login"
  const val REGISTER = "Register"
  const val EVENTS = "Events"
  const val TRENDS = "Trends"
  const val EXPLORE = "Explore"
  const val CREATE = "Create"
  const val PROFILE = "Profile"
  const val PUBLIC_CREATE = "Public Create"
  const val PRIVATE_CREATE = "Private Create"
  const val OTHERS_PROFILE = "OthersProfile/{uid}"
  const val EDIT_PROFILE = "EditProfile"
  const val ADD_PARTICIPANTS = "Add Participants/{eventId}"
  const val MANAGE_INVITES = "ManageInvites/{eventId}"
  const val EVENT_INFO =
      "eventInfo/{eventId}/{title}/{date}/{time}/{url}/{organizer}/{rating}/{description}/{latitude}/{longitude}"
  const val NOTIFICATIONS = "Notifications"
  const val SETTINGS = "Settings"
  const val ABOUT = "About"
  const val HELP = "Help"
  const val PERMISSIONS = "Permissions"
  const val MESSAGE = "Message/{id}"
  const val FOLLOWERS = "Followers/{uid}"
  const val FOLLOWING = "Following/{uid}"
  const val MESSAGE_CHANNELS = "MessageChannel"
  const val CHANNEL = "Channel/{id}"
  const val ADD_FRIEND = "AddFriend"
  const val EDIT_EVENT = "EditEvent/{eventId}"
  const val SCAN = "Scan"
}

val CREATE_ITEMS =
    listOf(
        TopLevelDestination(
            route = Route.PUBLIC_CREATE,
            icon = Icons.Default.AccountCircle,
            textId = Route.PUBLIC_CREATE),
        TopLevelDestination(
            route = Route.PRIVATE_CREATE,
            icon = Icons.Default.AccountCircle,
            textId = Route.PRIVATE_CREATE),
    )

val LOGIN_ITEMS =
    listOf(
        TopLevelDestination(
            route = Route.WELCOME, icon = Icons.Default.AccountCircle, textId = Route.WELCOME),
        TopLevelDestination(
            route = Route.LOGIN, icon = Icons.Default.AccountCircle, textId = Route.LOGIN),
        TopLevelDestination(
            route = Route.REGISTER, icon = Icons.Default.AccountCircle, textId = Route.REGISTER),
    )

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestination(
            route = Route.EVENTS, icon = Icons.Default.DateRange, textId = Route.EVENTS),
        TopLevelDestination(route = Route.TRENDS, icon = Icons.Default.Home, textId = Route.TRENDS),
        TopLevelDestination(
            route = Route.EXPLORE, icon = Icons.Default.Home, textId = Route.EXPLORE),
        TopLevelDestination(Route.NOTIFICATIONS, Icons.Default.Notifications, Route.NOTIFICATIONS),
        TopLevelDestination(
            route = Route.PROFILE, icon = Icons.Default.Person, textId = Route.PROFILE))

val SECOND_LEVEL_DESTINATION =
    listOf(
        TopLevelDestination(
            route = Route.OTHERS_PROFILE,
            icon = Icons.Default.Person,
            textId = Route.OTHERS_PROFILE),
        TopLevelDestination(
            route = Route.ADD_PARTICIPANTS,
            icon = Icons.Default.Person,
            textId = Route.ADD_PARTICIPANTS),
        TopLevelDestination(
            route = Route.MANAGE_INVITES,
            icon = Icons.Default.Person,
            textId = Route.MANAGE_INVITES),
        TopLevelDestination(
            route = Route.EVENT_INFO, icon = Icons.Default.Person, textId = Route.EVENT_INFO),
        TopLevelDestination(
            route = Route.SETTINGS, icon = Icons.Default.Settings, textId = Route.SETTINGS),
        TopLevelDestination(
            Route.EDIT_PROFILE, icon = Icons.Default.Person, textId = Route.EDIT_PROFILE),
        TopLevelDestination(
            route = Route.ADD_FRIEND, icon = Icons.Default.Person, textId = Route.ADD_FRIEND),
        TopLevelDestination(
            route = Route.EDIT_EVENT, icon = Icons.Default.Person, textId = Route.EDIT_EVENT),
    )

val SETTINGS =
    listOf(
        TopLevelDestination(
            route = Route.SETTINGS, icon = Icons.Default.Settings, textId = Route.SETTINGS),
        TopLevelDestination(
            route = Route.ABOUT, icon = Icons.Default.Settings, textId = Route.ABOUT),
        TopLevelDestination(
            route = Route.PERMISSIONS, icon = Icons.Default.Settings, textId = Route.PERMISSIONS),
        TopLevelDestination(route = Route.HELP, icon = Icons.Default.Settings, textId = Route.HELP))

/**
 * Class that handles navigation in the app.
 *
 * @param navController The navigation controller for the app.
 */
class NavigationActions(val navController: NavHostController) {

  /**
   * Navigates to the given destination.
   *
   * @param destination The destination to navigate to.
   * @param clearBackStack Whether to clear the back stack.
   */
  fun navigateTo(destination: TopLevelDestination, clearBackStack: Boolean = false) {
    Log.d(TAG, "Navigating to ${destination.route}, clear back stack: $clearBackStack")
    navController.navigate(destination.route) {
      if (clearBackStack) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  /**
   * Navigates to the given screen.
   *
   * @param route The route to navigate to.
   */
  fun navigateToScreen(route: String) {
    navController.navigate(route)
  }

  /**
   * Navigates to the event info screen.
   *
   * @param eventId The id of the event
   * @param title The title of the event.
   * @param date The date of the event.
   * @param time The time of the event.
   * @param url The url of the event
   * @param organizer The organizer of the event.
   * @param rating The rating of the event by the current user (0 if unrated, 1-5 otherwise).
   * @param description The description of the event.
   * @param loc The location of the event.
   */
  fun navigateToEventInfo(
      eventId: String,
      title: String,
      date: String,
      time: String,
      url: String,
      organizer: String,
      rating: Long,
      description: String,
      loc: LatLng
  ) {
    val route =
        Route.EVENT_INFO.replace("{eventId}", Uri.encode(eventId))
            .replace("{title}", Uri.encode(title))
            .replace("{date}", Uri.encode(date))
            .replace("{time}", Uri.encode(time))
            .replace("{url}", Uri.encode(url))
            .replace("{organizer}", Uri.encode(organizer))
            .replace("{rating}", rating.toString())
            .replace("{description}", Uri.encode(description))
            .replace("{latitude}", loc.latitude.toString())
            .replace("{longitude}", loc.longitude.toString())
    navController.navigate(route) {
      // popUpTo(Route.EVENTS) { inclusive = false }
    }
  }

  /** Navigates to the previous screen. */
  fun goBack() {
    navController.popBackStack()
  }
}
/**
 * Gets the icon for the given route if not selected.
 *
 * @param route The route to get the icon for.
 */
@Composable
fun getIconForRoute(route: String): ImageVector {
  return when (route) {
    Route.EVENTS -> Icons.Outlined.DateRange
    Route.TRENDS -> ImageVector.vectorResource(R.drawable.arrow_trending)
    Route.EXPLORE -> Icons.Outlined.Home
    Route.NOTIFICATIONS -> Icons.Outlined.Notifications
    Route.PROFILE -> Icons.Outlined.Person
    else -> Icons.Outlined.AccountCircle
  }
}

/**
 * Gets the icon for the given route if selected.
 *
 * @param route The route to get the icon for.
 */
@Composable
fun getIconForSelectedRoute(route: String): ImageVector {
  return when (route) {
    Route.EVENTS -> Icons.Filled.DateRange
    Route.TRENDS -> ImageVector.vectorResource(R.drawable.arrow_trending)
    Route.EXPLORE -> Icons.Filled.Home
    Route.NOTIFICATIONS -> Icons.Filled.Notifications
    Route.PROFILE -> Icons.Filled.Person
    else -> Icons.Filled.AccountCircle
  }
}
