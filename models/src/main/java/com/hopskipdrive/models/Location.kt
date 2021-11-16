package com.hopskipdrive.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Location(
    val address: String,
    @SerialName("lat") val latitude: Double,
    @SerialName("lng") val longitude: Double,
)
