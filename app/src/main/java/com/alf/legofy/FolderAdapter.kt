package com.alf.legofy

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

private const val TAG = "FileAdapterTag"
class FileAdapter(private val context: Context,
                  private val recyclerView: RecyclerView,
                  private val dataset: MutableList<Folder>,
                  private val onClick: (Int)->Unit):
    RecyclerView.Adapter<FileAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.file_name)
        val format: TextView = view.findViewById(R.id.format)
        val preview: ImageView = view.findViewById(R.id.preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_layout, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val maxWidth = context.resources.displayMetrics.widthPixels - 200
        val maxHeight = 600
        holder.preview.setImageBitmap(resizeBitmap(dataset[position].bitmap, maxWidth, maxHeight))
        @Suppress("SetTextI18n")
        holder.format.text = "${dataset[position].width} X ${dataset[position].height}"
        holder.fileName.text = dataset[position].name
        holder.itemView.setOnClickListener { onClick(position) }
        holder.itemView.setOnLongClickListener { onLongClick(position) }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    private fun onLongClick(position: Int) : Boolean {
        val path = context.filesDir
        val file = File(path, dataset[position].name)

        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Suppression du dossier")
        alertDialogBuilder.setMessage("Voulez-vous vraiment supprimer le dossier ${dataset[position].name} ?")
        alertDialogBuilder.setPositiveButton("Supprimer") { _: DialogInterface, _: Int ->
            file.deleteRecursively()
            Toast.makeText(context, "Fichier ${dataset[position].name} supprimé", Toast.LENGTH_SHORT).show()
            dataset.removeAt(position)
            recyclerView.adapter?.notifyDataSetChanged()
        }
        alertDialogBuilder.setNegativeButton("Annuler") { _: DialogInterface, _: Int -> }
        alertDialogBuilder.create().show()
        return true
    }

    private fun resizeBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var height = maxHeight
        var width = (height * source.width.toDouble() / source.height.toDouble()).toInt()
        if(width > maxWidth){
            width = maxWidth
            height = (width * source.height.toDouble() / source.width.toDouble()).toInt()
        }
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

}