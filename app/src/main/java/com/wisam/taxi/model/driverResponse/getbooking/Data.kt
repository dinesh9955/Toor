package com.wisam.taxi.model.driverResponse.getbooking

data class Data(
    val bookings: ArrayList<Booking>,
    val count: Int
)