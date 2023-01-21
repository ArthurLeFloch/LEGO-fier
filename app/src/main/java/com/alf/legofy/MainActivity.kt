package com.alf.legofy

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alf.legofy.databinding.ActivityMainBinding
import java.io.File


private const val TAG = "MainActivityTAG"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var folderList: MutableList<Folder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = getString(R.string.open_detail)*/

        folderList = mutableListOf()
        loadFiles()

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = FileAdapter(this, binding.recyclerView, folderList) { position: Int ->
            openFolderFromRecyclerView(
                position
            )
        }

        binding.fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageURI: Uri = data?.data!!
            createFile(selectedImageURI)
        }
    }


    private fun loadFiles(){
        folderList.clear()
        val path = this.filesDir.absolutePath
        val file = File(path).list()
        file?.forEach {
            if(File(path, it).isDirectory) {
                val info = File("$path/$it", "info.txt")
                if(info.exists()){
                    val lines = info.readLines()
                    val format = lines[0].split(',')

                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile("$path/$it/$it.png", options)
                    val shownBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width*4, bitmap.height*4, false)

                    folderList.add(Folder(it, format[0].toInt(), format[1].toInt(), shownBitmap))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        loadFiles()
        @Suppress("notifyDataSetChanged")
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    private fun createFile(imageURI: Uri){
        val folderName = File(getRealPathFromURI(imageURI)!!).nameWithoutExtension

        val intent = Intent(this, ImageSetupActivity::class.java)
        intent.putExtra("folderName", folderName)
        intent.putExtra("imageURI", imageURI.toString())
        Log.d(TAG, imageURI.toString())
        startActivity(intent)
    }

    private fun openFolder(folderName: String){
        val intent = Intent(this, TilesActivity::class.java)
        intent.putExtra("folderName", folderName)
        intent.putExtra("fileName", folderName)
        intent.putExtra("isNew", false)
        Log.d(TAG, folderName)
        startActivity(intent)
    }

    private fun openFolderFromRecyclerView(position: Int) {
        openFolder(folderList[position].name)
    }

}