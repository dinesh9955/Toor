package com.wisam.taxi.model.driverResponse.profile

data class Data(
    val __v: Int,
    val _id: String,
    val activateTime: Long,
    val authToken: String,
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
    val ratings: Double,
    val route: String,
    val status: Int,
    val updatedAt: String,
    val vehicleTypeId: VehicleTypeId
)