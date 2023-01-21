package com.alf.legofy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView


private const val TAG = "ColorEnableAdapterTAG"
class ColorEnableAdapter(private val context: Context,
                         private val pixels: MutableList<Pixel>,
                         private val enabledColors: MutableList<Boolean>,
                       private val onClick: (Int)->Boolean):
    RecyclerView.Adapter<ColorEnableAdapter.ItemViewHolder>() {


    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pixel: Button = view.findViewById(R.id.color)
        val switch: CheckBox = view.findViewById(R.id.enabled)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.color_enabler_layout, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.pixel.setBackgroundColor(pixels[position].color)
        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.isChecked = enabledColors[position]
        holder.switch.setOnCheckedChangeListener { _, _ ->
            onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return pixels.size
    }

}