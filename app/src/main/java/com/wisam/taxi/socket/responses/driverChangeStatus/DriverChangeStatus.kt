package com.wisam.taxi.socket.responses.driverChangeStatus

data class DriverChangeStatus(
    val booking: Booking,
    val status: Int,
    val sucess: Boolean
)