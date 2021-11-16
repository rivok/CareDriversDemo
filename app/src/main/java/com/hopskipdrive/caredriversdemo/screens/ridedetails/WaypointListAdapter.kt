package com.hopskipdrive.caredriversdemo.screens.ridedetails

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hopskipdrive.caredriversdemo.R
import com.hopskipdrive.caredriversdemo.screens.ridedetails.uimodels.WaypointDetailItem
import com.hopskipdrive.caredriversdemo.utils.extensions.kotlin.inflateWithRoot

class WaypointListAdapter : ListAdapter<WaypointDetailItem, WaypointListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        parent: ViewGroup,
    ) : RecyclerView.ViewHolder(R.layout.item_waypoint.inflateWithRoot(parent)) {
        private val anchorIcon: ImageView
            get() = itemView.findViewById(R.id.anchor_icon)
        private val anchorLabel: TextView
            get() = itemView.findViewById(R.id.anchor_label)
        private val addressText: TextView
            get() = itemView.findViewById(R.id.address_text)

        fun bind(waypointDetailItem: WaypointDetailItem) {
            val drawableRes = when (waypointDetailItem.anchorType) {
                WaypointDetailItem.AnchorType.PICKUP -> R.drawable.ic_pickup
                WaypointDetailItem.AnchorType.DROPOFF -> R.drawable.ic_dropoff
            }
            anchorIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context, drawableRes))
            val stringRes = when (waypointDetailItem.anchorType) {
                WaypointDetailItem.AnchorType.PICKUP -> R.string.pickup
                WaypointDetailItem.AnchorType.DROPOFF -> R.string.dropoff
            }
            anchorLabel.text = itemView.context.getString(stringRes)
            addressText.text = waypointDetailItem.address
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<WaypointDetailItem>() {
        override fun areItemsTheSame(
            oldItem: WaypointDetailItem,
            newItem: WaypointDetailItem
        ) = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: WaypointDetailItem,
            newItem: WaypointDetailItem
        ) = oldItem == newItem
    }
}
