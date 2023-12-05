package com.cmu.banavision.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface APIService {
    @Multipart
    @POST("upload-data")
    suspend fun uploadImage(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("countryName") countryName:String ,
        @Query("locality") locality: String,
        @Query("address") address: String,
        @Part image_file: MultipartBody.Part
    ): Response<ResponseBody>
}

