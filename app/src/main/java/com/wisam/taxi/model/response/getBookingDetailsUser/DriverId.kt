package com.wisam.taxi.model.response.getBookingDetailsUser

data class DriverId(
    val __v: Int,
    val _id: String,
    val activateTime: Any,
    val buildingNumber: String,
    val countryCode: String,
    val createdAt: String,
    val flatNumber: String,
    val fullName: String,
    val id: String,
    val isApproved: Boolean,
    val isAvailable: Int,
    val isSocialRegister: Int,
    val isVerified: Boolean,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val profilePic: String,
    val route: Any,
    val status: Int,
    val updatedAt: String,
    val vehicleTypeId: String
)