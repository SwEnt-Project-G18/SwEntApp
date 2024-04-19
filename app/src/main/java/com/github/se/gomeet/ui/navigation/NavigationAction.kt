package com.github.se.gomeet.ui.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.github.se.gomeet.R

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val textId: String,
)

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
    const val OTHERS_PROFILE = "Others Profile"
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
        TopLevelDestination(
            route = Route.TRENDS, icon = Icons.Default.Home, textId = Route.TRENDS),
        TopLevelDestination(
            route = Route.EXPLORE, icon = Icons.Default.Home, textId = Route.EXPLORE),
        TopLevelDestination(
            route = Route.CREATE, icon = Icons.Default.Add, textId = Route.CREATE),
        TopLevelDestination(
            route = Route.PROFILE, icon = Icons.Default.Person, textId = Route.PROFILE))

val SECOND_LEVEL_DESTINATION = listOf(
    TopLevelDestination(
        route = Route.OTHERS_PROFILE, icon = Icons.Default.Person, textId = Route.OTHERS_PROFILE))

class NavigationActions(val navController: NavHostController) {
  fun navigateTo(destination: TopLevelDestination, clearBackStack: Boolean = false) {
      Log.d("Navigation", "Navigating to ${destination.route}, clear back stack: $clearBackStack")
    navController.navigate(destination.route) {
      if (clearBackStack) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  fun goBack() {
    navController.popBackStack()
  }
}

@Composable
fun getIconForRoute(route: String): ImageVector {
  return when (route) {
    Route.EVENTS -> Icons.Default.DateRange
    Route.TRENDS -> ImageVector.vectorResource(R.drawable.arrow_trending)
    Route.EXPLORE -> Icons.Default.Home
    Route.CREATE -> Icons.Default.Add
    Route.PROFILE -> Icons.Default.Person
    else -> Icons.Default.AccountCircle
  }
}
