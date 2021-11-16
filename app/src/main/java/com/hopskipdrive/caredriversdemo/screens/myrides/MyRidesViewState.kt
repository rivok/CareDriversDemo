package com.hopskipdrive.caredriversdemo.screens.myrides

import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.MyRidesItem

// The state machine for the view for the My Rides screen
sealed interface MyRidesViewState {
    object Loading : MyRidesViewState
    object FailedToLoad : MyRidesViewState
    data class Loaded(val items: List<MyRidesItem>) : MyRidesViewState
}
