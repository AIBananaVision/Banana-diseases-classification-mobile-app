package com.cmu.banavision

import android.content.Context
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.repository.ImageCaptureSingleton
import com.urutare.kategora.domain.usecase.PictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val pictureUseCase: PictureUseCase
) : ViewModel() {

    private val _imageUri = MutableStateFlow(ImageState())
    val imageUri = _imageUri.asStateFlow()
    fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        viewModelScope.launch {
            pictureUseCase.captureAndSaveImageUseCase.showCameraPreview(
                previewView,
                lifecycleOwner
            )
        }
    }
    fun deleteImage(){
        viewModelScope.launch {
            _imageUri.update { imageState ->
                imageState.copy(
                    uri = null
                )
            }
        }
    }

    fun captureAndSave(context: Context) {
        viewModelScope.launch {
            pictureUseCase.captureAndSaveImageUseCase.captureAndSaveImage(context).apply {
                ImageCaptureSingleton.imageUri.collectLatest {
                    _imageUri.update { imageState ->
                        imageState.copy(
                            uri = it.uri
                        )
                    }

                }
            }

        }
    }

    fun chooseImageFromGallery(uri: Uri) {
        viewModelScope.launch {
            _imageUri.update { imageState ->
                imageState.copy(
                    uri = uri
                )
            }
        }
    }


}