package com.hopskipdrive.caredriversdemo.screens.myrides

import com.hopskipdrive.models.Ride

// A set of requests that the MyRidesViewModel can make to the view
sealed interface MyRidesViewRequest {
    data class NavigateToRideDetails(val rideId: Long) : MyRidesViewRequest
}
