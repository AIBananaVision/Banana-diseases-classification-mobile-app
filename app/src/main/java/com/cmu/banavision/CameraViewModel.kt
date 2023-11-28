package com.cmu.banavision

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.usecases.PictureUseCase
import com.cmu.banavision.util.LocationState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val pictureUseCase: PictureUseCase,

    ) : ViewModel() {
    // declare context

    private val _imageUris = MutableStateFlow(ImagesState())
    val imageUris = _imageUris.asStateFlow()
    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()
    private val _pendingDeleteImage = MutableStateFlow<Uri?>(null)
    val pendingDeleteImage = _pendingDeleteImage.asStateFlow()

    fun deleteImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _pendingDeleteImage.value = uri
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = imageState.uris - uri
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
        _imageUris.update { imageState ->
            imageState.copy(
                uris = imageState.uris + uri
            )
        }
    }
}

    fun onTakePhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = imageState.uris + uri
                )
            }
            getLocation(context)
        }

    }

    fun chooseImageFromGallery(uri: Uri, context: Context) {
        viewModelScope.launch {
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = imageState.uris + uri
                )
            }
            getLocation(context)
        }

    }

    private fun getLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )

        } else {
           Log.i( "Location", "Location permission granted")
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Get location name
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val locationName = addresses?.get(0)?.getAddressLine(0)
                    //Toast location
                    Toast.makeText(context, "Location is $locationName", Toast.LENGTH_SHORT).show()
                    _locationState.value = locationState.value.copy(
                        latitude = latitude,
                        longitude = longitude,
                        locationName = locationName
                    )
                }
                else {
                    Log.i("Location", "Location is null")
                }
            }
        }
    }

    fun bitmapToUri(context: Context, it: Bitmap): Uri {
        return pictureUseCase.captureAndSaveImageUseCase.bitmapToUri(context, it)
    }

    fun clearImageUris(context: Context) {
        viewModelScope.launch {
            _imageUris.value.uris.forEach {
                if (it != null) {
                    pictureUseCase.captureAndSaveImageUseCase.deleteImage(context, it)
                    _pendingDeleteImage.value = it
                }
            }
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = emptyList()
                )
            }
        }

    }
}