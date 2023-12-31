package com.cmu.banavision.usecases

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.cmu.banavision.repository.CustomCameraRepo
import com.cmu.banavision.util.LocationData
import javax.inject.Inject

class CaptureAndSaveImage @Inject constructor(
    private val repository: CustomCameraRepo
) {

  fun bitmapToUri(context: Context, it: Bitmap): Uri {
        return repository.bitmapToUri(context, it)
    }
    fun deleteImage(context: Context, uri: Uri):Boolean{
        return repository.deleteFile(uri = uri, context = context)
    }
    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): LocationData {
        return repository.getAddressFromLocation(context, latitude, longitude)
    }
}
