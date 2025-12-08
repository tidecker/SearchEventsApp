package com.example.eventsearch.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

data class LatLng(val lat: Double, val lng: Double)

private val httpClient = OkHttpClient()

suspend fun fetchAutoLocation(): LatLng = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("https://ipinfo.io/json?token=7aae794b33679a")
        .build()

    httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Failed to get location: ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty body")
        val json = JSONObject(body)
        val loc = json.getString("loc")           // "lat,lng"
        val parts = loc.split(",")

        if (parts.size != 2) throw Exception("Invalid loc format")

        val lat = parts[0].toDouble()
        val lng = parts[1].toDouble()
        LatLng(lat, lng)
    }
}

suspend fun geocodeLocation(address: String): LatLng = withContext(Dispatchers.IO) {
    val encodedAddress = URLEncoder.encode(address, "UTF-8")
    val url =
        "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=AIzaSyACnOgTCfnLXpO6-eeFAtk4j8ttVomjfjc"

    val request = Request.Builder()
        .url(url)
        .build()

    httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Failed to geocode location: ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty body")
        val json = JSONObject(body)

        val status = json.optString("status")
        val results = json.optJSONArray("results")

        if (status != "OK" || results == null || results.length() == 0) {
            throw Exception("Failed to geocode location: status=$status")
        }

        val location = results.getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONObject("location")

        val lat = location.getDouble("lat")
        val lng = location.getDouble("lng")
        LatLng(lat, lng)
    }
}
