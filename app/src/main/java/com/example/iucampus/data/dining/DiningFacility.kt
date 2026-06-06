package com.example.iucampus.data.dining

data class DiningFacility(
    val name: String,
    val cuisine: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val opening: List<String>,
    val phoneNumber: String,
    val image: String,
    val rate: Double
)
