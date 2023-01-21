package com.alf.legofy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "PixelInfoAdapterTAG"
class PixelInfoAdapter(private val context: Context,
                        private val pixels: MutableList<Pixel>,
                        private val currentPixelCount: MutableList<Int>,
                        private val totalPixelCount: MutableList<Int>,
                        private val onClick: (Int)->Unit)://todo: refer to LEGO website piece name
    RecyclerView.Adapter<PixelInfoAdapter.ItemViewHolder>() {


    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pixel: Button = view.findViewById(R.id.color)
        val pixelId: TextView = view.findViewById(R.id.color_code)
        val textInfo: TextView = view.findViewById(R.id.text_info)
        val countInfo: ProgressBar = view.findViewById(R.id.count_info)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.pixel_info_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.countInfo.progressDrawable.mutate()
        holder.pixel.setBackgroundColor(pixels[position].color)
        holder.pixelId.text = pixels[position].colorCode.toString()
        @Suppress("SetTextI18n")
        holder.textInfo.text = "${currentPixelCount[position]} / ${totalPixelCount[position]}"
        holder.countInfo.max = totalPixelCount[position]
        holder.countInfo.progress = currentPixelCount[position]

    }

    override fun getItemCount(): Int {
        return pixels.size
    }

}