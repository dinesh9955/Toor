package com.wisam.taxi.model.response.getBookingDetailsUser

data class Destination(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)