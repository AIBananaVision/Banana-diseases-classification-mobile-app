package com.cmu.banavision.network

import retrofit2.http.GET
import retrofit2.http.Query

interface SoilService {

    @GET("soilgrids/v2.0/properties/query")
    suspend fun getSoilProperties(
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double,
        @Query("property") properties: List<String>,
        @Query("depth") depth: String,
        @Query("value") values: List<String>
    ): SoilResponse
}
