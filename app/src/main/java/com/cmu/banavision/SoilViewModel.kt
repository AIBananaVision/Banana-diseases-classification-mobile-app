package com.cmu.banavision
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmu.banavision.network.SoilResponse
import com.cmu.banavision.network.SoilService
import com.cmu.banavision.util.SoilPropertState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilViewModel @Inject constructor(private val soilService: SoilService) : ViewModel() {

    private val _soilProperties = MutableStateFlow<SoilPropertState?>(null)
    val soilProperties: StateFlow<SoilPropertState?> get() = _soilProperties

    fun getSoilProperties(longitude: Double, latitude: Double, properties: List<String>, depth: String, values: List<String>) {
        viewModelScope.launch {
            _soilProperties.value = SoilPropertState(isLoading = true)
            try {
                val response = soilService.getSoilProperties(
                    longitude = longitude,
                    latitude = latitude,
                    properties = properties,
                    depth = depth,
                    values = values
                )

                _soilProperties.value = soilProperties.value?.copy(
                    soilPropertState = response,
                    isLoading = false,
                    error = ""
                )
                print("Soil properties: ${response.properties.layers[0].depths[0].values.mean}")
            } catch (e: Exception) {
                _soilProperties.value = soilProperties.value?.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false,
                    soilPropertState = null
                )
                e.printStackTrace()
            }
        }
    }
}