package com.alf.legofy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf.legofy.databinding.ActivityTilesBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Integer.max
import java.lang.Integer.min
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "TilesActivityTAG"
class TilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTilesBinding
    private lateinit var pixelList: MutableList<Pixel>
    private lateinit var actualPixelList: MutableList<Pixel>
    private lateinit var folderName: String
    private lateinit var bitmap: Bitmap
    private lateinit var path: String
    private lateinit var dataFile: File

    private var isPixelInfoEnabled = true
    private var x = 0
    private var y = 0

    private var width = 0
    private var height = 0

    private var recyclerViewMaxWidth = 0

    private var totalCount = 0
    private var currentCount = 0
    private var pixelsList = mutableListOf<Pixel>()
    private var currentPixelCount = mutableListOf<Int>()
    private var totalPixelCount = mutableListOf<Int>()
    private var accessArray = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isNew = intent.getBooleanExtra("isNew", true)
        folderName = intent.getStringExtra("folderName")!!

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = folderName

        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        recyclerViewMaxWidth = displayMetrics.widthPixels-2*dpToPix(40) - dpToPix(32)

        togglePixelInfo()

        for(pixel in Pixel.lego_colors){
            pixelsList.add(Pixel(false, pixel.first, pixel.second, pixel.third))
            currentPixelCount.add(0)
            accessArray.add(0)
            totalPixelCount.add(0)
        }

        pixelList = mutableListOf()
        actualPixelList = mutableListOf()

        path = this.filesDir.absolutePath + "/" + folderName
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmap = BitmapFactory.decodeFile("$path/$folderName.png", options)
        width = bitmap.width
        height = bitmap.height
        val shownBitmap = Bitmap.createScaledBitmap(bitmap, width*4, height*4, false)
        binding.imageView.setImageBitmap(shownBitmap)

        dataFile = File(path, "$folderName.txt")
        if (isNew) {
            createBitmapPixelsFile(bitmap)
            setupInfoFile()
        }
        loadBitmapPixels()

        val layoutManagerPixels: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.pixelItems.layoutManager = layoutManagerPixels
        binding.pixelItems.adapter = PixelInfoAdapter(this, pixelsList, currentPixelCount, totalPixelCount) { }
        binding.pixelItems.setHasFixedSize(true)

        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, min(getSize(), width - x))
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = PixelAdapter(this, actualPixelList) {position -> switchPix(position)}

        updateRecyclerView()

        binding.imageView.setOnLongClickListener{
            onSave()
        }

        binding.zoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                updateRecyclerView(changeSize = true)
            }

            override fun onStartTrackingTouch(seek: SeekBar) { }

            override fun onStopTrackingTouch(seek: SeekBar) {
                updateRecyclerView(changeSize = true)
            }
        })

        binding.fill.setOnClickListener { fillHere() }

        binding.left.setOnClickListener{leftClick()}
        binding.right.setOnClickListener{rightClick()}
        binding.top.setOnClickListener{topClick()}
        binding.bottom.setOnClickListener{bottomClick()}

        binding.left.setOnLongClickListener{leftLongClick()}
        binding.right.setOnLongClickListener{rightLongClick()}
        binding.top.setOnLongClickListener{topLongClick()}
        binding.bottom.setOnLongClickListener{bottomLongClick()}

        binding.materialCardView2.setOnClickListener{ togglePixelInfo() }
    }

    private fun showPositionOnBitmap(){
        val size = getSize()
        val bx = min(x + size, width)
        val by = min(y + size, height)
        val pixelsArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val white = Color.rgb(255,255,255)
        val black = Color.rgb(0,0,0)
        for(i in x+1 until bx-1) {
            for (j in y+1 until by-1) {
                val position = i + j * width
                pixelsArray[position] = white
            }
        }
        for(i in x until bx){
            val topBar = i + y * width
            pixelsArray[topBar] = black
            val bottomBar = i + (by-1) * width
            pixelsArray[bottomBar] = black
        }
        for(j in y until by){
            val leftBar = x + j * width
            pixelsArray[leftBar] = black
            val rightBar = (bx-1) + j * width
            pixelsArray[rightBar] = black
        }
        val res: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        res.setPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        binding.imageView.setImageBitmap(res)
    }

    private fun togglePixelInfo(){
        isPixelInfoEnabled = !isPixelInfoEnabled
        if(isPixelInfoEnabled){
            val offset = -binding.cardPixels.width.toFloat()

            binding.showPixels.text = "Pixels ->"
            binding.cardPixels.animate().translationX(offset)

            binding.recyclerView.animate().translationX(offset)
            binding.top.animate().translationX(offset)
            binding.bottom.animate().translationX(offset)
            binding.left.animate().translationX(offset)
            binding.right.animate().translationX(offset)
            binding.x.animate().translationX(offset)
            binding.y.animate().translationX(offset)
        } else {
            binding.showPixels.text = "<- Pixels"

            binding.cardPixels.animate().translationX(0F)
            binding.recyclerView.animate().translationX(0F)

            binding.top.animate().translationX(0F)
            binding.bottom.animate().translationX(0F)
            binding.left.animate().translationX(0F)
            binding.right.animate().translationX(0F)
            binding.x.animate().translationX(0F)
            binding.y.animate().translationX(0F)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fillHere(){
        val size = getSize()
        val bx = min(x + size, width)
        val by = min(y + size, height)
        for(i in x until bx){
            for(j in y until by){
                val position = i + j*width
                val gridRelativePosition = (i-x) + (j-y)*size

                if(! pixelList[position].enabled){
                    currentPixelCount[accessArray[pixelList[position].colorCode]] += 1
                    currentCount += 1
                    pixelList[position].enabled = true
                    actualPixelList[gridRelativePosition].enabled = true
                    binding.pixelItems.adapter?.notifyItemChanged(accessArray[pixelList[position].colorCode])
                    binding.recyclerView.adapter?.notifyItemChanged(gridRelativePosition)
                }
            }
        }
        updateProgress()
    }

    private fun updateProgress(){
        val ratio = currentCount.toFloat() / totalCount.toFloat()
        binding.progress.progress = (ratio * 10000).toInt()
        @Suppress("SetTextI18n")
        binding.percent.text = "${(ratio*100).toInt()} %"
    }

    private fun updateRecyclerView(changeSize: Boolean = false){
        binding.x.text = (x.plus(1)).toString()
        binding.y.text = (y.plus(1)).toString()

        val size = getSize()
        PixelAdapter.itemSize = recyclerViewMaxWidth/size

        actualPixelList.clear()
        for(j in y until min(y+size, height)){
            for(i in x until min(x+size, width)){
                actualPixelList.add(getPix(i, j))
            }
        }
        updateButtonColors()
        showPositionOnBitmap()

        Log.d(TAG, "$size, ${width-x}")
        binding.recyclerView.layoutManager = GridLayoutManager(this, min(size, width - x))
        @Suppress("NotifyDataSetChanged")
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun pixToDp(px: Int): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return (px / displayMetrics.density).toInt()
    }

    private fun dpToPix(dp: Int): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return (dp * displayMetrics.density).toInt()
    }

    private fun updateButtonColors(){
        val red = Color.rgb(170,20,20)
        val normal = Color.rgb(200, 200, 200)
        if(x == 0){
            binding.left.setBackgroundColor(red)
        } else {
            binding.left.setBackgroundColor(normal)
        }
        if(y == 0){
            binding.top.setBackgroundColor(red)
        } else {
            binding.top.setBackgroundColor(normal)
        }
        if(x >= width-getSize()){
            binding.right.setBackgroundColor(red)
        } else {
            binding.right.setBackgroundColor(normal)
        }
        if(y >= height-getSize()){
            binding.bottom.setBackgroundColor(red)
        } else {
            binding.bottom.setBackgroundColor(normal)
        }
    }

    private fun getSize():Int{
        return 6 + (2-binding.zoom.progress)
    }

    private fun createBitmapPixelsFile(bitmap: Bitmap){
        var r: Int
        var g: Int
        var b: Int
        var tmp: String
        var xt:Int
        var yt:Int
        val pixelsArray = IntArray(width * height)
        bitmap.getPixels(pixelsArray, 0, width, 0, 0, width, height)
        for(k in pixelsArray.indices) {
            xt = k % width
            yt = k / width
            tmp = "0"
            val argb = bitmap.getPixel(xt, yt)
            r = Color.red(argb)
            g = Color.green(argb)
            b = Color.blue(argb)
            if (xt != 0) {
                tmp = "|0"
            }
            dataFile.appendText("$tmp,$r,$g,$b")
            if (yt != height - 1 && xt == width - 1) {
                dataFile.appendText("\n")
            }
        }
    }

    private fun loadBitmapPixels(){
        val lines = dataFile.readLines()
        for(line in lines){
            val pixels = line.split('|')
            for(pixel in pixels){
                val data = pixel.split(',')
                val p = Pixel(intToBool(data[0].toInt()), data[1].toInt(), data[2].toInt(), data[3].toInt())
                pixelList.add(p)
                totalPixelCount[p.colorCode] += 1
                totalCount += 1
                if(p.enabled){
                    currentPixelCount[p.colorCode] += 1
                    currentCount += 1
                }
            }
        }
        for(i in pixelsList.size-1 downTo 0){
            if(totalPixelCount[i] == 0){
                pixelsList.removeAt(i)
                currentPixelCount.removeAt(i)
                totalPixelCount.removeAt(i)
            }
        }
        Log.wtf(TAG, totalPixelCount.toString())
        for(i in pixelsList.indices){
            accessArray[pixelsList[i].colorCode] = i
        }

        updateProgress()

        @Suppress("notifyDataSetChanged")
        binding.pixelItems.adapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        saveBitmapPixels(File(path, "$folderName.txt"))
    }

    private fun getPix(xt:Int, yt:Int): Pixel{
        return pixelList[width * yt + xt]
    }

    private fun switchPix(gridRelativePosition:Int){
        val size = getSize()
        val xt = gridRelativePosition % min(size, width-x)
        val yt = gridRelativePosition / min(size, width-x)
        val trueX = xt + x
        val trueY = yt + y
        val position = trueX + trueY * width

        val state = !pixelList[position].enabled
        pixelList[position].enabled = state
        actualPixelList[gridRelativePosition].enabled = state

        if(pixelList[position].enabled){
            currentPixelCount[accessArray[pixelList[position].colorCode]] += 1
            currentCount += 1

        } else {
            currentPixelCount[accessArray[pixelList[position].colorCode]] -= 1
            currentCount -= 1
        }

        updateProgress()

        binding.pixelItems.adapter?.notifyItemChanged(accessArray[pixelList[position].colorCode])
        binding.recyclerView.adapter?.notifyItemChanged(gridRelativePosition)

        Log.d(TAG, "${position % width}, ${position / width}")

    }

    private fun boolToInt(bool: Boolean): Int{
        return if(bool){
            1
        } else {
            0
        }
    }

    private fun intToBool(i: Int): Boolean{
        return i == 1
    }

    private fun saveBitmapPixels(dataFile: File){
        var tmp: String
        dataFile.delete()
        for(yt in 0 until height){
            for(xt in 0 until width) {
                tmp = ""
                val pixel = getPix(xt, yt)
                if (xt != 0) {
                    tmp = "|"
                }
                dataFile.appendText("$tmp${boolToInt(pixel.enabled)},${pixel.r},${pixel.g},${pixel.b}")
            }
            if(yt != height-1){
                dataFile.appendText("\n")
            }
        }
        Log.i(TAG, "Fichier enregistré avec succès")
    }

    private fun setupInfoFile() {
        val file = File(path, "info.txt")
        file.delete()
        file.appendText("${width},${height}\n")
        Toast.makeText(this, "Dossier $folderName enregistré !", Toast.LENGTH_SHORT)
            .show()
    }

    private fun leftClick() {
        x = max(x-1, 0)
        updateRecyclerView()
    }
    private fun rightClick(){
        x = min(x+1, max(0,width-getSize()))
        updateRecyclerView()
    }
    private fun topClick(){
        y = max(y-1, 0)
        updateRecyclerView()
    }
    private fun bottomClick(){
        y = min(y+1,max(0,height-getSize()))
        updateRecyclerView()
    }

    private fun leftLongClick(): Boolean{
        x = 0
        updateRecyclerView()
        return true
    }
    private fun rightLongClick(): Boolean{
        x = max(width-getSize(),0)
        updateRecyclerView()
        return true
    }
    private fun topLongClick(): Boolean{
        y = 0
        updateRecyclerView()
        return true
    }
    private fun bottomLongClick(): Boolean{
        y = max(height-getSize(),0)
        updateRecyclerView()
        return true
    }

    private fun onSave(): Boolean{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "legofier_$timeStamp"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        Log.d(TAG, storageDir)
        val output = FileOutputStream("$storageDir/$imageFileName.png")

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width*10, bitmap.height*10, false)

        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        Toast.makeText(this, "Image enregistrée dans la galerie avec succès", Toast.LENGTH_SHORT).show()
        return true
    }

}