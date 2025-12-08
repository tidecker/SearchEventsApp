package com.example.eventsearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteEvent(
    // Replace these fields with the actual fields your API returns.
    // Below are common examples based on Ticketmaster/Event data.

    @SerialName("_id")
    val id: String,

    @SerialName("eventId")
    val eventId: String,

    @SerialName("name")
    val name: String,

    @SerialName("date")
    val date: String,

    @SerialName("genre")
    val genre: String,

    @SerialName("venue")
    val venue: String,

    @SerialName("imageUrl")
    val imageUrl: String,

    @SerialName("isFavorite")
    val isFavorite: Boolean
)