package com.agrosense.app.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agrosense.app.R
import com.agrosense.app.domain.entity.Measurement
import java.lang.StringBuilder

class MeasurementsAdapter(
    var data: List<Measurement>,
    private val onItemClick: (Measurement) -> Unit
) : RecyclerView.Adapter<MeasurementsAdapter.ViewHolder>() {

    class ViewHolder(view: View, onItemClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val measurementNameText: TextView = view.findViewById(R.id.measurementNameTextView)
        private val startEndText: TextView = view.findViewById(R.id.measurementStartEndTextView)
        private val icon: ImageView = view.findViewById(R.id.iconImageView)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(model: Measurement, drawable: Drawable) {
            measurementNameText.text = model.name
            startEndText.text =
                StringBuilder().append(model.start.toString()).append(" ")
                    .append(model.end?.toString())
            icon.setImageDrawable(drawable)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.measurement_item, parent, false)
        return ViewHolder(viewHolder) {
            onItemClick(data[it])
        }
    }

    override fun getItemCount() =
        data.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val measurement = data[position]
        holder.bind(measurement, determineIconDrawable(measurement, holder.itemView))

    }

    fun updateDataSet(devices: List<Measurement>) {
        val oldData = data
        data + devices
        notifyItemRangeInserted(oldData.size - 1, devices.size)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun determineIconDrawable(measurement: Measurement, view: View): Drawable =
        measurement.end?.let {
            return view.resources.getDrawable(
                R.drawable.baseline_hourglass_bottom_24,
                view.context.theme
            )
        } ?: view.resources.getDrawable(R.drawable.baseline_check_24, view.context.theme)

}