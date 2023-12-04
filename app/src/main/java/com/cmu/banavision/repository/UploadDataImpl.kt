package com.cmu.banavision.repository

import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.APIService
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.util.ModelData
import javax.inject.Inject

class UploadDataImpl  @Inject constructor(
    private val apiService: APIService
) : UploadDataRepo {
    override suspend fun uploadData(modelData: ModelData): Resource<ApiResponse> {
        return try {
            val response = apiService.uploadImage(
                modelData.locationData.latitude.toString(),
                modelData.locationData.longitude.toString(),
                modelData.locationData.countryName,
                modelData.locationData.locality,
                modelData.locationData.address,
                modelData.createImageRequestBody()
            ).execute()
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}