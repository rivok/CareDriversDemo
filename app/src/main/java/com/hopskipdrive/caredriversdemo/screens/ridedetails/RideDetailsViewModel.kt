package com.hopskipdrive.caredriversdemo.screens.ridedetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hopskipdrive.api.fetchMyRides
import com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels.RideDetails
import com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels.WaypointDetailItem
import com.hopskipdrive.models.Ride
import com.hopskipdrive.models.Waypoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RideDetailsViewModel private constructor(private val rideId: Long) : ViewModel() {
    class Factory(private val rideId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RideDetailsViewModel(rideId) as T
    }

    companion object {
        private val loggingTag
            get() = RideDetailsViewModel::class.java.canonicalName
    }

    val viewStateFlow: StateFlow<RideDetailsViewState>
        get() = mutableViewStateFlow

    fun refresh() {
        if (internalState !is InternalState.Loading) {
            viewModelScope.launch { callApi() }
        }
    }

    private val mutableViewStateFlow = MutableStateFlow<RideDetailsViewState>(RideDetailsViewState.Loading)

    private var internalState: InternalState = InternalState.Loading
        set(value) {
            mutableViewStateFlow.value = value.toViewState()
            field = value
        }

    init {
        viewModelScope.launch { callApi() }
    }

    private suspend fun callApi() {
        internalState = InternalState.Loading

        val ride = try {
            fetchMyRides().find { it.id == rideId } ?: error("No ride with given ID")
        } catch (e: Exception) {
            Log.e(loggingTag, "Failed to fetch ride from API", e)
            internalState = InternalState.FailedToLoad
            return
        }

        internalState = InternalState.Loaded(ride)
    }
}

// The finite state machine that powers RideDetailsViewModel
private sealed interface InternalState {
    // Initial state, may transition to FailedToLoad or Loaded
    object Loading : InternalState

    // Fail state, may transition back to Loading (for retry)
    object FailedToLoad : InternalState

    // Data state, may transition back to Loading (for refresh)
    data class Loaded(val ride: Ride) : InternalState
}

private fun InternalState.toViewState(): RideDetailsViewState = when (this) {
    is InternalState.Loading -> RideDetailsViewState.Loading
    is InternalState.FailedToLoad -> RideDetailsViewState.FailedToLoad
    is InternalState.Loaded -> RideDetailsViewState.Loaded(details = ride.toRideDetails())
}

private fun Ride.toRideDetails(): RideDetails {
    val startWaypoint = orderedWaypoints.first()
    val endWaypoint = orderedWaypoints.last()
    return RideDetails(
        rideId = id,
        inSeries = inSeries,
        startsAt = startsAt,
        endsAt = endsAt,
        estimatedEarningsCents = estimatedEarningsInCents,
        estimatedRideMinutes = estimatedRideMinutes,
        estimatedRideMiles = estimatedRideMiles,
        startLatitude = startWaypoint.location.latitude,
        startLongitude = startWaypoint.location.longitude,
        endLatitude = endWaypoint.location.latitude,
        endLongitude = endWaypoint.location.longitude,
        waypointDetailItems = orderedWaypoints.map { it.toWaypointDetailItem() },
    )
}

private fun Waypoint.toWaypointDetailItem(): WaypointDetailItem =
    WaypointDetailItem(
        anchorType = if (anchor) WaypointDetailItem.AnchorType.PICKUP else WaypointDetailItem.AnchorType.DROPOFF,
        address = location.address,
    )
