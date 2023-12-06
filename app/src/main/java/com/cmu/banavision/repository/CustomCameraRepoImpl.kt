package com.cmu.banavision.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.cmu.banavision.R
import com.cmu.banavision.util.LocationData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class CustomCameraRepoImpl : CustomCameraRepo {

    override fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
        val outputDirectory = context.getOutputDirectory()
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Save the Bitmap to the photoFile
        try {
            FileOutputStream(photoFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the exception appropriately based on your app's requirements
        }

        // Obtain a Uri for the saved photoFile using FileProvider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    override fun deleteFile(context: Context, uri: Uri): Boolean {
        val filePath = getRealPathFromUri(context, uri)
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                return file.delete()
            }
        }
        return false
    }

    override fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationData {
        val geocoder = android.location.Geocoder(context, Locale.getDefault())
        geocoder.getFromLocation(latitude, longitude, 1)?.let { addresses ->
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressFragments = with(address) {
                    (0..maxAddressLineIndex).map { getAddressLine(it) }
                }
                val userAddress = addressFragments.joinToString(separator = "\n")
                return LocationData(
                   latitude= latitude,
                    longitude = longitude,
                    address=userAddress,
                    locality = address.locality,
                    countryName = address.countryName
                )
            }
        }
        return LocationData(
            latitude,
            longitude,
            "",
            "",
            ""
        )
    }


    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        // Check if the Uri scheme is "file"
        if (uri.scheme == "file") {
            return uri.path
        }

        // Check if the Uri scheme is "content"
        if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    if (columnIndex != -1) {
                        // Column '_data' exists
                        return cursor.getString(columnIndex)
                    } else {
                        print(" Not found!")
                    }
                }
            }
        }

        return null
    }


    private fun Context.getOutputDirectory(): File {
        val mediaDir = this.externalCacheDir?.let {
            File(it, this.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else this.filesDir
    }
}


