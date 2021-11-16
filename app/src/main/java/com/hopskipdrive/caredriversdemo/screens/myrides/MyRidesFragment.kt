package com.hopskipdrive.caredriversdemo.screens.myrides

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hopskipdrive.caredriversdemo.R
import kotlinx.coroutines.flow.collect
import kotlin.properties.Delegates.observable

class MyRidesFragment : Fragment(R.layout.fragment_my_rides) {
    private val viewModel: MyRidesViewModel by viewModels()
    private var viewState: MyRidesViewState by observable(MyRidesViewState.Loading) { _, old, new ->
        onStateUpdated(old, new)
    }

    private val recyclerViewAdapter = MyRidesListAdapter { viewModel.onRideCardClicked(it) }

    private lateinit var progressView: View
    private lateinit var failView: View
    private lateinit var swipeRefreshView: SwipeRefreshLayout

    init {
        lifecycleScope.launchWhenResumed { viewModel.viewStateFlow.collect(::viewState::set) }
        lifecycleScope.launchWhenResumed { viewModel.viewRequestFlow.collect(::onRequestMade) }
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
        view.findViewById<RecyclerView>(R.id.my_rides_recycler).apply {
            adapter = recyclerViewAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        onStateUpdated(null, viewModel.viewStateFlow.value)
    }

    private fun onStateUpdated(oldState: MyRidesViewState?, newState: MyRidesViewState) {
        val isRefreshing =
            oldState is MyRidesViewState.Loaded && newState is MyRidesViewState.Loading

        progressView.isVisible = newState is MyRidesViewState.Loading && !isRefreshing
        failView.isVisible = newState is MyRidesViewState.FailedToLoad
        swipeRefreshView.isVisible = newState is MyRidesViewState.Loaded || isRefreshing

        val loaded = newState as? MyRidesViewState.Loaded ?: return
        swipeRefreshView.isRefreshing = false
        recyclerViewAdapter.submitList(loaded.items)
    }

    private fun onRequestMade(request: MyRidesViewRequest) {
        when (request) {
            is MyRidesViewRequest.NavigateToRideDetails -> view?.findNavController()
                ?.navigate(MyRidesFragmentDirections.fragmentMyRidesToFragmentRideDetail(request.rideId))
        }
    }
}
