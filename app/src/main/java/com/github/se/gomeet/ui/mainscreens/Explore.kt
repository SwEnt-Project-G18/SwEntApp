package com.github.se.gomeet.ui.mainscreens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.se.gomeet.R
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.ui.navigation.BottomNavigationMenu
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.ui.theme.DarkCyan
import com.github.se.gomeet.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val defaultPosition = LatLng(46.51912357457158, 6.568023741881372)
private const val defaultZoom = 16f

private enum class CameraAction {
  NO_ACTION,
  MOVE,
  ANIMATE
}

private var moveToCurrentLocation = mutableStateOf(CameraAction.NO_ACTION)

@Composable
fun Explore(nav: NavigationActions, eventViewModel: EventViewModel) {
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  val locationPermitted: MutableState<Boolean?> = remember { mutableStateOf(null) }
  val locationPermissionsAlreadyGranted =
      (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED) ||
          (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED)
  val locationPermissions =
      arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
  val locationPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions(),
          onResult = { permissions ->
            when {
              permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationPermitted.value = true
              }
              permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermitted.value = true
              }
              else -> {
                locationPermitted.value = false
              }
            }
          })
  val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

  val eventList = remember { mutableStateListOf<Event>() }
  val query = remember { mutableStateOf("") }
  val currentPosition = remember { mutableStateOf(defaultPosition) }
  var isMapLoaded by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    if (locationPermissionsAlreadyGranted) {
      locationPermitted.value = true
    } else {
      locationPermissionLauncher.launch(locationPermissions)
    }

    // wait for user input
    while (locationPermitted.value == null) {
      delay(100)
    }

    while (true) {
      coroutineScope.launch {
        val allEvents = eventViewModel.getAllEvents()
        if (allEvents != null) {
          eventList.retainAll(allEvents)
          eventList.addAll(allEvents - eventList)
        }

        if (locationPermitted.value == true) {
          val priority = PRIORITY_BALANCED_POWER_ACCURACY
          val result =
              locationClient
                  .getCurrentLocation(
                      priority,
                      CancellationTokenSource().token,
                  )
                  .await()
          result?.let { fetchedLocation ->
            currentPosition.value = LatLng(fetchedLocation.latitude, fetchedLocation.longitude)
            isMapLoaded = true
          }
        } else if (locationPermitted.value == false) {
          isMapLoaded = true
        }
      }
      delay(5000) // map is updated every 5s
    }
  }

  Scaffold(
      floatingActionButton = {
        if (locationPermitted.value == true) {
          FloatingActionButton(
              onClick = { moveToCurrentLocation.value = CameraAction.ANIMATE },
              modifier = Modifier.size(45.dp),
              containerColor = DarkCyan) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.location_icon),
                    contentDescription = null,
                    tint = Color.White)
              }
        }
      },
      floatingActionButtonPosition = FabPosition.Start,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selectedTab ->
              if (selectedTab != "Explore") {
                nav.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == selectedTab })
              }
            },
            tabList = TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.EXPLORE)
      }) { innerPadding ->
        if (isMapLoaded) {
          moveToCurrentLocation.value = CameraAction.MOVE

          Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMapView(
                currentPosition = currentPosition,
                events = eventList,
                modifier = Modifier.testTag("Map"),
                query = query,
                locationPermitted = locationPermitted.value!!)
          }
        } else {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        SearchBar(query, Color.White)
      }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    currentPosition: MutableState<LatLng>,
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {},
    events: SnapshotStateList<Event>,
    query: MutableState<String>,
    locationPermitted: Boolean
) {
  val coroutineScope = rememberCoroutineScope()

  val eventLocations =
      events.map { event -> LatLng(event.location.latitude, event.location.longitude) }
  val eventStates = eventLocations.map { location -> rememberMarkerState(position = location) }

  val uiSettings by remember {
    mutableStateOf(
        MapUiSettings(
            compassEnabled = false, zoomControlsEnabled = false, myLocationButtonEnabled = false))
  }
  val mapProperties by remember {
    mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = locationPermitted))
  }
  val mapVisible by remember { mutableStateOf(true) }
  val cameraPositionState = rememberCameraPositionState()

  LaunchedEffect(moveToCurrentLocation.value, Unit) {
    if (moveToCurrentLocation.value == CameraAction.MOVE) {
      coroutineScope.launch {
        cameraPositionState.move(
            update =
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(currentPosition.value, defaultZoom)))
        moveToCurrentLocation.value = CameraAction.NO_ACTION
      }
    } else if (moveToCurrentLocation.value == CameraAction.ANIMATE) {
      coroutineScope.launch {
        cameraPositionState.animate(
            update =
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(currentPosition.value, defaultZoom)),
            durationMs = 1000)
        moveToCurrentLocation.value = CameraAction.NO_ACTION
      }
    }
  }

  if (mapVisible) {
    Box(Modifier.fillMaxSize()) {
      GoogleMap(
          modifier = modifier,
          cameraPositionState = cameraPositionState,
          properties = mapProperties,
          uiSettings = uiSettings,
          onMapLoaded = onMapLoaded,
          onPOIClick = {}) {
            val markerClick: (Marker) -> Boolean = { false }

            for (i in eventStates.indices) {
              MarkerInfoWindowContent(
                  state = eventStates[i],
                  title = events[i].title,
                  icon =
                      BitmapDescriptorFactory.defaultMarker(
                          BitmapDescriptorFactory.HUE_RED), // TODO: change this
                  onClick = markerClick,
                  visible = events[i].title.contains(query.value, ignoreCase = true)) {
                    Text(it.title!!, color = Color.Black)
                  }
            }
            content()
          }
    }
  }
}
