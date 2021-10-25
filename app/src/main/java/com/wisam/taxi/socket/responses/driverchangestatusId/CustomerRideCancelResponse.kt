package com.wisam.taxi.socket.responses.driverchangestatusId

data class CustomerRideCancelResponse(
    val booking: Booking,
    val message: String,
    val status: Int,
    val sucess: Boolean
)