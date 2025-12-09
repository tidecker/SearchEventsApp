package com.example.eventsearch.data.remote

import com.example.eventsearch.data.model.FavoriteEvent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL =
    "https://events-assignment-3-827999363029.us-west1.run.app/"

private val json = Json {
    ignoreUnknownKeys = true
}

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()


@kotlinx.serialization.Serializable
data class RemoveFavoriteRequest(val eventId: String)
interface FavoritesApiService {
    @GET("api/favorites")
    suspend fun getFavorites(): List<FavoriteEvent>

    @POST("api/post/favorite")
    suspend fun addFavorite(@Body favorite: FavoriteEvent)

    @POST("api/post/remove-favorite")
    suspend fun removeFavorite(@Body body: RemoveFavoriteRequest)
}

object FavoritesApi {
    val retrofitService: FavoritesApiService by lazy {
        retrofit.create(FavoritesApiService::class.java)
    }
}
