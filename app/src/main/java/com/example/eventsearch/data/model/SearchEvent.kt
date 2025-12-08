package com.example.eventsearch.data.model

import java.time.LocalDateTime

data class SearchEvent(
    val id: String,
    val name: String,
    val categoryLabel: String,
    val dateTimeLabel: String,
    val imageUrl: String,
    val venueName: String,
    val sortKey: LocalDateTime?    // used only for sorting
)
