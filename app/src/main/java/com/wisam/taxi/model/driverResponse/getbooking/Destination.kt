package com.wisam.taxi.model.driverResponse.getbooking

data class Destination(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)