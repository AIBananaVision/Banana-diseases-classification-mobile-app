package com.cmu.banavision

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.network.SoilResponse
import com.cmu.banavision.network.SoilService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilViewModel @Inject constructor(private val soilService: SoilService) : ViewModel() {

    private val _soilProperties = MutableLiveData<SoilResponse>()
    val soilProperties: LiveData<SoilResponse> get() = _soilProperties

    fun getSoilProperties(longitude: Double, latitude: Double, properties: List<String>, depth: String, values: List<String>) {
        viewModelScope.launch {
            try {
                val response = soilService.getSoilProperties(
                    longitude = longitude,
                    latitude = latitude,
                    properties = properties,
                    depth = depth,
                    values = values
                )

                _soilProperties.value = response
                print("Soil properties: ${response.properties.layers[0].depths[0].values.mean}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
