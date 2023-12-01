package com.cmu.banavision.util

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val countryName: String,
    val locality: String,
    val address: String
)
object LocationAltitutdeAndLongitude {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}