package com.cmu.banavision.network

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("location") val location: Location,
    @SerializedName("model_results") val modelResults: ModelResults
)

data class Location(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("locality") val locality: String,
    @SerializedName("address") val address: String
)

data class ModelResults(
    @SerializedName("predicted_class") val predictedClass: String,
    @SerializedName("probabilities") val probabilities: Map<String, Double>
)