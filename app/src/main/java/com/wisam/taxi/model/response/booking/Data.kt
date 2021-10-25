package com.wisam.taxi.model.response.booking

data class Data(
    val bookingList: ArrayList<Booking>,
    val count: Int,
    val notiCount: Int
)