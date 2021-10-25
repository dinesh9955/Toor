package com.wisam.taxi.socket.responses.driverChangeStatus

data class Destination(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)