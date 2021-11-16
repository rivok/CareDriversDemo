package com.hopskipdrive.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Passenger(
    val id: Long,
    @SerialName("booster_seat") val boosterSeat: Boolean,
    @SerialName("first_name") val firstName: String,
)
