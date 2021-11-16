package com.hopskipdrive.caredriversdemo.screens.ridedetails

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hopskipdrive.caredriversdemo.R
import com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels.RideDetails
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlin.formatCentsAsUsd
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.formatLocalDate
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.formatLocalTime
import kotlinx.coroutines.flow.collect
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.properties.Delegates.observable

class RideDetailsFragment : Fragment(R.layout.fragment_ride_details) {
    private val viewModel: RideDetailsViewModel by viewModels {
        val args: RideDetailsFragmentArgs by navArgs()
        RideDetailsViewModel.Factory(rideId = args.rideId)
    }
    private var viewState: RideDetailsViewState by observable(RideDetailsViewState.Loading) { _, old, new ->
        onStateUpdated(old, new)
    }

    private val recyclerViewAdapter = WaypointListAdapter()

    private lateinit var progressView: View
    private lateinit var failView: View
    private lateinit var swipeRefreshView: SwipeRefreshLayout
    private lateinit var dateText: TextView
    private lateinit var startsAtText: TextView
    private lateinit var endsAtText: TextView
    private lateinit var estimatedEarningsText: TextView
    private lateinit var mapView: MapView
    private lateinit var seriesLabel: View
    private lateinit var seriesSpacer: View
    private lateinit var statsText: TextView

    init {
        lifecycleScope.launchWhenResumed { viewModel.viewStateFlow.collect(::viewState::set) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance()
            .load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressView = view.findViewById<View>(R.id.progress_bar).apply {
            isVisible = true
        }
        failView = view.findViewById<View>(R.id.fail_layout).apply {
            isVisible = false
        }
        view.findViewById<View>(R.id.retry_button).setOnClickListener { viewModel.refresh() }
        swipeRefreshView = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_view).apply {
            setOnRefreshListener {
                viewModel.refresh()
            }
            isVisible = false
        }
        dateText = view.findViewById(R.id.date_text)
        startsAtText = view.findViewById(R.id.start_at_text)
        endsAtText = view.findViewById(R.id.end_at_text)
        estimatedEarningsText = view.findViewById(R.id.estimated_earnings_text)
        mapView = view.findViewById<MapView>(R.id.map_view).apply {
            isTilesScaledToDpi = true
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            // the map won't zoom properly if the zoom is set before first layout
            addOnFirstLayoutListener { _, _, _, _, _ ->
                (viewModel.viewStateFlow.value as? RideDetailsViewState.Loaded)?.details?.let(::update)
            }
        }
        seriesLabel = view.findViewById(R.id.series_label)
        seriesSpacer = view.findViewById(R.id.series_spacer)
        view.findViewById<RecyclerView>(R.id.waypoint_recycler).apply {
            adapter = recyclerViewAdapter
        }
        statsText = view.findViewById(R.id.stats_text)
        view.findViewById<View>(R.id.cancel_button).setOnClickListener {
            Toast.makeText(requireContext(), R.string.no, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        onStateUpdated(null, viewModel.viewStateFlow.value)
    }

    private fun onStateUpdated(oldState: RideDetailsViewState?, newState: RideDetailsViewState) {
        val isRefreshing =
            oldState is RideDetailsViewState.Loaded && newState is RideDetailsViewState.Loading

        progressView.isVisible = newState is RideDetailsViewState.Loading && !isRefreshing
        failView.isVisible = newState is RideDetailsViewState.FailedToLoad
        swipeRefreshView.isVisible = newState is RideDetailsViewState.Loaded || isRefreshing

        val details = (newState as? RideDetailsViewState.Loaded)?.details ?: return
        swipeRefreshView.isRefreshing = false

        dateText.text = details.startsAt.formatLocalDate()
        startsAtText.text = details.startsAt.formatLocalTime()
        endsAtText.text = details.endsAt.formatLocalTime()
        estimatedEarningsText.text = details.estimatedEarningsCents.formatCentsAsUsd()

        mapView.update(details)

        seriesLabel.isVisible = details.inSeries
        recyclerViewAdapter.submitList(details.waypointDetailItems)
        statsText.text = getString(
            R.string.stats, details.rideId, details.estimatedRideMiles, details.estimatedRideMinutes)
    }
}

private fun MapView.update(details: RideDetails) {
    overlays.clear()
    val startPoint = GeoPoint(details.startLatitude, details.startLongitude)
    val endPoint = GeoPoint(details.endLatitude, details.endLongitude)
    overlays.add(getLocationPin(startPoint, R.color.map_start))
    overlays.add(getLocationPin(endPoint, R.color.map_end))
    zoomToBoundingBox(getMapBoundingBox(startPoint, endPoint), false)
    invalidate()
}

private fun MapView.getLocationPin(point: GeoPoint, @ColorRes colorRes: Int): Marker =
    Marker(this).apply {
        position = point
        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)?.apply {
            setTint(ContextCompat.getColor(context, colorRes))
        }
    }

// we want to zoom out a bit from the points of interest so that they are fully within view
private fun getMapBoundingBox(startPoint: GeoPoint, endPoint: GeoPoint): BoundingBox {
    val multiplier = 1.8
    val box = BoundingBox.fromGeoPoints(listOf(startPoint, endPoint))
    val latitudeOffset = box.latitudeSpan * multiplier / 2.0
    val longitudeOffset = box.longitudeSpanWithDateLine * multiplier / 2.0
    val firstCorner = GeoPoint(box.centerLatitude - latitudeOffset, box.centerLongitude - longitudeOffset)
    val secondCorner = GeoPoint(box.centerLatitude + latitudeOffset, box.centerLongitude + longitudeOffset)
    return BoundingBox.fromGeoPoints(listOf(firstCorner, secondCorner))
}
