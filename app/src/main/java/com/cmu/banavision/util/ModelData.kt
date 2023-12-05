package com.cmu.banavision.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class ModelData(
    val locationData: LocationData,
    val imageFile: File
){
    fun createImageRequestBody(): MultipartBody.Part {
        val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image_file", imageFile.name, imageRequestBody)
    }
}
