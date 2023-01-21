package com.alf.legofy

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf.legofy.Pixel.Companion.lego_colors
import com.alf.legofy.databinding.ActivityImageSetupBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

private const val TAG = "ImageSetupActivityTAG"
class ImageSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageSetupBinding
    private lateinit var folderName: String
    private lateinit var defaultBitmap: Bitmap
    private lateinit var reducedBitmap: Bitmap
    private lateinit var finalBitmap: Bitmap
    private lateinit var maxResolution: Bitmap
    private var pixelsList = mutableListOf<Pixel>()
    private var isPixelInfoEnabled = true
    private var enabledColors = mutableListOf<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        togglePixelInfo()

        for(i in lego_colors.indices){
            enabledColors.add(true)
        }

        for(pixel in lego_colors){
            pixelsList.add(Pixel(false, pixel.first, pixel.second, pixel.third))
        }

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        folderName = intent.getStringExtra("folderName").toString()
        binding.folderName.setText(folderName)

        val imageURI = intent.getStringExtra("imageURI")?.toUri()!!

        val imageStream = contentResolver.openInputStream(imageURI)
        defaultBitmap = BitmapFactory.decodeStream(imageStream)

        legofy(height = 80, saturation = 1f, brightness = 1f)

        binding.validate.setOnClickListener { onValidation() }

        binding.save.setOnClickListener { onSave() }

        binding.brightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            }

            override fun onStartTrackingTouch(seek: SeekBar) { }

            override fun onStopTrackingTouch(seek: SeekBar) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            }
        })

        binding.brightnessText.setOnLongClickListener{
            onBrightnessTextClick()
        }
        binding.saturationText.setOnLongClickListener{
            onSaturationTextClick()
        }
        binding.sizeText.setOnLongClickListener{
            onSizeTextClick()
        }

        binding.saturation.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            }
        })

        binding.size.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())

                val offset = (binding.size.width - binding.size.paddingLeft - binding.size.paddingRight) * progress / binding.size.max
                binding.sizeValueContainer.x = binding.size.x + binding.size.paddingLeft + offset - binding.sizeValueContainer.width / 2
                binding.sizeValueContainer.y = binding.size.y - 40
                @Suppress("SetTextI18n")
                binding.sizeValue.text = "${getMatchingWidth((getHeight()))} X ${getHeight()}"

            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                val offset = (binding.size.width - binding.size.paddingLeft - binding.size.paddingRight) * binding.size.progress / binding.size.max
                binding.sizeValueContainer.x = binding.size.x + binding.size.paddingLeft + offset - binding.sizeValueContainer.width / 2
                binding.sizeValueContainer.y = binding.size.y - 40
                binding.sizeValueContainer.visibility = View.VISIBLE
                @Suppress("SetTextI18n")
                binding.sizeValue.text = "${getHeight()} X ${getMatchingWidth((getHeight()))}"
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            }
        })

        binding.materialCardView2.setOnClickListener{ togglePixelInfo() }

        val layoutManagerPixels: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.pixelItems.layoutManager = layoutManagerPixels
        binding.pixelItems.adapter = ColorEnableAdapter(this, pixelsList, enabledColors) { pos: Int -> onColorSwitched(pos) }
        binding.pixelItems.setHasFixedSize(true)
        
        binding.selectAll.setOnCheckedChangeListener { _, _ ->
            for(k in enabledColors.indices){
                enabledColors[k] = binding.selectAll.isChecked
            }
            legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
            @Suppress("notifyDataSetChanged")
            binding.pixelItems.adapter?.notifyDataSetChanged()
        }
    }

    private fun onColorSwitched(pos: Int): Boolean{
        enabledColors[pos] = !enabledColors[pos]
        Log.d(TAG, "toggled to ${enabledColors[pos]}")
        legofy(height = getHeight(), saturation = getSaturationFactor(), brightness = getBrightnessFactor())
        return true
    }

    private fun togglePixelInfo(){
        isPixelInfoEnabled = !isPixelInfoEnabled
        if(isPixelInfoEnabled){
            @Suppress("SetTextI18n")
            binding.showPixels.text = "Palette de couleur ->"
            binding.cardPixels.animate().translationX(-binding.cardPixels.width.toFloat())
            binding.legofied.animate().translationX(-binding.cardPixels.width.toFloat())
            binding.materialCardView.animate().translationX(-binding.cardPixels.width.toFloat())
            binding.size.isEnabled = false
            binding.brightness.isEnabled = false
            binding.saturation.isEnabled = false
        } else {
            @Suppress("SetTextI18n")
            binding.showPixels.text = "<- Palette de couleur"
            binding.cardPixels.animate().translationX(0F)
            binding.legofied.animate().translationX(0F)
            binding.materialCardView.animate().translationX(0F)
            binding.size.isEnabled = true
            binding.brightness.isEnabled = true
            binding.saturation.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onBrightnessTextClick():Boolean{
        binding.brightness.progress = 19
        return true
    }
    private fun onSaturationTextClick():Boolean{
        binding.saturation.progress = 19
        return true
    }
    private fun onSizeTextClick():Boolean{
        binding.size.progress = 75
        return true
    }

    private fun getSaturationFactor(): Float{
        return .05f*(binding.saturation.progress + 1)
    }
    private fun getBrightnessFactor(): Float{
        return .05f*(binding.brightness.progress + 1)
    }
    private fun getHeight(): Int{
        return 16 + binding.size.progress
    }

    private fun getMatchingWidth(height:Int):Int{
        return height * defaultBitmap.width/defaultBitmap.height
    }

    private fun legofy(height:Int = 80, saturation: Float = 4f, brightness: Float = 1f, saveMaxResolution: Boolean = false){

        val usedColor = mutableListOf<Boolean>()
        for(i in lego_colors.indices){
            usedColor.add(false)
        }

        fun dist(pixel1: Triple<Int, Int, Int>, pixel2: Triple<Int, Int, Int>) : Int{
            return abs(pixel1.first - pixel2.first) + abs(pixel1.second - pixel2.second) + abs(
                pixel1.third - pixel2.third
            )
        }

        fun tripleToColor(triple: Triple<Int, Int, Int>): Int{
            return Color.rgb(triple.first, triple.second, triple.third)
        }

        fun colorToTriple(color:Int): Triple<Int, Int, Int>{
            return Triple(color.red, color.green, color.blue)
        }

        fun closestColor(pixel: Triple<Int, Int, Int>) : Int{
            var minIndex = -1
            var minDist = 1000
            var d: Int
            var color: Triple<Int, Int, Int>
            for(k in lego_colors.indices){
                if(enabledColors[k]){
                    color = lego_colors[k]
                    d = dist(pixel, color)
                    if(d < minDist){
                        minIndex = k; minDist = d
                    }
                    usedColor[minIndex] = true
                }
            }
            if(minIndex == -1){
                return Color.argb(0,0,0,0)
            }
            return tripleToColor(lego_colors[minIndex])
        }

        fun transform(bitmap: Bitmap) : Bitmap {

            val pixelsArray = IntArray(bitmap.width * bitmap.height)

            bitmap.getPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)


            for (y in pixelsArray.indices) {
                pixelsArray[y] = closestColor(colorToTriple(pixelsArray[y]))
            }
            val res = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            res.setPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return res
        }

        fun applyEffect(bitmap: Bitmap, saturationFactor: Float, brightnessFactor: Float) : Bitmap {
            val pixelsArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            for (k in pixelsArray.indices) {
                val color = pixelsArray[k]
                val hsl = floatArrayOf(0F,0F,0F)
                ColorUtils.colorToHSL(color, hsl)
                hsl[1] = min(hsl[1]*saturationFactor, 1F)
                hsl[2] = min(hsl[2]*brightnessFactor, 1F)
                pixelsArray[k] = ColorUtils.HSLToColor(hsl)
            }

            val res: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            res.setPixels(pixelsArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return res
        }

        fun toPixels(bitmap: Bitmap, height: Int): Bitmap {
            val width = Math.floorDiv(height * bitmap.width, bitmap.height)
            return Bitmap.createScaledBitmap(bitmap, width, height, false)
        }

        if(!saveMaxResolution){
            reducedBitmap = toPixels(defaultBitmap, height)
            finalBitmap = transform(applyEffect(reducedBitmap, saturation, brightness))

            val shownBitmap = Bitmap.createScaledBitmap(finalBitmap, finalBitmap.width*2, finalBitmap.height*2, false)
            binding.legofied.setImageBitmap(shownBitmap)
        } else {
            maxResolution = transform(applyEffect(defaultBitmap, saturation, brightness))
        }

    }

    private fun maxResolutionSave(){
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "legofier_${timeStamp}_default"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        Log.d(TAG, storageDir)
        val output = FileOutputStream("$storageDir/$imageFileName.png")

        legofy(saturation = getSaturationFactor(), brightness = getBrightnessFactor(), saveMaxResolution = true)

        val scaledBitmap = Bitmap.createScaledBitmap(maxResolution, maxResolution.width, maxResolution.height, false)

        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        Toast.makeText(this, "Image enregistrée dans la galerie avec succès", Toast.LENGTH_SHORT).show()
    }

    private fun startNew(){
        val folderName = binding.folderName.text.toString()

        val dir = this.filesDir.absolutePath + '/' + folderName
        File(dir).mkdir()
        val output = FileOutputStream("$dir/$folderName.png")

        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        val intent = Intent(this, AdjustSize::class.java)
        intent.putExtra("folderName", folderName)
        Log.d(TAG, "Creating a new folder, launching intent")
        startActivity(intent)
        this.finish()
    }

    private fun classicSave(){
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "legofier_$timeStamp"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        Log.d(TAG, storageDir)
        val output = FileOutputStream("$storageDir/$imageFileName.png")

        val scaledBitmap = Bitmap.createScaledBitmap(finalBitmap, finalBitmap.width*10, finalBitmap.height*10, false)

        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        Toast.makeText(this, "Image enregistrée dans la galerie avec succès", Toast.LENGTH_SHORT).show()
    }

    private fun onSave(){
        if(binding.resolutionSwitch.isChecked){
            val alertDialogBuilder = MaterialAlertDialogBuilder(this)
            alertDialogBuilder.setTitle("Sauvegarde de l'image")
            alertDialogBuilder.setMessage("Attention : la durée de sauvegarde du fichier pour une résolution maximale peut varier de quelques secondes à quelques minutes.")
            alertDialogBuilder.setPositiveButton("Continuer") { _: DialogInterface, _: Int ->
                Toast.makeText(this, "Sauvegarde en cours...", Toast.LENGTH_LONG).show()
                maxResolutionSave()
            }
            alertDialogBuilder.setNegativeButton("Annuler") { _: DialogInterface, _: Int -> }
            alertDialogBuilder.create().show()
        } else {
            classicSave()
        }
    }

    private fun onValidation(){
        val folderName = binding.folderName.text.toString()

        val dir = this.filesDir.absolutePath + '/' + folderName

        if(File(dir).exists()){
            val alertDialogBuilder = MaterialAlertDialogBuilder(this)
            alertDialogBuilder.setTitle("Écraser le fichier ?")
            alertDialogBuilder.setMessage("Le dossier \"${folderName}\" existe déjà.\n\nVoulez-vous le remplacer ou changer le nom du nouveau fichier ?")
            alertDialogBuilder.setPositiveButton("Remplacer") { _: DialogInterface, _: Int ->
                File(dir).deleteRecursively()
                startNew()
            }
            alertDialogBuilder.setNegativeButton("Changer le nom") { _: DialogInterface, _: Int ->
                binding.folderName.requestFocus()
                if(binding.folderName.text.toString() != "") {
                    binding.folderName.setSelection(binding.folderName.text!!.length)
                }
            }
            alertDialogBuilder.create().show()
        } else {
            startNew()
        }
    }
}