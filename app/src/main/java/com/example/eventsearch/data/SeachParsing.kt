package com.example.eventsearch.data

import com.example.eventsearch.data.model.SearchEvent
import com.example.eventsearch.data.model.Seatmap
import org.json.JSONObject
import java.time.*

import java.time.format.DateTimeFormatter

fun parseSearchEvents(json: String): List<SearchEvent> {
    val root = JSONObject(json)

    if (!root.has("_embedded")) return emptyList()
    val events = root.getJSONObject("_embedded").getJSONArray("events")

    val list = mutableListOf<SearchEvent>()

    for (i in 0 until events.length()) {
        val ev = events.getJSONObject(i)

        val id = ev.optString("id", "")
        if (id.isEmpty()) continue

        val name = ev.optString("name", "")

        val classifications = ev.optJSONArray("classifications")
        val categoryLabel = classifications
            ?.optJSONObject(0)
            ?.optJSONObject("segment")
            ?.optString("name")
            ?: "—"

        val start = ev.optJSONObject("dates")
            ?.optJSONObject("start")
        val localDate = start?.optString("localDate", null)
        val localTime = start?.optString("localTime", null)

        val (dateTimeLabel, sortKey) = formatEventDate(localDate, localTime)

        val images = ev.optJSONArray("images")
        val imageUrl = if (images != null && images.length() > 0) {
            images.optJSONObject(0)?.optString("url") ?: ""
        } else ""

        val venueName = ev.optJSONObject("_embedded")
            ?.optJSONArray("venues")
            ?.optJSONObject(0)
            ?.optString("name")
            ?: "—"

        val seatmapStaticUrl = ev.optJSONObject("seatmap")?.optString("staticUrl") ?: ""
        val seatmap = Seatmap(seatmapStaticUrl)

        list.add(
            SearchEvent(
                id = id,
                name = name,
                categoryLabel = categoryLabel,
                dateTimeLabel = dateTimeLabel,
                imageUrl = imageUrl,
                venueName = venueName,
                sortKey = sortKey,
                seatmap = seatmap
        ))
    }

    // sort ascending by true date/time (like the JSX does)
    return list.sortedWith(
        compareBy<SearchEvent> { it.sortKey == null }.thenBy { it.sortKey }
    )
}

private fun formatEventDate(
    localDate: String?,
    localTime: String?
): Pair<String, LocalDateTime?> {
    if (localDate == null) return "" to null

    val date = LocalDate.parse(localDate)
    val time = if (localTime != null) LocalTime.parse(localTime) else LocalTime.MIDNIGHT
    val dt = LocalDateTime.of(date, time)

    val pattern =
        if (localTime != null) "MMM d, yyyy, h:mm a"
        else "MMM d, yyyy"

    val formatter = DateTimeFormatter.ofPattern(pattern)
    return dt.format(formatter) to dt
}
