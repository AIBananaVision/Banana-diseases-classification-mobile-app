package com.cmu.banavision

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.common.Resource
import com.cmu.banavision.usecases.PictureUseCase
import com.cmu.banavision.usecases.UploadUseCase
import com.cmu.banavision.util.LocationAltitutdeAndLongitude
import com.cmu.banavision.util.LocationData
import com.cmu.banavision.util.LocationService
import com.cmu.banavision.util.LocationState
import com.cmu.banavision.util.ModelData
import com.cmu.banavision.util.ResponseState
import com.cmu.banavision.util.toFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@SuppressLint("UnspecifiedRegisterReceiverFlag")
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val pictureUseCase: PictureUseCase,
    private val uploadUseCase: UploadUseCase,
    application: Application
) : ViewModel() {
    // declare context

    private val _imageUri = MutableStateFlow(ImagesState())
    val imageUri = _imageUri.asStateFlow()

    private val _pendingDeleteImage = MutableStateFlow<Uri?>(null)
    val pendingDeleteImage = _pendingDeleteImage.asStateFlow()
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData = _locationData.asStateFlow()
    private val _responseState = MutableStateFlow(ResponseState())
    val responseState = _responseState.asStateFlow()

    inner class LocationUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val lat = intent.getDoubleExtra("latitude", 0.0)
            val long = intent.getDoubleExtra("longitude", 0.0)
            _locationData.value = LocationData(lat, long, "", "", "")
            // get the location address from the latitude and longitude
            viewModelScope.launch(Dispatchers.IO) {
                if (lat != 0.0 && long != 0.0) {

                    _locationData.value =
                        pictureUseCase.captureAndSaveImageUseCase.getAddressFromLocation(
                            context,
                            lat,
                            long
                        )

                } else {
                    _locationData.value = locationData.value?.copy(
                        address = "Location not found"
                    )

                }
            }
        }
    }

    init {
        getLocation(application)
        val filter = IntentFilter("LOCATION_UPDATE")
        val receiver = LocationUpdateReceiver() // create an instance of LocationUpdateReceiver
        application.registerReceiver(receiver, filter)
    }

    fun deleteImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _pendingDeleteImage.value = uri
            _imageUri.update { imageState ->
                imageState.copy(
                    uri = null
                )
            }
            // delete image in 5 seconds when undo is not clicked
            delay(5000)
            if (pendingDeleteImage.value == uri) {
                pictureUseCase.captureAndSaveImageUseCase.deleteImage(context, uri)
                _pendingDeleteImage.value = null
            }
        }
    }

    fun undoDeleteImage(uri: Uri) {
        viewModelScope.launch {
            _pendingDeleteImage.value = null
            _imageUri.update { imageState ->
                imageState.copy(
                    uri = uri
                )
            }
        }
    }

    fun onTakePhoto(uri: Uri) {
        viewModelScope.launch {
            _imageUri.update { imageState ->
                imageState.copy(
                    uri = uri
                )
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

    fun sendImageAndLocationToModel(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val location = locationData.value
            if (location != null) {
                val imageFile: File? = uri.toFile(context)
                val modelData = ModelData(
                    imageFile = imageFile ?: File(""),
                    locationData = location
                )
                _responseState.update { responseState ->
                    responseState.copy(
                        loading = true,
                        uri = uri
                    )
                }
                when (val results = uploadUseCase(modelData)) {
                    is Resource.Loading -> {
                        Log.i("Response", "Resource is Loading")
                        _responseState.update { responseState ->
                            responseState.copy(
                                loading = true,
                                uri = uri
                            )

                        }
                    }
                    is Resource.Success -> {
                        _responseState.update { responseState ->
                            print("Response state: ${responseState.response}")
                            Log.d("Response", "Response Gotten: ${results.data}")
                            responseState.copy(
                                response = results.data,
                                loading = false,
                                uri = uri
                            )

                        }
                    }

                    is Resource.Error -> {
                        _responseState.update { responseState ->
                            responseState.copy(
                                error = results.message,
                                loading = false,
                                uri = uri
                            )

                        }
                    }


                }
            }

        }

    }

    private fun getLocation(context: Context) {

        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            context.startService(this)
        }
        _locationData.value = locationData.value?.copy(
            latitude = LocationAltitutdeAndLongitude.latitude,
            longitude = LocationAltitutdeAndLongitude.longitude
        )
    }

    fun bitmapToUri(context: Context, it: Bitmap): Uri {
        return pictureUseCase.captureAndSaveImageUseCase.bitmapToUri(context, it)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}