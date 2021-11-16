package com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels

data class WaypointDetailItem(
    val anchorType: AnchorType,
    val address: String,
) {
    enum class AnchorType { PICKUP, DROPOFF }
}
