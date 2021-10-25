package com.wisam.taxi.model.response.booking

data class Booking(
    val __v: Int,
    val _id: String,
    val bookingEndDate: Long,
    val bookingEndTime: Long,
    val bookingStartDate: Long,
    val bookingStartTime: Long,
    val bookingType: Int,
    val couponCode: String,
    val couponDiscount: String,
    val createdAt: String,
    val date: Long,
    val destination: Destination,
    val distance: Int,
    val driverId: DriverId,
    val driverRating: Double,
    val driverOverallRating: Double,
    val driverReachTime: Int,
    val id: String,
    val note: String,
    val path: String,
    val paymentMode: String,
    val seats: Int,
    val source: Source,
    val status: Int,
    val subTotalAmount: Int,
    val tax: Int,
    val taxiOrderNo: Int,
    val totalAmount: Int,
    val totalDiscount: Int,
    val updatedAt: String,
    val userId: UserId,
    val userRating: Int
)