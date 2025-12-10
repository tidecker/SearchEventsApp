package com.example.eventsearch.data

import com.example.eventsearch.data.model.*
import org.json.JSONObject

fun parseEventDetails(json: String): EventDetails {
    val root = JSONObject(json)

    val id = root.optString("id", null)
    val name = root.optString("name", null)
    val url = root.optString("url", null)

    // --- images ---
    val imagesJson = root.optJSONArray("images")
    val images = mutableListOf<TmImage>()
    if (imagesJson != null) {
        for (i in 0 until imagesJson.length()) {
            val imgObj = imagesJson.optJSONObject(i) ?: continue
            val imgUrl = imgObj.optString("url", null)
            images += TmImage(url = imgUrl)
        }
    }

    // --- dates ---
    val datesObj = root.optJSONObject("dates")
    val startObj = datesObj?.optJSONObject("start")
    val statusObj = datesObj?.optJSONObject("status")

    val start = if (startObj != null) {
        TmStart(
            localDate = startObj.optString("localDate", null),
            localTime = startObj.optString("localTime", null),
            dateTime = startObj.optString("dateTime", null)
        )
    } else null

    val status = if (statusObj != null) {
        TmStatus(
            code = statusObj.optString("code", null)
        )
    } else null

    val dates = if (start != null || status != null) {
        TmDates(
            start = start,
            status = status
        )
    } else null

    // --- priceRanges ---
    val priceRangesJson = root.optJSONArray("priceRanges")
    val priceRanges = mutableListOf<TmPriceRange>()
    if (priceRangesJson != null) {
        for (i in 0 until priceRangesJson.length()) {
            val prObj = priceRangesJson.optJSONObject(i) ?: continue
            val min = if (prObj.has("min")) prObj.optDouble("min") else null
            val max = if (prObj.has("max")) prObj.optDouble("max") else null

            priceRanges += TmPriceRange(
                type = prObj.optString("type", null),
                currency = prObj.optString("currency", null),
                min = min,
                max = max
            )
        }
    }
    val priceRangesOrNull = if (priceRanges.isEmpty()) null else priceRanges

    // --- classifications ---
    val classificationsJson = root.optJSONArray("classifications")
    val classifications = mutableListOf<TmClassification>()
    if (classificationsJson != null) {
        for (i in 0 until classificationsJson.length()) {
            val cObj = classificationsJson.optJSONObject(i) ?: continue

            fun nameHolder(key: String): TmNameHolder? {
                val inner = cObj.optJSONObject(key) ?: return null
                val n = inner.optString("name", null)
                if (n == null) return null
                return TmNameHolder(name = n)
            }

            classifications += TmClassification(
                segment = nameHolder("segment"),
                genre = nameHolder("genre"),
                subGenre = nameHolder("subGenre"),
                type = nameHolder("type"),
                subType = nameHolder("subType")
            )
        }
    }
    val classificationsOrNull = if (classifications.isEmpty()) null else classifications

    // --- seatmap ---
    val seatmapObj = root.optJSONObject("seatmap")
    val seatmap = seatmapObj?.optString("staticUrl", null)?.let {
        TmSeatmap(staticUrl = it)
    }

    // --- _embedded: venues + attractions ---
    val embeddedObj = root.optJSONObject("_embedded")
    var embedded: TmEmbedded? = null

    if (embeddedObj != null) {
        // venues
        val venuesJson = embeddedObj.optJSONArray("venues")
        val venues = mutableListOf<TmVenue>()
        if (venuesJson != null) {
            for (i in 0 until venuesJson.length()) {
                val vObj = venuesJson.optJSONObject(i) ?: continue

                val addressObj = vObj.optJSONObject("address")
                val address = if (addressObj != null) {
                    TmVenueAddress(
                        line1 = addressObj.optString("line1", null)
                    )
                } else null

                val city = vObj.optJSONObject("city")?.let {
                    TmNameHolder(name = it.optString("name", null))
                }

                val state = vObj.optJSONObject("state")?.let {
                    TmStateInfo(
                        name = it.optString("name", null),
                        stateCode = it.optString("stateCode", null)
                    )
                }

                val country = vObj.optJSONObject("country")?.let {
                    TmNameHolder(name = it.optString("name", null))
                }

                val locationObj = vObj.optJSONObject("location")
                val location = if (locationObj != null) {
                    TmVenueLocation(
                        longitude = locationObj.optString("longitude", null),
                        latitude = locationObj.optString("latitude", null)
                    )
                } else null

                val boxOfficeObj = vObj.optJSONObject("boxOfficeInfo")
                val boxOfficeInfo = if (boxOfficeObj != null) {
                    TmBoxOfficeInfo(
                        phoneNumberDetail = boxOfficeObj.optString("phoneNumberDetail", null),
                        openHoursDetail = boxOfficeObj.optString("openHoursDetail", null)
                    )
                } else null

                val generalObj = vObj.optJSONObject("generalInfo")
                val generalInfo = if (generalObj != null) {
                    TmGeneralInfo(
                        generalRule = generalObj.optString("generalRule", null),
                        childRule = generalObj.optString("childRule", null)
                    )
                } else null

                venues += TmVenue(
                    name = vObj.optString("name", null),
                    url = vObj.optString("url", null),
                    address = address,
                    city = city,
                    state = state,
                    country = country,
                    location = location,
                    boxOfficeInfo = boxOfficeInfo,
                    generalInfo = generalInfo
                )
            }
        }

        // attractions
        val attractionsJson = embeddedObj.optJSONArray("attractions")
        val attractions = mutableListOf<TmAttraction>()
        if (attractionsJson != null) {
            for (i in 0 until attractionsJson.length()) {
                val aObj = attractionsJson.optJSONObject(i) ?: continue
                attractions += TmAttraction(
                    name = aObj.optString("name", null)
                )
            }
        }

        embedded = TmEmbedded(
            venues = if (venues.isEmpty()) null else venues,
            attractions = if (attractions.isEmpty()) null else attractions
        )
    }

    return EventDetails(
        id = id,
        name = name,
        url = url,
        ticketmasterUrl = url,      // reuse Ticketmaster URL
        images = images,
        dates = dates,
        priceRanges = priceRangesOrNull,
        classifications = classificationsOrNull,
        seatmap = seatmap,
        embedded = embedded
    )
}
