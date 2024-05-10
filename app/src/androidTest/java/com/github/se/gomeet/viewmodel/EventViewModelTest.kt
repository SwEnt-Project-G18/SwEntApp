package com.github.se.gomeet.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.gomeet.model.event.Event
import com.github.se.gomeet.model.event.location.Location
import com.github.se.gomeet.model.repository.EventRepository
import com.github.se.gomeet.model.repository.UserRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventViewModelTest {

  private val title = "testevent2"
  private val uid = "testuid"

  @Test
  fun test() = runTest {
    val eventViewModel = EventViewModel(uid, EventRepository(Firebase.firestore))

    // test getAllEvents and createEvent
    eventViewModel.createEvent(
        title,
        "description",
        Location(0.0, 0.0, "name"),
        LocalDate.of(2024, 4, 29),
        0.0,
        "url",
        emptyList(),
        emptyList(),
        0,
        false,
        emptyList(),
        emptyList(),
        null,
        UserViewModel(UserRepository(Firebase.firestore)),
        uid)
    TimeUnit.SECONDS.sleep(3)

    var events: List<Event> = eventViewModel.getAllEvents()!!.filter { it.title == title }
    while (events.isEmpty()) {
      events = eventViewModel.getAllEvents()!!.filter { it.title == title }
    }

    assert(events.isNotEmpty())

    // test getEvent
    val uid = events[0].uid
    var event = eventViewModel.getEvent(uid)!!
    TimeUnit.SECONDS.sleep(3)

    assert(event.uid == uid)
    assert(event.title == title)

    assert(eventViewModel.getEvent("this_event_does_not_exist") == null)

    // test editEvent
    val newTitle = "newtestevent"
    val newEvent =
        Event(
            event.uid,
            event.creator,
            newTitle,
            event.description,
            event.location,
            event.date,
            event.price,
            event.url,
            event.participants,
            event.visibleToIfPrivate,
            event.maxParticipants,
            event.public,
            event.tags,
            event.images)

    eventViewModel.editEvent(newEvent)
    TimeUnit.SECONDS.sleep(3)

    event = eventViewModel.getEvent(uid)!!
    TimeUnit.SECONDS.sleep(3)

    assert(event.uid == uid)
    assert(event.title == newTitle)

    // test removeEvent
    eventViewModel.removeEvent(uid)
    TimeUnit.SECONDS.sleep(3)
    assert(eventViewModel.getEvent(uid) == null)
  }
}
