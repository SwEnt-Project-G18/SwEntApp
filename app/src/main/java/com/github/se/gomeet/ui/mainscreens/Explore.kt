package com.github.se.gomeet.ui.mainscreens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.ui.navigation.BottomNavigationMenu
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.viewmodel.EventViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun Explore(nav: NavigationActions, eventViewModel: EventViewModel) {
  Log.d("Explore", "Back in Explore")
  val coroutineScope = rememberCoroutineScope()

  var isMapLoaded by remember { mutableStateOf(false) }
  val eventList = remember { mutableListOf<Event>() }

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      eventList.addAll(eventViewModel.getAllEvents()!!)
      isMapLoaded = true
    }
  }

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selectedTab ->
              nav.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == selectedTab })
            },
            tabList = TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.EXPLORE)
      }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
          if (isMapLoaded) {
            GoogleMapView(events = eventList)
          }
        }
      }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {},
    events: List<Event>
) {

  val locations = events.map { event -> LatLng(event.location.latitude, event.location.longitude) }
  val states = locations.map { location -> rememberMarkerState(position = location) }

  val uiSettings by remember {
    mutableStateOf(MapUiSettings(compassEnabled = false, zoomControlsEnabled = false))
  }
  val mapProperties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL)) }
  val mapVisible by remember { mutableStateOf(true) }

  if (mapVisible) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = onMapLoaded,
        onPOIClick = {}) {
          val markerClick: (Marker) -> Boolean = { false }

          for (i in states.indices) {
            MarkerInfoWindowContent(
                state = states[i],
                title = events[i].title,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = markerClick,
            ) {
              Text(it.title!!, color = Color.Black)
            }
          }
          content()
        }
  }
}
