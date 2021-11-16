package com.hopskipdrive.caredriversdemo.screens.ridedetails

import com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels.RideDetails

// The state machine for the view for the Ride Details screen
sealed interface RideDetailsViewState {
    object Loading : RideDetailsViewState
    object FailedToLoad : RideDetailsViewState
    data class Loaded(val details: RideDetails) : RideDetailsViewState
}
