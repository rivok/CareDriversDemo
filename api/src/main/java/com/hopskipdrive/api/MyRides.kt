package com.hopskipdrive.api

import com.hopskipdrive.models.Ride
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

private const val MY_RIDES_ENDPOINT =
    "https://storage.googleapis.com/hsd-interview-resources/simplified_my_rides_response.json"

private val httpClient: HttpClient
    get() = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

// The API wraps the array of rides in an object instead of just returning the array, hence this wrapper
@Serializable
private data class RidesWrapper(val rides: List<Ride>)

public suspend fun fetchMyRides(): List<Ride> = httpClient.use { client ->
    client.get<RidesWrapper>(MY_RIDES_ENDPOINT).rides
}
