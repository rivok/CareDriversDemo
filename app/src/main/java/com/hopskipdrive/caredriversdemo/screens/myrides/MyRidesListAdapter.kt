package com.hopskipdrive.caredriversdemo.screens.myrides

import android.content.res.Resources
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hopskipdrive.caredriversdemo.R
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.MyRidesItem
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.RideCard
import com.hopskipdrive.caredriversdemo.screens.myrides.uimodels.SectionHeader
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlin.formatCentsAsUsd
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlin.inflateWithRoot
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.formatLocalDate
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.formatLocalTime
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime.localDate

class MyRidesListAdapter(
    private val onRideCardClicked: (RideCard) -> Unit
) : ListAdapter<MyRidesItem, RecyclerView.ViewHolder>(DiffCallback) {
    companion object {
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_RIDE_CARD = 1
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is SectionHeader -> VIEW_TYPE_SECTION_HEADER
        is RideCard -> VIEW_TYPE_RIDE_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder(parent)
            VIEW_TYPE_RIDE_CARD -> RideCardViewHolder(parent, onRideCardClicked)
            else -> error("Unknown view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SectionHeader -> (holder as SectionHeaderViewHolder).bind(item)
            is RideCard -> (holder as RideCardViewHolder).bind(item)
        }
    }

    private class SectionHeaderViewHolder(
        parent: ViewGroup,
    ) : RecyclerView.ViewHolder(R.layout.item_my_rides_section_header.inflateWithRoot(parent)) {
        private val dateText: TextView
            get() = itemView.findViewById(R.id.date_text)
        private val startAtText: TextView
            get() = itemView.findViewById(R.id.start_at_text)
        private val endAtText: TextView
            get() = itemView.findViewById(R.id.end_at_text)
        private val estimatedEarningsText: TextView
            get() = itemView.findViewById(R.id.estimated_earnings_text)

        fun bind(sectionHeader: SectionHeader) {
            dateText.text = sectionHeader.startsAt.formatLocalDate()
            startAtText.text = sectionHeader.startsAt.formatLocalTime()
            endAtText.text = sectionHeader.endsAt.formatLocalTime()
            estimatedEarningsText.text = sectionHeader.estimatedEarningsInCents.formatCentsAsUsd()
        }
    }

    private class RideCardViewHolder(
        parent: ViewGroup,
        private val onRideCardClicked: (RideCard) -> Unit,
    ) : RecyclerView.ViewHolder(R.layout.item_my_rides_ride_card.inflateWithRoot(parent)) {
        private val startAtText: TextView
            get() = itemView.findViewById(R.id.start_at_text)
        private val endAtText: TextView
            get() = itemView.findViewById(R.id.end_at_text)
        private val riderBoosterText: TextView
            get() = itemView.findViewById(R.id.rider_booster_text)
        private val estimatedEarningsText: TextView
            get() = itemView.findViewById(R.id.estimated_earnings_text)
        private val addressListText: TextView
            get() = itemView.findViewById(R.id.address_list_text)

        fun bind(rideCard: RideCard) {
            itemView.setOnClickListener { onRideCardClicked(rideCard) }
            startAtText.text = rideCard.startsAt.formatLocalTime()
            endAtText.text = rideCard.endsAt.formatLocalTime()
            riderBoosterText.text =
                itemView.resources.getRiderBoosterString(rideCard.riderCount, rideCard.boosterCount)
            estimatedEarningsText.text = rideCard.estimatedEarningsInCents.formatCentsAsUsd()
            addressListText.text = itemView.resources.getOrderedListString(rideCard.waypointAddresses)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MyRidesItem>() {
        override fun areItemsTheSame(
            oldItem: MyRidesItem,
            newItem: MyRidesItem
        ) = when {
            oldItem is RideCard && newItem is RideCard ->
                oldItem.rideId == newItem.rideId
            oldItem is SectionHeader && newItem is SectionHeader ->
                oldItem.startsAt.localDate == newItem.startsAt.localDate
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: MyRidesItem,
            newItem: MyRidesItem
        ) = oldItem == newItem
    }
}

private fun Resources.getRiderBoosterString(riderCount: Long, boosterCount: Long): String {
    val riderString = getQuantityString(R.plurals.rider, riderCount.toInt(), riderCount)
    val riderBoosterString = if (boosterCount == 0L) {
        riderString
    } else {
        val boosterString = getQuantityString(R.plurals.booster, boosterCount.toInt(), boosterCount)
        getString(R.string.rider_booster_format, riderString, boosterString)
    }
    return getString(R.string.parenthesized, riderBoosterString)
}

private fun Resources.getOrderedListString(strings: List<String>) =
    strings.mapIndexed { index, string ->
        // add 1 for human friendly numbering
        index + 1 to string
    }.joinToString(getString(R.string.ordered_list_item_separator)) { (number, string) ->
        getString(R.string.ordered_list_item, number, string)
    }
