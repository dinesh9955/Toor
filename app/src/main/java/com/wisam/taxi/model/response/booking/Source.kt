package com.wisam.taxi.model.response.booking

data class Source(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)