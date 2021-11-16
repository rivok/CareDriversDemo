package com.hopskipdrive.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Ride(
    @SerialName("trip_id") val id: Long,
    @SerialName("in_series") val inSeries: Boolean,
    @SerialName("starts_at") val startsAt: Instant,
    @SerialName("ends_at") val endsAt: Instant,
    @SerialName("estimated_earnings_cents") val estimatedEarningsInCents: Long,
    @SerialName("estimated_ride_minutes") val estimatedRideMinutes: Long,
    @SerialName("estimated_ride_miles") val estimatedRideMiles: Double,
    @SerialName("ordered_waypoints") val orderedWaypoints: List<Waypoint>,
)
