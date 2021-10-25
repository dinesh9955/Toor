package com.wisam.taxi.model.driverResponse.getbooking

data class Source(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)