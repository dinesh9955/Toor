package com.wisam.taxi.socket.responses.bookingAccept

data class BookingAcceptResponse(
    val acceptStatus: Int,
    val data: Data,
    val sucess: Boolean
)