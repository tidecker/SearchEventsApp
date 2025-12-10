package com.example.eventsearch.data

import com.example.eventsearch.data.model.SpotifyAlbum
import com.example.eventsearch.data.model.SpotifyArtist
import com.example.eventsearch.data.model.SpotifyDetails
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

/**
 * Parse the Spotify JSON returned by your backend into a SpotifyArtistInfo.
 * Returns null if we can't find any artist info.
 */
fun parseSpotifyInfo(json: String, fallbackName: String): SpotifyDetails? {
    val trimmed = json.trim()
    if (trimmed.startsWith("<!doctype", true) || trimmed.startsWith("<html", true)) {
        // got HTML, not JSON
        return null
    }
    val root = JSONObject(json)

    // ----- artist -----
    val artistObj: JSONObject? = when {
        root.has("artist") -> root.optJSONObject("artist")
        root.has("artists") && root.get("artists") is JSONArray -> {
            val arr = root.optJSONArray("artists")
            if (arr != null && arr.length() > 0) arr.optJSONObject(0) else null
        }
        else -> null
    }

    if (artistObj == null) return null

    val name = artistObj.optString("name", fallbackName.ifBlank { "Unknown Artist" })

    // image: either flattened "imageUrl" or Spotify-style images[0].url
    val imageUrl = artistObj.optString("imageUrl", null)
        ?: artistObj.optJSONArray("images")?.optJSONObject(0)?.optString("url", null)

    // followers: handle "followersText", "followers" (int), or Spotify-style { total: ... }
    val followersNumber: Long? = when {
        artistObj.has("followersText") -> {
            // backend might already give formatted text; leave raw, will skip formatting below
            null
        }
        artistObj.has("followers") && artistObj.get("followers") !is JSONObject ->
            artistObj.optLong("followers")
        artistObj.has("followers") && artistObj.get("followers") is JSONObject ->
            artistObj.optJSONObject("followers")?.optLong("total")
        else -> null
    }

    val genresList: List<String> = when {
        // Proper Spotify-style array: ["soft pop", "uk pop", ...]
        artistObj.has("genres") && artistObj.get("genres") is JSONArray -> {
            val arr = artistObj.optJSONArray("genres")
            (0 until (arr?.length() ?: 0)).mapNotNull { i ->
                arr?.optString(i)?.takeIf { it.isNotBlank() }
            }
        }
        // Fallback: single comma-separated string
        artistObj.has("genres") -> {
            artistObj.optString("genres")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        else -> emptyList()
    }

    // popularity 0–100
    val popularity = artistObj.optInt("popularity", 0).coerceIn(0, 100)

    // spotify URL: either flattened or Spotify-style external_urls.spotify
    val spotifyUrl = artistObj.optString("spotifyUrl", null)
        ?: artistObj.optJSONObject("external_urls")?.optString("spotify", null)

    // ----- albums -----
    val albumsArray: JSONArray? = when {
        root.has("albums") && root.get("albums") is JSONArray ->
            root.optJSONArray("albums")
        root.has("albums") && root.get("albums") is JSONObject ->
            root.optJSONObject("albums")?.optJSONArray("items")
        else -> null
    }

    val albums = mutableListOf<SpotifyAlbum>()
    if (albumsArray != null) {
        for (i in 0 until albumsArray.length()) {
            val aObj = albumsArray.optJSONObject(i) ?: continue

            val albumId = aObj.optString("id", "")
            val albumName = aObj.optString("name", "Unknown Album")

            val albumImageUrl = aObj.optString("imageUrl", null)
                ?: aObj.optJSONArray("images")?.optJSONObject(0)?.optString("url", null)

            val releaseDate = aObj.optString(
                "releaseDate",
                aObj.optString("release_date", null)
            )

            val totalTracks = when {
                aObj.has("totalTracks") -> aObj.optInt("totalTracks")
                aObj.has("total_tracks") -> aObj.optInt("total_tracks")
                else -> null
            }?.takeIf { it > 0 }

            val albumSpotifyUrl = aObj.optString("spotifyUrl", null)
                ?: aObj.optJSONObject("external_urls")?.optString("spotify", null)

            albums += SpotifyAlbum(
                id = albumId,
                name = albumName,
                imageUrl = albumImageUrl,
                releaseDate = releaseDate,
                totalTracks = totalTracks,
                spotifyUrl = albumSpotifyUrl
            )
        }
    }

    val spotifyArtist = SpotifyArtist(
        name = name,
        imageUrl = imageUrl,
        followers = followersNumber,
        popularity = popularity,
        genres = genresList.ifEmpty { listOf("N/A") },
        id = null,
        spotifyUrl = spotifyUrl
    )

    return SpotifyDetails(
        artist = spotifyArtist,
        albums = albums
    )
}

private fun formatFollowers(value: Long): String {
    val nf = NumberFormat.getIntegerInstance(Locale.US)
    return nf.format(value)
}
