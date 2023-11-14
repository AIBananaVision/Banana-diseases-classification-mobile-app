package com.cmu.banavision.repository

import android.net.Uri
import androidx.camera.core.ImageCaptureException
import com.cmu.banavision.ImageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ImageCaptureSingleton {
    private val imageUriState = MutableStateFlow(ImageState())
    val imageUri = imageUriState.asStateFlow()

    fun setOnError(onError: (ImageCaptureException)) {
        println("Error happened: $onError")
        imageUriState.update { imageState ->
            imageState.copy(
                uri = null
            )
        }
    }

    fun notifySelectedUri(uri: Uri?) {
        println("New image uri: $uri")
        imageUriState.update { imageState ->
            imageState.copy(
                uri = uri
            )
        }
    }
}
