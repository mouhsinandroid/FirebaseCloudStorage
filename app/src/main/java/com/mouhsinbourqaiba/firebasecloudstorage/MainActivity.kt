package com.mouhsinbourqaiba.firebasecloudstorage

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mouhsinbourqaiba.firebasecloudstorage.Const.REQUEST_CODE_IMAGE_PICK
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    var curFile: Uri? = null

    val imageRef = Firebase.storage.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
            }
        }

        btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }

        btnDownloadImage.setOnClickListener {
            downloadImage("myImage")
        }

        btnDeleteImage.setOnClickListener {
            deleteImage("myImage")
        }

        listFiles()
    }


    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = imageRef.child("images/").listAll().await()
            val imagesUrls = mutableListOf<String>()

            for (image in images.items) {
                val url = image.downloadUrl.await()
                imagesUrls.add(url.toString())
            }

            withContext(Dispatchers.Main) {
                val imageAdapter = ImageAdapter(imagesUrls)
                rvImages.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun deleteImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$fileName").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Image deleting Successfully",
                        Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun downloadImage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$fileName")
                    .getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            withContext(Dispatchers.Main) {
                ivImage.setImageBitmap(bmp)

                Toast.makeText(this@MainActivity, "Successfully download Image",
                        Toast.LENGTH_SHORT).show()
            }

        }catch (e:Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageToStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$fileName").putFile(it).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully uploaded Image",
                            Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
                && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let { uri ->
                curFile = uri
                ivImage.setImageURI(uri)
            }

        }
    }
}

object Const {
    const val REQUEST_CODE_IMAGE_PICK = 0
}