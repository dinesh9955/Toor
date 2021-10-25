package com.wisam.taxi.socket.responses.newBooking

data class NewBookingResponse(
    val couponCode: String,
    val couponDiscount: Int,
    val date: Long,
    val driver: String,
    val note: String,
    val requestId: String,
    val route: String,
    val routeDetail: RouteDetail,
    val seats: Int,
    val subTotalAmount: Int,
    val tax: Int,
    val totalAmount: Int,
    val totalDiscount: Int,
    val user: User,
    val userId: String
)