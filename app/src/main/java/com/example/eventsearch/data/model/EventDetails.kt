package com.example.eventsearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDetails(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("url")
    val url: String? = null,

    // Ticketmaster event URL
    @SerialName("ticketmasterUrl")
    val ticketmasterUrl: String? = null,

    @SerialName("images")
    val images: List<TmImage> = emptyList(),       // header image, etc.

    @SerialName("dates")
    val dates: TmDates? = null,                    // local date/time + status
    @SerialName("priceRanges")
    val priceRanges: List<TmPriceRange>? = null,   // min/max prices (if present)

    @SerialName("classifications")
    val classifications: List<TmClassification>? = null, // segment/genre/...

    @SerialName("seatmap")
    val seatmap: TmSeatmap? = null,                // seatmap.staticUrl

    @SerialName("_embedded")
    val embedded: TmEmbedded? = null               // venues / attractions
)

@Serializable
data class TmSeatmap(
    val staticUrl: String? = null
)


@Serializable
data class TmImage(
    @SerialName("url")
    val url: String? = null
)

@Serializable
data class TmDates(
    @SerialName("start")
    val start: TmStart? = null,
    @SerialName("status")
    val status: TmStatus? = null
)

@Serializable
data class TmStart(
    @SerialName("localDate") val localDate: String? = null,
    @SerialName("localTime") val localTime: String? = null,
    @SerialName("dateTime") val dateTime: String? = null
)

@Serializable
data class TmStatus(
    @SerialName("code")
    val code: String? = null       // onsale / offsale / canceled / postponed / rescheduled
)

@Serializable
data class TmPriceRange(
    @SerialName("type")
    val type: String? = null,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("min")
    val min: Double? = null,
    @SerialName("max")
    val max: Double? = null
)

@Serializable
data class TmClassification(
    @SerialName("segment")
    val segment: TmNameHolder? = null,
    @SerialName("genre")
    val genre: TmNameHolder? = null,
    @SerialName("subGenre") val subGenre: TmNameHolder? = null,
    val type: TmNameHolder? = null,
    @SerialName("subType") val subType: TmNameHolder? = null
)

@Serializable
data class TmNameHolder(
    @SerialName("name")
    val name: String? = null
)

/**
 * Embedded data: venues + attractions (artists/teams)
 */
@Serializable
data class TmEmbedded(
    @SerialName("venues")
    val venues: List<TmVenue>? = null,
    @SerialName("attractions")
    val attractions: List<TmAttraction>? = null
)

@Serializable
data class TmVenue(
    @SerialName("name")
    val name: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("address")
    val address: TmVenueAddress? = null,
    @SerialName("city")
    val city: TmNameHolder? = null,
    @SerialName("state")
    val state: TmStateInfo? = null,
    @SerialName("country")
    val country: TmNameHolder? = null,
    @SerialName("postalCode")
    val location: TmVenueLocation? = null,
    @SerialName("boxOfficeInfo")
    val boxOfficeInfo: TmBoxOfficeInfo? = null,
    @SerialName("generalInfo")
    val generalInfo: TmGeneralInfo? = null
)

@Serializable
data class TmVenueAddress(
    @SerialName("line1")
    val line1: String? = null
)

@Serializable
data class TmStateInfo(
    @SerialName("name")
    val name: String? = null,
    @SerialName("stateCode")
    val stateCode: String? = null
)

@Serializable
data class TmVenueLocation(
    @SerialName("longitude")
    val longitude: String? = null,
    @SerialName("latitude")
    val latitude: String? = null
)

@Serializable
data class TmBoxOfficeInfo(
    @SerialName("phoneNumberDetail")
    val phoneNumberDetail: String? = null,
    @SerialName("openHoursDetail")
    val openHoursDetail: String? = null
)

@Serializable
data class TmGeneralInfo(
    @SerialName("generalRule")
    val generalRule: String? = null,
    @SerialName("childRule")
    val childRule: String? = null
)

@Serializable
data class TmAttraction(
    @SerialName("name")
    val name: String? = null
)
