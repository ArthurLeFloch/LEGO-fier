package com.alf.legofy

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "FileAdapterTag"
class PixelAdapter(private val context: Context,
                   private val dataset: MutableList<Pixel>,
                   private val onClick: (Int)->Unit):
    RecyclerView.Adapter<PixelAdapter.ItemViewHolder>() {

    companion object {
        var itemSize: Int = 10
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.button)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.button_layout, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.button.setOnClickListener { onClick(position) }
        val tmp = LinearLayout.LayoutParams(itemSize, itemSize)
        //tmp.setMargins(3)
        holder.button.layoutParams = tmp

        if(dataset[position].enabled) {
            holder.button.setBackgroundColor(dataset[position].color)
            holder.button.text = ""
        } else {
            holder.button.setBackgroundColor(Color.rgb(55,40,38))
            holder.button.text = dataset[position].colorCode.toString()
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}