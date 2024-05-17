package com.github.se.gomeet.viewmodel

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.event.location.Location
import com.github.se.gomeet.model.repository.EventRepository
import com.github.se.gomeet.model.repository.UserRepository
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

/**
 * ViewModel for the event. The viewModel is responsible for handling the logic that comes from the
 * UI and the repository.
 *
 * @param creatorId the id of the creator of the event
 */
class EventViewModel(private val creatorId: String? = null) : ViewModel() {
  private val _bitmapDescriptors = mutableStateMapOf<String, BitmapDescriptor>()
  val bitmapDescriptors: MutableMap<String, BitmapDescriptor> = _bitmapDescriptors

  private var lastLoadedEvents: List<Event> = emptyList()
  private val _loading = MutableLiveData(false)
  val loading: LiveData<Boolean> = _loading

//  private val currentUser = if(creatorId != null) runBlocking { UserViewModel(UserRepository(Firebase.firestore)).getUser(creatorId) } else null
    // The weight that the user tags will have compared to the number of views (for the sorting
    // algorithm)
  private val relevanceFactor = 0.5

    /**
   * Load custom pins for the events.
   *
   * @param context the context of the application
   * @param events the list of events to load the custom pins for
   */
  fun loadCustomPins(context: Context, events: List<Event>) =
      viewModelScope.launch {
        // Check if the current events are different from the last loaded events
        if (events != lastLoadedEvents) {
          _loading.value = true
          val loadJobs =
              events.map { event ->
                async {
                  val imagePath = "event_icons/${event.eventID}.png"
                  val storageRef = FirebaseStorage.getInstance().reference.child(imagePath)
                  val uri = storageRef.downloadUrl.await() // Await the download URL
                  try {
                    val bitmapDescriptor =
                        loadBitmapFromUri(context, uri) // Load the bitmap as a BitmapDescriptor
                    _bitmapDescriptors[event.eventID] = bitmapDescriptor
                  } catch (e: Exception) {
                    Log.e("ViewModel", "Error loading bitmap descriptor: ${e.message}")
                    _bitmapDescriptors[event.eventID] =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                  }
                }
              }

          try {
            loadJobs.awaitAll() // Await all loading jobs
          } finally {
            lastLoadedEvents = events.toList() // Update the last loaded events
            Log.d("ViewModel", "Finished loading custom pins")
            _loading.value = false
          }
        }
      }

  /**
   * Load a bitmap from a URI.
   *
   * @param context the context of the application
   * @param uri the URI of the image to load
   */
  suspend fun loadBitmapFromUri(context: Context, uri: Uri): BitmapDescriptor =
      suspendCancellableCoroutine { continuation ->
        // Create a temporary ImageView to load the image.
        val imageView = ImageView(context)
        imageView.layout(0, 0, 1, 1) // Minimal size

        Picasso.get()
            .load(uri)
            .into(
                imageView,
                object : com.squareup.picasso.Callback {
                  override fun onSuccess() {
                    imageView.drawable?.let { drawable ->
                      val bitmap = (drawable as BitmapDrawable).bitmap
                      continuation.resume(BitmapDescriptorFactory.fromBitmap(bitmap))
                    }
                        ?: run {
                          Log.e("ViewModel", "Drawable is null after loading image.")
                          continuation.resumeWithException(
                              RuntimeException("Drawable is null after loading image"))
                        }
                  }

                  override fun onError(e: Exception?) {
                    Log.e("ViewModel", "Error loading image from Picasso: ${e?.message}")
                    continuation.resumeWithException(
                        e ?: RuntimeException("Unknown error in Picasso"))
                  }
                })

        // Handle cancellation of the coroutine.
        continuation.invokeOnCancellation {
          imageView.setImageDrawable(null) // Clear resources
        }
      }

  /**
   * Get an event by its UID.
   *
   * @param uid the UID of the event to get
   * @return the event with the given UID, or null if it does not exist
   */
  suspend fun getEvent(uid: String): Event? {
    return try {
      val event = CompletableDeferred<Event?>()
      EventRepository.getEvent(uid) { t -> event.complete(t) }
      event.await()
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Get the image URL of an event's image.
   *
   * @param eventId the ID of the event
   * @return the image URL of the event
   */
  suspend fun getEventImageUrl(eventId: String): String? {
    val db = FirebaseFirestore.getInstance()
    return try {
      val event = CompletableDeferred<String?>()
      db.collection("events")
          .document(eventId)
          .get()
          .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
              val imagesList = documentSnapshot.get("images") as? List<*>
              if (!imagesList.isNullOrEmpty()) {
                event.complete(imagesList.firstOrNull()?.toString())
              } else {
                event.complete(null)
              }
            } else {
              event.complete(null)
            }
          }
          .addOnFailureListener { event.completeExceptionally(it) }
      event.await()
    } catch (e: Exception) {
      Log.e("Firebase", "Error fetching event image: ${e.localizedMessage}")
      null
    }
  }

  /**
   * Upload an image to Firebase Storage and get the download URL.
   *
   * @param imageUri the URI of the image to upload
   * @return the download URL of the uploaded image
   */
  suspend fun uploadImageAndGetUrl(imageUri: Uri): String {
    val imageRef = Firebase.storage.reference.child("images/${imageUri.lastPathSegment}")
    val uploadTaskSnapshot = imageRef.putFile(imageUri).await()
    return uploadTaskSnapshot.metadata?.reference?.downloadUrl?.await().toString()
  }

  /** Get all events that exist in the database. */
  suspend fun getAllEvents(): List<Event>? {
    return try {
      val event = CompletableDeferred<List<Event>?>()
      EventRepository.getAllEvents { t -> event.complete(t) }
      event.await()
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Create an event.
   *
   * @param title the title of the event
   * @param description the description of the event
   * @param location the location of the event
   * @param date the date of the event
   * @param price the price of the event
   * @param url the URL of the event
   * @param participants the participants of the event
   * @param visibleToIfPrivate the users that the event is visible to if it is private
   * @param maxParticipants the maximum number of participants of the event
   * @param public whether the event is public
   * @param tags the tags of the event
   * @param images the images of the event
   * @param imageUri the URI of the image of the event
   * @param eventId the UID of the event
   */
  fun createEvent(
      title: String,
      description: String,
      location: Location,
      date: LocalDate,
      time: LocalTime,
      price: Double,
      url: String,
      pendingParticipants: List<String>,
      participants: List<String>,
      visibleToIfPrivate: List<String>,
      maxParticipants: Int,
      public: Boolean,
      tags: List<String>,
      images: List<String>,
      imageUri: Uri?,
      userViewModel: UserViewModel,
      eventId: String
  ) {
    Log.d("CreatorID", "Creator ID is $creatorId")
    CoroutineScope(Dispatchers.IO).launch {
      try {
          val participantsWithCreator = if(participants.contains(creatorId)) participants else participants.plus(creatorId!!)
        val imageUrl = imageUri?.let { uploadImageAndGetUrl(it) }
        val updatedImages = images.toMutableList().apply { imageUrl?.let { add(it) } }
        val event =
            Event(
                eventId,
                creatorId!!,
                title,
                description,
                location,
                date,
                time,
                price,
                url,
                pendingParticipants,
                participantsWithCreator,
                visibleToIfPrivate,
                maxParticipants,
                public,
                tags,
                updatedImages)

        EventRepository.addEvent(event)
          lastLoadedEvents = lastLoadedEvents.plus(event)
        userViewModel.joinEvent(event.eventID, creatorId)
        userViewModel.userCreatesEvent(event.eventID, creatorId)
      } catch (e: Exception) {
        Log.w(TAG, "Error uploading image or adding event", e)
      }
    }
  }

  /**
   * Edit an event.
   *
   * @param event the event to edit
   */
  fun editEvent(event: Event) {
    lastLoadedEvents = lastLoadedEvents.filter { it.eventID != event.eventID }
    lastLoadedEvents = lastLoadedEvents.plus(event)
    EventRepository.updateEvent(event)
  }

  /**
   * Remove an event by its UID.
   *
   * @param eventID the ID of the event to remove
   */
  fun removeEvent(eventID: String) {
    lastLoadedEvents = lastLoadedEvents.filter { it.eventID != eventID }
    EventRepository.removeEvent(eventID)
  }

  /**
   * Update the event participants field by adding the given user to the list. Note that this
   * function should be called at the same time as the equivalent function in the UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to add to the event
   */
  fun joinEvent(event: Event, userId: String) {
    if (event.participants.contains(userId)) {
      Log.w(TAG, "User $userId is already in event ${event.eventID}")
      return
    }

    EventRepository.updateEvent(event.copy(participants = event.participants.plus(userId)))
  }

  /**
   * Update the event pendingParticipants field by adding the given user to the list. Note that this
   * function should be called at the same time as the equivalent function in the UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to add to the event
   */
  fun sendInvitation(event: Event, userId: String) {
    if (event.pendingParticipants.contains(userId)) {
      Log.w(TAG, "User $userId is already invited to event ${event.eventID}")
      return
    }

    EventRepository.updateEvent(
        event.copy(pendingParticipants = event.pendingParticipants.plus(userId)))
  }

  /**
   * Update the event pendingParticipants field by removing the given user to the list and adds the
   * given user to the participants list of the event. Note that this function should be called at
   * the same time as the equivalent function in the UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to add to the event
   */
  fun acceptInvitation(event: Event, userId: String) {
    assert(event.pendingParticipants.contains(userId))
    EventRepository.updateEvent(
        event.copy(pendingParticipants = event.pendingParticipants.minus(userId)))
    joinEvent(event, userId)
  }

  /**
   * Update the event pendingParticipants field by removing the given user from the list. Note that
   * this function should be called at the same time as the equivalent function in the
   * UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to remove from the event
   */
  fun declineInvitation(event: Event, userId: String) {
    assert(event.pendingParticipants.contains(userId))
    EventRepository.updateEvent(
        event.copy(pendingParticipants = event.pendingParticipants.minus(userId)))
  }

  /**
   * Update the event participants field by removing the given user from the list. Note that this
   * function should be called at the same time as the equivalent function in the UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to remove from the event
   */
  fun kickParticipant(event: Event, userId: String) {
    assert(event.participants.contains(userId))
    EventRepository.updateEvent(event.copy(participants = event.participants.minus(userId)))
  }

  /**
   * Update the event pendingParticipants field by removing the given user from the list. Note that
   * this function should be called at the same time as the equivalent function in the
   * UserViewModel.
   *
   * @param event the event to update
   * @param userId the ID of the user to remove from the event
   */
  fun cancelInvitation(event: Event, userId: String) {
    if (!event.pendingParticipants.contains(userId)) {
      Log.w(TAG, "Event doesn't have $userId as a pendingParticipant")
      return
    }

    EventRepository.updateEvent(
        event.copy(pendingParticipants = event.pendingParticipants.minus(userId)))
  }

  /**
   * Get the location of an event.
   *
   * @param locationName the name of the location
   * @param numberOfResults the number of results to get
   * @param onResult the function to call with the result
   */
  fun location(locationName: String, numberOfResults: Int, onResult: (List<Location>) -> Unit) {
    viewModelScope.launch {
      try {
        val client = OkHttpClient()
        val url =
            "https://nominatim.openstreetmap.org/search?q=${
                        locationName.replace(
                            " ",
                            "+"
                        )
                    }&format=json&limit=$numberOfResults"
        val req = Request.Builder().url(url).build()
        val res = withContext(Dispatchers.IO) { client.newCall(req).execute() }
        if (!res.isSuccessful) throw IOException("IOException")
        val resBody = res.body?.string() ?: throw IOException("No response from nominatim")
        val locations = locHelper(resBody, numberOfResults)
        withContext(Dispatchers.Main) { onResult(locations) }
      } catch (e: Exception) {
        onResult(emptyList())
      }
    }
  }

    /**
     * Update the number of views of an event.
     *
     * @param eventID the id of the event to update
     */
    fun sawEvent(eventID: String) {
        val event = lastLoadedEvents.find { it.eventID == eventID } ?: return
        lastLoadedEvents = lastLoadedEvents.filter { it.eventID != eventID}
        lastLoadedEvents = lastLoadedEvents.plus(event.copy(nViews = event.nViews + 1))
        EventRepository.updateEvent(event.copy(nViews = event.nViews + 1))
        Log.d("ViewModel", "Saw event $eventID")
    }

  /**
   * Helper function to parse the location response.
   *
   * @param responseBody the response body
   * @param numberOfResults the number of results to get
   * @return the list of locations
   */
  private fun locHelper(responseBody: String, numberOfResults: Int): List<Location> {
    val locations: MutableList<Location> = mutableListOf()
    val jar = JSONArray(responseBody)
    if (jar.length() > 0) {
      for (i in 0 until numberOfResults) {
        try {
          val jObj = jar.getJSONObject(i)
          val displayName = jObj.optString("display_name", "Unknown Location")
          locations.add(
              Location(
                  jObj.getString("lat").toDouble(), jObj.getString("lon").toDouble(), displayName))
        } catch (e: Exception) {
          return locations
        }
      }
    }
    return locations
  }

    /**
     * Sort the events depending on the number of event views and the user's preferences. Called each
     * time getAllEvents() is called or when loadCustomPins() is called. If currentUser is null, the
     * events are only sorted by descending order of nViews. This method is like O(n^3) lmao, but hey
     * it works so don't worry about it too much bro
     */
    private fun sortEvents() {

        // Sort events by descending order of nViews
        val currentUser = if(creatorId != null) runBlocking { UserViewModel().getUser(creatorId) } else null

        if (currentUser != null) {
            val tags = currentUser.tags
            val eventScoreList: Map<String, Pair<Int, Int>> = mutableMapOf()
            var maxTags = 0
            var maxViews = 0
            lastLoadedEvents.forEach { event ->
                eventScoreList.plus(Pair(event.eventID, Pair(0, 0)))
                // Check if the event has any of the user's preferred tags
                event.tags.forEach { tag ->
                    if (tags.contains(tag)) {
                        eventScoreList[event.eventID]?.first?.plus(event.nViews)
                        eventScoreList[event.eventID]?.second?.plus(1)
                    }
                }
                val nTags = eventScoreList[event.eventID]?.second ?: 0
                if (nTags > maxTags) maxTags = nTags
                if (event.nViews > maxViews) maxViews = event.nViews
            }

            val sortedEvents =
                lastLoadedEvents.sortedByDescending { event ->
                    val tagScore: Double =
                        (eventScoreList[event.eventID]?.second?.toDouble() ?: 0.0) / (maxTags.toDouble())
                    val viewScore: Double = (event.nViews.toDouble() / maxViews.toDouble())
                    (1 - relevanceFactor) * viewScore + relevanceFactor * tagScore
                }

            lastLoadedEvents = sortedEvents
        } else {
            lastLoadedEvents = lastLoadedEvents.sortedByDescending { event -> event.nViews }
        }
    }

  /** Events sorting enum, placed here because this is also where the sorting algorithm goes. */
  enum class SortOption {
    DEFAULT,
    ALPHABETICAL,
    DATE
  }
}
