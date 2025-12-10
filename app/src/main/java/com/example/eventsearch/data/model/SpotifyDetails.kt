package com.example.eventsearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyDetails(
    @SerialName("artist")
    val artist: SpotifyArtist? = null,

    @SerialName("albums")
    val albums: List<SpotifyAlbum> = emptyList()
)

@Serializable
data class SpotifyArtist(
    @SerialName("name")
    val name: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("followers")
    val followers: Long? = null,

    @SerialName("popularity")
    val popularity: Int? = null,

    @SerialName("genres")
    val genres: List<String> = emptyList(),

    @SerialName("id")
    val id: String? = null,

    @SerialName("spotifyUrl")
    val spotifyUrl: String? = null
)

@Serializable
data class SpotifyAlbum(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null,

    @SerialName("releaseDate")
    val releaseDate: String? = null,

    @SerialName("totalTracks")
    val totalTracks: Int? = null,

    @SerialName("spotifyUrl")
    val spotifyUrl: String? = null
)
