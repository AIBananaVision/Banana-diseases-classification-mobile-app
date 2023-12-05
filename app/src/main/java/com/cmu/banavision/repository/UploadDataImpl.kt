package com.cmu.banavision.repository

import android.util.Log
import com.cmu.banavision.common.Resource
import com.cmu.banavision.network.APIService
import com.cmu.banavision.network.ApiResponse
import com.cmu.banavision.network.ErrorInfo
import com.cmu.banavision.util.ModelData
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class UploadDataImpl @Inject constructor(
    private val apiService: APIService
) : UploadDataRepo {


    // Assume uploadData is a Flow in your repository
    override suspend fun uploadData(modelData: ModelData): Flow<Resource<ApiResponse>> = flow {
        // Emit loading state
        emit(Resource.Loading())
        try {
            val latitude = modelData.locationData.latitude.toString()
            val longitude = modelData.locationData.longitude.toString()
            val countryName = modelData.locationData.countryName
            val locality = modelData.locationData.locality
            val address = modelData.locationData.address
            val imagePart = modelData.createImageRequestBody()

            try {
                val response = apiService.uploadImage(
                    latitude = latitude,
                    longitude = longitude,
                    countryName = countryName,
                    locality = locality,
                    address = address,
                    image_file = imagePart
                )

                if (response.isSuccessful) {
                    val responseData = response.body()?.string() ?: ""
                    Log.i("UploadDataImpl", "uploadData Response: $responseData")
                    emit(Resource.Success(Gson().fromJson(responseData, ApiResponse::class.java)))
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                Log.e("UploadDataImpl", "Exception while calling uploadImage", e)
                emit(Resource.Error(e.message ?: "An error occurred while calling uploadImage"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }



    private fun handleErrorResponse(response: Response<ResponseBody>): Resource<ApiResponse> {
        return try {
            val errorMessage = response.errorBody()?.string()
            Log.e("UploadDataImpl", "uploadData Error: $errorMessage")
            val errorInfo: ErrorInfo = Gson().fromJson(errorMessage, ErrorInfo::class.java)
            Resource.Error(errorInfo.message ?: "An error occurred")
        } catch (e: Exception) {
            Resource.Error("An error occurred while handling the response")
        }
    }


    private fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}