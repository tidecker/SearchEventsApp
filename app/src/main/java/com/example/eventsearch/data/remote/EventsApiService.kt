package com.example.eventsearch.data.remote

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL =
    "https://events-assignment-3-827999363029.us-west1.run.app/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(ScalarsConverterFactory.create())
    .build()

interface EventsApiService {
    @GET("api/events")
    suspend fun searchEvents(
        @Query("keyword") keyword: String,
        @Query("category") category: String,
        @Query("distance") distance: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): String   // raw JSON for now
}

object EventsApi {
    val retrofitService: EventsApiService by lazy {
        retrofit.create(EventsApiService::class.java)
    }
}
