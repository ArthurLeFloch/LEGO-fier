package com.alf.legofy

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.alf.legofy.databinding.ActivityAdjustSizeBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Integer.max
import java.lang.Integer.min

private const val TAG = "AdjustSizeActivityTAG"
class AdjustSize : AppCompatActivity() {
    private lateinit var binding: ActivityAdjustSizeBinding
    private lateinit var defaultBitmap: Bitmap
    private lateinit var folderName: String
    private lateinit var newBitmap: Bitmap

    private var defaultWidth = 0
    private var defaultHeight = 0

    private var startX = 0
    private var startY = 0
    private var endX = 0
    private var endY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdjustSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        folderName = intent.getStringExtra("folderName")!!
        val path = this.filesDir.absolutePath + "/" + folderName

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        defaultBitmap = BitmapFactory.decodeFile("$path/$folderName.png", options)

        endX = defaultBitmap.width
        endY = defaultBitmap.height
        defaultWidth = defaultBitmap.width
        defaultHeight = defaultBitmap.height

        update()

        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = folderName

        binding.addTop.setOnClickListener { addTop() }
        binding.addRight.setOnClickListener { addRight() }
        binding.addBottom.setOnClickListener { addBottom() }
        binding.addLeft.setOnClickListener { addLeft() }
        binding.addTop.setOnLongClickListener { resetTop() }
        binding.addRight.setOnLongClickListener { resetRight() }
        binding.addBottom.setOnLongClickListener { resetBottom() }
        binding.addLeft.setOnLongClickListener { resetLeft() }
        binding.removeTop.setOnClickListener { removeTop() }
        binding.removeRight.setOnClickListener { removeRight() }
        binding.removeBottom.setOnClickListener { removeBottom() }
        binding.removeLeft.setOnClickListener { removeLeft() }

        binding.validate.setOnClickListener { startNew() }

    }

    private fun update(){
        updateButtonColors()
        newBitmap = Bitmap.createBitmap(defaultBitmap, startX, startY, endX-startX, endY-startY)
        val shownBitmap = Bitmap.createScaledBitmap(newBitmap, newBitmap.width*4, newBitmap.height*4, false)
        binding.reducedBitmap.setImageBitmap(shownBitmap)
        binding.width.text = (endX-startX).toString()
        binding.height.text = (endY-startY).toString()
    }

    private fun updateButtonColors(){
        val red = Color.rgb(170,20,20)
        val normal = Color.rgb(200, 200, 200)
        Log.i(TAG, "($startX, $startY), ($endX, $endY)")

        if(startY == 0){
            binding.addTop.setBackgroundColor(red)
        } else {
            binding.addTop.setBackgroundColor(normal)
        }
        if(endX == defaultWidth){
            binding.addRight.setBackgroundColor(red)
        } else {
            binding.addRight.setBackgroundColor(normal)
        }
        if(endY == defaultHeight){
            binding.addBottom.setBackgroundColor(red)
        } else {
            binding.addBottom.setBackgroundColor(normal)
        }
        if(startX == 0){
            binding.addLeft.setBackgroundColor(red)
        } else {
            binding.addLeft.setBackgroundColor(normal)
        }

        if(startY + 1 == endY){
            binding.removeTop.setBackgroundColor(red)
            binding.removeBottom.setBackgroundColor(red)
        } else {
            binding.removeTop.setBackgroundColor(normal)
            binding.removeBottom.setBackgroundColor(normal)
        }
        if(startX + 1 == endX){
            binding.removeRight.setBackgroundColor(red)
            binding.removeLeft.setBackgroundColor(red)
        } else {
            binding.removeRight.setBackgroundColor(normal)
            binding.removeLeft.setBackgroundColor(normal)
        }
    }

    private fun addTop(){
        startY = max(startY - 1, 0)
        update()
    }
    private fun addRight(){
        endX = min(endX + 1, defaultWidth)
        update()
    }
    private fun addBottom(){
        endY = min(endY + 1, defaultHeight)
        update()
    }
    private fun addLeft(){
        startX = max(startX - 1, 0)
        update()
    }
    private fun removeTop(){
        startY = min(startY + 1, endY - 1)
        update()
    }
    private fun removeRight(){
        endX = max(endX - 1, startX + 1)
        update()
    }
    private fun removeBottom(){
        endY = max(endY - 1, startY + 1)
        update()
    }
    private fun removeLeft(){
        startX = min(startX + 1, endX - 1)
        update()
    }

    private fun resetTop(): Boolean{
        startY = 0
        update()
        return true
    }
    private fun resetRight(): Boolean{
        endX = defaultWidth
        update()
        return true
    }
    private fun resetBottom(): Boolean{
        endY = defaultHeight
        update()
        return true
    }
    private fun resetLeft(): Boolean{
        startX = 0
        update()
        return true
    }

    private fun startNew(){
        val dir = this.filesDir.absolutePath + '/' + folderName
        if(File(dir, "$folderName.png").exists())
        File(dir, "$folderName.png").delete()
        val output = FileOutputStream("$dir/$folderName.png")

        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        val intent = Intent(this, TilesActivity::class.java)
        intent.putExtra("folderName", folderName)
        intent.putExtra("isNew", true)
        Log.d(TAG, "Launching tiles activity")
        startActivity(intent)
        this.finish()
    }
}