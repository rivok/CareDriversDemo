package com.hopskipdrive.caredriversdemo.screens.myrides.uimodels

import kotlinx.datetime.Instant

// My Rides list is composed of two different kinds of elements, which is more efficient for than
// nested RecyclerViews
sealed interface MyRidesItem

data class SectionHeader(
    val startsAt: Instant,
    val endsAt: Instant,
    val estimatedEarningsInCents: Long,
) : MyRidesItem

data class RideCard(
    val rideId: Long,
    val startsAt: Instant,
    val endsAt: Instant,
    val estimatedEarningsInCents: Long,
    val riderCount: Long,
    val boosterCount: Long,
    val waypointAddresses: List<String>,
) : MyRidesItem
