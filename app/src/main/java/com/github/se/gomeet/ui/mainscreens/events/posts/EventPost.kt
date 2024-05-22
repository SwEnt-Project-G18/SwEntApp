package com.github.se.gomeet.ui.mainscreens.events.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.gomeet.R
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.event.Post
import com.github.se.gomeet.model.event.getEventDateString
import com.github.se.gomeet.model.event.getEventTimeString
import com.github.se.gomeet.model.user.GoMeetUser
import com.github.se.gomeet.ui.mainscreens.profile.ProfileImage
import com.github.se.gomeet.viewmodel.EventViewModel
import com.github.se.gomeet.viewmodel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EventPost(event: Event, post: Post, userViewModel: UserViewModel, eventViewModel: EventViewModel, currentUser: String) {
    var poster by remember { mutableStateOf<GoMeetUser?>(null) }
    var liked by remember { mutableStateOf(false) }
    var likes by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            poster = userViewModel.getUser(post.userId)
            liked = post.likes.contains(currentUser)
            likes =  post.likes.size
        }
    }

    Column {
        if (poster !=null){
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("UserInfo")) {
                    ProfileImage(
                        userId = poster!!.uid,
                        modifier = Modifier.testTag("Profile Picture"),
                        size = 50.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            (poster!!.firstName + " " + poster!!.lastName),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Text(
                            text = "@" + (poster!!.username),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            if (post.content.isNotEmpty()){

                Spacer(modifier = Modifier.height(screenHeight / 60))
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary)
            }



            if (post.image.isNotEmpty()) {
                Spacer(modifier = Modifier.height(screenHeight / 80))
                Image(
                    painter = rememberAsyncImagePainter(post.image),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier =
                    Modifier
                        .padding(horizontal = 10.dp)
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(20.dp)))
            }
            Row (verticalAlignment = Alignment.CenterVertically)  {
                IconButton(onClick = {
                    val oldPost = post.copy()
                    if (liked){
                        likes --
                        post.likes = post.likes.minus(currentUser)
                        eventViewModel.editPost(event, oldPost, post)
                    } else {
                        likes ++
                        post.likes = post.likes.plus(currentUser)
                        eventViewModel.editPost(event, oldPost, post)
                    }
                    liked = !liked
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!liked){
                            Icon(Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = likes.toString(),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.bodyMedium)
                        }else{
                            Icon(Icons.Filled.ThumbUp,
                                contentDescription = "Like",
                                tint = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = likes.toString(),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                style = MaterialTheme.typography.bodyMedium)

                        }

                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(text = getEventDateString( post.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)

                Text(text = ", " + getEventTimeString( post.time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}