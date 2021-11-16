package com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels

import kotlinx.datetime.Instant

data class RideDetails(
    val rideId: Long,
    val inSeries: Boolean,
    val startsAt: Instant,
    val endsAt: Instant,
    val estimatedEarningsCents: Long,
    val estimatedRideMinutes: Long,
    val estimatedRideMiles: Double,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val waypointDetailItems: List<WaypointDetailItem>,
)
