package com.urutare.kategora.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.cmu.banavision.repository.CustomCameraRepo
import javax.inject.Inject

class CaptureAndSaveImage @Inject constructor(
    private val repository: CustomCameraRepo
) {
    suspend  fun captureAndSaveImage(context: Context) {
        return repository.captureAndSaveImage(context)
    }
    suspend fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ){
        repository.showCameraPreview(
            previewView,
            lifecycleOwner
        )
    }
}
