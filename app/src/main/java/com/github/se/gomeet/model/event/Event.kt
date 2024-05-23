package com.github.se.gomeet.model.event

import com.github.se.gomeet.model.event.location.Location
import java.time.LocalDate
import java.time.LocalTime

/**
 * This data class represents an event. An event is the main entity of the application.
 *
 * @param eventID Event's id
 * @param creator Creator of the event
 * @param title Title of the event
 * @param description Description of the event
 * @param location Location of the event
 * @param date Date of the event
 * @param time Time of the event
 * @param price Price of the Event
 * @param url Website of the event (can be ticketLink)
 * @param participants People participating to the event
 * @param visibleToIfPrivate People that can enter the event if it's private
 * @param maxParticipants Maximum number of Participants of the event
 * @param public True if the event is public, false if it's private
 * @param tags Tags of the event
 * @param eventRatings Ratings of the event by each user (i.e. userID -> rating)
 * @param images Images of the event
 * @param posts Posts of the event >>>>>>> a0979603ee96764731490b732fddd35bdc089325
 */
data class Event(
    val eventID: String,
    val creator: String,
    val title: String,
    val description: String,
    val location: Location,
    val date: LocalDate,
    val time: LocalTime,
    val price: Double,
    val url: String = "",
    val pendingParticipants: List<String> = emptyList(),
    var participants: List<String>,
    val visibleToIfPrivate: List<String> = emptyList(),
    val maxParticipants: Int,
    val public: Boolean,
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val eventRatings: Map<String, Int> = emptyMap(),
    val posts: List<Post> = emptyList()
) {

  /**
   * This function checks if the event matches the search query.
   *
   * @param query The search query.
   * @return true if the event matches the search query, false otherwise.
   */
  fun doesMatchSearchQuery(query: String): Boolean {
    val matchingCombinations =
        listOf(
            "$title$description",
            "$title $description",
            "${title.first()} ${description.first()}",
            creator,
            location.toString())

    return matchingCombinations.any { it.contains(query, ignoreCase = true) }
  }
  /**
   * This function decides whether an event is in the past or not.
   *
   * @return true if the event is today or in the future, false otherwise.
   */
  fun isPastEvent(): Boolean {
    return this.date.isBefore(LocalDate.now()) && this.date != LocalDate.now()
  }

  /**
   * This function returns whether a user has joined an event or not.
   *
   * @param userId The user's id.
   * @return true if the user has joined the event, false otherwise.
   */
  fun hasUserJoined(userId: String): Boolean {
    return this.participants.contains(userId)
  }

  /**
   * Converts the time of an event to a string.
   *
   * @return The string representation of the time.
   */
  fun getTimeString(): String {
    val minutes = if (this.time.minute < 9) "0${this.time.minute}" else this.time.minute
    val hours = if (this.time.hour < 9) "0${this.time.hour}" else this.time.hour
    return "${hours}:${minutes}"
  }

  /**
   * Converts the date of an event to a string.
   *
   * @return The string representation of the date.
   */
  fun getDateString(): String {
    val date =
        if (this.date == LocalDate.now()) "Today"
        else "${this.date.dayOfMonth}/${this.date.monthValue}/${this.date.year}"
    return date
  }

  /**
   * Converts an event's date and time to a string.
   *
   * @return The string representation of the date and time.
   */
  fun momentToString(): String {
    return "${this.getDateString()} at ${this.getTimeString()}"
  }
}
