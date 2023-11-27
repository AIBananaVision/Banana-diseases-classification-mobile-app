package com.cmu.banavision.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface CustomCameraRepo {
    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri
    fun deleteFile(context: Context,uri: Uri): Boolean
}
