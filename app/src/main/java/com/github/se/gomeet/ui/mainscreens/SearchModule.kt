package com.github.se.gomeet.ui.mainscreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.github.se.gomeet.R
import com.github.se.gomeet.ui.navigation.NavigationActions
import com.github.se.gomeet.ui.navigation.Route
import com.github.se.gomeet.viewmodel.SearchViewModel
import com.google.android.gms.maps.model.LatLng
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * Composable function for the search module
 *
 * @param nav navigation actions
 * @param backgroundColor background color of the search bar
 * @param contentColor color of the content of the search bar
 * @param currentUID current user's uid
 */
@Composable
fun SearchModule(
    nav: NavigationActions,
    backgroundColor: Color,
    contentColor: Color,
    currentUID: String
) {
  val viewModel = viewModel<SearchViewModel>()
  val searchText by viewModel.searchText.collectAsState()
  val persons by viewModel.searchQuery.collectAsState()
  val isSearching by viewModel.isSearching.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  Column(modifier = Modifier.padding(16.dp)) {
    TextField(
        textStyle = MaterialTheme.typography.bodySmall,
        value = searchText,
        leadingIcon = {
          IconButton(
              onClick = { // TODO: Handle Voice Search
              }) {
                Icon(
                    ImageVector.vectorResource(R.drawable.mic_icon),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp))
              }
        },
        onValueChange = viewModel::performSearch,
        modifier =
            Modifier.fillMaxWidth()
                .height(45.dp)
                .focusRequester(focusRequester)
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor),
        placeholder = {
          Text("Search", color = contentColor, style = MaterialTheme.typography.bodySmall)
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                  keyboardController?.hide()
                  coroutineScope.launch { viewModel.performSearch(searchText) }
                }),
        colors =
            TextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                cursorColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent),
        singleLine = true)

    Spacer(modifier = Modifier.height(16.dp))
    if (isSearching) {
      Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = contentColor)
      }
    } else if (persons.isNotEmpty() && searchText.isNotEmpty()) {
      LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
        items(persons) { item ->
          SearchModuleSnippet(item, nav = nav, backgroundColor = backgroundColor, currentUID)
        }
      }
    }
  }
}

/**
 * Composable for a search result
 *
 * @param item item being displayed
 * @param nav navigation actions
 * @param backgroundColor background color of the composable
 * @param currentUID current user's uid
 */
@Composable
fun SearchModuleSnippet(
    item: SearchViewModel.SearchableItem,
    nav: NavigationActions,
    backgroundColor: Color,
    currentUID: String
) {
  when (item) {
    is SearchViewModel.SearchableItem.User -> {
      val painter: Painter =
          if (item.user.profilePicture.isNotEmpty()) {
            rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = item.user.profilePicture)
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
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
              Modifier.padding(8.dp)
                  .clickable {
                    nav.navigateToScreen(Route.OTHERS_PROFILE.replace("{uid}", item.user.uid))
                  }
                  .background(color = backgroundColor, shape = RoundedCornerShape(10.dp))) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painter,
                contentDescription = "User Icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(55.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
              Text(
                  text = "${item.user.firstName} ${item.user.lastName}",
                  color = MaterialTheme.colorScheme.onBackground)
              Text(text = "@${item.user.username}", color = Color.Gray)
            }
          }
    }
    is SearchViewModel.SearchableItem.Event -> {
      val painter: Painter =
          if (item.event.images.isNotEmpty()) {
            rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = item.event.images[0])
                    .apply(
                        block =
                            fun ImageRequest.Builder.() {
                              crossfade(true)
                              placeholder(R.drawable.gomeet_icon)
                            })
                    .build())
          } else {
            painterResource(id = R.drawable.gomeet_icon)
          }
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
              Modifier.padding(vertical = 8.dp)
                  .background(color = backgroundColor, shape = RoundedCornerShape(10.dp))
                  .clickable {
                    nav.navigateToEventInfo(
                        eventId = item.event.eventID,
                        title = item.event.title,
                        date = item.event.date.toString(),
                        time = item.event.time.toString(),
                        description = item.event.description,
                        url = item.event.url,
                        organizer = item.event.creator,
                        loc = LatLng(item.event.location.latitude, item.event.location.longitude),
                        rating = item.event.ratings[currentUID] ?: 0)
                  }) {
            Image(
                painter = painter,
                contentDescription = "Event Icon",
                modifier =
                    Modifier.size(100.dp) // Make the image bigger
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(vertical = 8.dp) // Add vertical padding around the texts
                ) {
                  Text(
                      text = item.event.title,
                      modifier = Modifier.fillMaxWidth(),
                      color = MaterialTheme.colorScheme.onBackground,
                      fontSize = 20.sp // Make the title text larger
                      )
                  Text(
                      text =
                          "${item.event.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))} - ${item.event.time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                      color = MaterialTheme.colorScheme.onBackground,
                      modifier = Modifier.fillMaxWidth())
                  val croppedDescription =
                      if (item.event.description.length > 150) {
                        item.event.description.take(150) + "..."
                      } else {
                        item.event.description
                      }
                  Text(croppedDescription, color = Color.Gray)
                }
          }
    }
  }
}
