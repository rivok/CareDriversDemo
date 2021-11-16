package com.hopskipdrive.models

import kotlinx.serialization.Serializable

@Serializable
public data class Waypoint(
    val id: Long,
    val anchor: Boolean,
    val passengers: List<Passenger>,
    val location: Location,
)
