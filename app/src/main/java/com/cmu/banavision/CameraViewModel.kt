package com.cmu.banavision

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.usecases.PictureUseCase
import com.cmu.banavision.util.LocationData
import com.cmu.banavision.util.LocationState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    application: Application
    ) : ViewModel() {
    // declare context

    private val _imageUris = MutableStateFlow(ImagesState())
    val imageUris = _imageUris.asStateFlow()
    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()
    private val _pendingDeleteImage = MutableStateFlow<Uri?>(null)
    val pendingDeleteImage = _pendingDeleteImage.asStateFlow()
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData = _locationData.asStateFlow()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
init {
    getLocation(application)
}
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

    fun onTakePhoto(uri: Uri) {
        viewModelScope.launch {
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = imageState.uris + uri
                )
            }
        }

    }

    fun chooseImageFromGallery(uri: Uri) {
        viewModelScope.launch {
            _imageUris.update { imageState ->
                imageState.copy(
                    uris = imageState.uris + uri
                )
            }

        }

    }

private fun getLocation(context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
        while (true) {
            Log.i("LocationViewModel", "getLocation: called")
            if (checkPermissions(context)) {
                Log.i("LocationViewModel", "getLocation: permission granted")
                if (isLocationEnabled(context)) {
                    Log.i("LocationViewModel", "getLocation: location enabled")
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.i("LocationViewModel", "getLocation: permission not granted")
                        return@launch
                    }
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                        val location: Location? = task.result
                        Log.i("LocationViewModel", "getLocation: $location")
                        if (location != null) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)?.forEach(
                                fun(address: Address) {
                                    Log.i("LocationViewModel", "getLocation: $address")
                                    _locationData.value = LocationData(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        countryName = address.countryName,
                                        locality = address.locality,
                                        address = address.getAddressLine(0)
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Log.i("LocationViewModel", "getLocation: location not enabled")
                }
            } else {
                requestPermissions(context)
            }
            delay(100000) // delay for 10 seconds before the next update
        }
    }
}

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
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