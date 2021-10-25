package com.wisam.taxi.socket.responses.requestAction

data class Source(
    val address: String,
    val cordinates: List<Double>,
    val latitude: Double,
    val longitude: Double
)