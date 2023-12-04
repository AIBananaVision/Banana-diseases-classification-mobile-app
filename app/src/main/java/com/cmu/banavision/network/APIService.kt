package com.cmu.banavision.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {
    @Multipart
    @POST("upload-data")
    fun uploadImage(
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String,
        @Path("countryName") countryName: String,
        @Path("locality") locality: String,
        @Path("address") address: String,
        @Part image: MultipartBody.Part
    ): Call<ApiResponse>
}