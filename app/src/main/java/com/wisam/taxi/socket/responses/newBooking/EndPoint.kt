package com.wisam.taxi.socket.responses.newBooking

data class EndPoint(
    val __v: Int,
    val _createdDate: String,
    val _id: String,
    val _isDeleted: Boolean,
    val _updatedDate: String,
    val address: String,
    val coordinates: List<Double>
)