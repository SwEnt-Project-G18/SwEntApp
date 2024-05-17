package com.github.se.gomeet.ui.mainscreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.github.se.gomeet.R
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.ui.mainscreens.events.EventWidget
import com.github.se.gomeet.ui.mainscreens.events.GoMeetSearchBar
import com.github.se.gomeet.ui.navigation.BottomNavigationMenu
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.github.se.gomeet.ui.theme.DarkCyan
import com.github.se.gomeet.ui.theme.NavBarUnselected
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.SearchViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.launch

// TODO : This class has only been implemented for testing purposes!
//  It is showing ALL EVENTS IN FIREBASE,
//  THIS IS NOT THE IMPLEMENTATION OF TRENDS

/**
 * Trends screen composable. This is where the popular trends are displayed.
 *
 * @param nav Navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Trends(
    currentUser: String,
    nav: NavigationActions,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel
) {

  val eventList = remember { mutableListOf<Event>() }
  val coroutineScope = rememberCoroutineScope()
  val viewModel = viewModel<SearchViewModel>()
  val query by viewModel.searchText.collectAsState()
  // val query = remember { mutableStateOf("") }
  var eventsLoaded = remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      val allEvents = eventViewModel.getAllEvents()!!
      if (allEvents.isNotEmpty()) {
        eventList.addAll(allEvents)
      }
      eventsLoaded.value = true
    }
  }

  Scaffold(
      topBar = {
        Text(
            text = "Trends",
            modifier = Modifier.padding(start = 15.dp, top = 15.dp, end = 15.dp, bottom = 0.dp),
            color = DarkCyan,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Default,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineLarge)
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { selectedTab ->
              nav.navigateTo(TOP_LEVEL_DESTINATIONS.first { it.route == selectedTab })
            },
            tabList = TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.TRENDS)
      }) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding)) {
              Spacer(modifier = Modifier.height(5.dp))
              GoMeetSearchBar(query, NavBarUnselected, Color.DarkGray)
              Spacer(modifier = Modifier.height(5.dp))

              if (!eventsLoaded.value) {
                LoadingText()
              } else {

                Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()) {
                  Text(
                      text = "All Events",
                      style =
                          TextStyle(
                              fontSize = 20.sp,
                              lineHeight = 16.sp,
                              fontFamily = FontFamily(Font(R.font.roboto)),
                              fontWeight = FontWeight(1000),
                              color = DarkCyan,
                              textAlign = TextAlign.Center,
                              letterSpacing = 0.5.sp,
                          ),
                      modifier = Modifier.padding(10.dp).align(Alignment.Start))

                  eventList.forEach { event ->
                    if (event.title.contains(query, ignoreCase = true)) {
                      val painter: Painter =
                          if (event.images.isNotEmpty()) {
                            rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = event.images[0])
                                    .apply(
                                        block =
                                            fun ImageRequest.Builder.() {
                                              crossfade(true)
                                              placeholder(R.drawable.gomeet_logo)
                                            })
                                    .build())
                          } else {
                            painterResource(id = R.drawable.gomeet_logo)
                          }

                      EventWidget(
                          userName = event.creator,
                          eventName = event.title,
                          eventId = event.uid,
                          eventDescription = event.description,
                          eventDate =
                              Date.from(
                                  event.date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                          eventPicture = painter,
                          verified = false,
                          nav = nav) // verification to be done using user details
                    }
                  }
                }
              }
            }
      }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun EventCarousel(events: List<Event>, nav: NavigationActions) {
  val pagerState = rememberPagerState()

  LaunchedEffect(pagerState) {
    launch {
      while (true) {
        delay(10000) // Wait for 10 seconds
        val nextPage = if (events.isNotEmpty()) (pagerState.currentPage + 1) % events.size else 0
        pagerState.animateScrollToPage(nextPage)
      }
    }
  }

  Column(modifier = Modifier.fillMaxWidth().height(250.dp)) {
    HorizontalPager(count = events.size, state = pagerState, modifier = Modifier.weight(1f)) { page
      ->
      val event = events[page]
      val painter =
          if (event.images.isNotEmpty()) {
            rememberAsyncImagePainter(event.images[0])
          } else {
            painterResource(id = R.drawable.gomeet_logo)
          }

      val eventDate = Date.from(event.date.atStartOfDay(ZoneId.systemDefault()).toInstant())

      val currentDate = Calendar.getInstance()
      val startOfWeek = currentDate.clone() as Calendar
      startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
      val endOfWeek = startOfWeek.clone() as Calendar
      endOfWeek.add(Calendar.DAY_OF_WEEK, 6)

      val eventCalendar = Calendar.getInstance().apply { time = eventDate }

      val isThisWeek = eventCalendar.after(currentDate) && eventCalendar.before(endOfWeek)
      val isToday =
          currentDate.get(Calendar.YEAR) == eventCalendar.get(Calendar.YEAR) &&
              currentDate.get(Calendar.DAY_OF_YEAR) == eventCalendar.get(Calendar.DAY_OF_YEAR)

      val dayFormat =
          if (isThisWeek) {
            SimpleDateFormat("EEEE", Locale.getDefault())
          } else {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault())
          }

      val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

      val dayString =
          if (isToday) {
            "Today"
          } else {
            dayFormat.format(eventDate)
          }
      val timeString = timeFormat.format(eventDate)

      Box(
          modifier =
              Modifier.padding(8.dp)
                  .background(Color.Gray, shape = RoundedCornerShape(16.dp))
                  .fillMaxSize()
                  .clickable {
                    nav.navigateToEventInfo(
                        eventId = event.eventID,
                        title = event.title,
                        date = dayString,
                        time = timeString,
                        organizer = event.creator,
                        rating = 0.0,
                        description = event.description,
                        loc = LatLng(event.location.latitude, event.location.longitude))
                  }
                  .clip(RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
              Box(
                  modifier =
                      Modifier.clip(RoundedCornerShape(8.dp))
                          .background(Color.Black.copy(alpha = 0.5f))
                          .padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontSize = 17.sp,
                    )
                  }
            }
          }
    }

    HorizontalPagerIndicator(
        pagerState = pagerState,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
        inactiveColor = MaterialTheme.colorScheme.inverseOnSurface,
        activeColor = MaterialTheme.colorScheme.onSurface)
  }
}

enum class SortOption {
  DEFAULT,
  ALPHABETICAL,
  DATE
}

@Composable
fun SortButton(eventList: MutableList<Event>) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(SortOption.DEFAULT) }

  Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 10.dp, end = 10.dp)) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiary),
            onClick = { expanded = true }) {
              Text("Sort")
            }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier.align(Alignment.Center)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)) {
              DropdownMenuItem(
                  text = { Text("Popularity") },
                  onClick = {
                    // TODO: Implement popularity sorting
                    selectedOption = SortOption.DEFAULT
                    expanded = false
                  },
                  modifier =
                      Modifier.background(
                          if (selectedOption == SortOption.DEFAULT)
                              MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                          else Color.Transparent))
              DropdownMenuItem(
                  text = { Text("Name") },
                  onClick = {
                    selectedOption = SortOption.ALPHABETICAL
                    eventList.sortBy { it.title }
                    expanded = false
                  },
                  modifier =
                      Modifier.background(
                          if (selectedOption == SortOption.ALPHABETICAL)
                              MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                          else Color.Transparent))
              DropdownMenuItem(
                  text = { Text("Date") },
                  onClick = {
                    selectedOption = SortOption.DATE
                    eventList.sortBy { it.date }
                    expanded = false
                  },
                  modifier =
                      Modifier.background(
                          if (selectedOption == SortOption.DATE)
                              MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                          else Color.Transparent))
            }
      }
}
