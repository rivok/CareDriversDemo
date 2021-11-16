package com.hopskipdrive.caredriversdemo.screens.myrides

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hopskipdrive.api.fetchMyRides
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.MyRidesItem
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.RideCard
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.SectionHeader
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.localDate
import com.hopskipdrive.models.Ride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class MyRidesViewModel : ViewModel() {
    companion object {
        private val loggingTag
            get() = MyRidesViewModel::class.java.canonicalName
    }

    val viewStateFlow: StateFlow<MyRidesViewState>
        get() = mutableViewStateFlow

    val viewRequestFlow: Flow<MyRidesViewRequest>
        get() = mutableViewRequestFlow

    fun refresh() {
        if (internalState !is InternalState.Loading) {
            viewModelScope.launch { callApi() }
        }
    }

    fun onRideCardClicked(rideCard: RideCard) {
        val ride = (internalState as? InternalState.Loaded)?.rides?.get(rideCard.rideId) ?: return
        viewModelScope.launch {
            mutableViewRequestFlow.emit(MyRidesViewRequest.NavigateToRideDetails(ride.id))
        }
    }

    private val mutableViewStateFlow = MutableStateFlow<MyRidesViewState>(MyRidesViewState.Loading)

    private val mutableViewRequestFlow = MutableSharedFlow<MyRidesViewRequest>(replay = 1)

    private var internalState: InternalState = InternalState.Loading
        set(value) {
            Log.d("HJTEST", "MyRides internal state = $value")
            mutableViewStateFlow.value = value.toViewState()
            field = value
        }

    init {
        viewModelScope.launch { callApi() }
    }

    private suspend fun callApi() {
        internalState = InternalState.Loading

        val rides = try {
            fetchMyRides()
        } catch (e: Exception) {
            Log.e(loggingTag, "Failed to fetch my rides from API", e)
            internalState = InternalState.FailedToLoad
            return
        }

        internalState = InternalState.Loaded(rides = rides.associateBy { it.id })
    }
}

// The finite state machine that powers MyRidesViewModel
private sealed interface InternalState {
    // Initial state, may transition to FailedToLoad or Loaded
    object Loading : InternalState

    // Fail state, may transition back to Loading (for retry)
    object FailedToLoad : InternalState

    // Data state, may transition back to Loading (for refresh)
    data class Loaded(val rides: Map<Long, Ride>) : InternalState
}

private fun InternalState.toViewState(): MyRidesViewState = when (this) {
    is InternalState.Loading -> MyRidesViewState.Loading
    is InternalState.FailedToLoad -> MyRidesViewState.FailedToLoad
    is InternalState.Loaded -> MyRidesViewState.Loaded(items = rides.values.toMyRidesItems())
}

// Group and sort rides by local date and build the sections
private fun Collection<Ride>.toMyRidesItems(): List<MyRidesItem> =
    groupBy { it.startsAt.localDate }.toSortedMap().values.flatMap { rides ->
        val sectionHeader = SectionHeader(
            startsAt = rides.earliestStartsAt,
            endsAt = rides.latestEndsAt,
            estimatedEarningsInCents = rides.totalEstimatedEarningsInCents,
        )
        val rideCards = rides.toRideCards()
        listOf(sectionHeader) + rideCards
    }

// Sort the rides by start time and build the cards
private fun Collection<Ride>.toRideCards(): List<RideCard> =
    sortedBy { it.startsAt }.map { ride ->
        RideCard(
            rideId = ride.id,
            startsAt = ride.startsAt,
            endsAt = ride.endsAt,
            estimatedEarningsInCents = ride.estimatedEarningsInCents,
            riderCount = ride.riderCount,
            boosterCount = ride.boosterCount,
            waypointAddresses = ride.waypointAddresses,
        )
    }

private val Collection<Ride>.earliestStartsAt: Instant
    get() = minOf { it.startsAt }

private val Collection<Ride>.latestEndsAt: Instant
    get() = maxOf { it.endsAt }

private val Collection<Ride>.totalEstimatedEarningsInCents: Long
    get() = fold(initial = 0L) { acc, ride -> acc + ride.estimatedEarningsInCents }

// Get the total number of riders by counting unique rider IDs between all waypoints
private val Ride.riderCount: Long
    get() = orderedWaypoints.flatMap { it.passengers }.distinctBy { it.id }.size.toLong()

// Get the total number of booster seats needed by counting the max needed between all waypoints
private val Ride.boosterCount: Long
    get() = orderedWaypoints.maxOf { waypoint ->
        waypoint.passengers.count { it.boosterSeat }.toLong()
    }

private val Ride.waypointAddresses: List<String>
    get() = orderedWaypoints.map { it.location.address }
