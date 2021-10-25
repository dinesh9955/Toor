package com.wisam.taxi.model.response.booking

data class DriverId(
    val _id: String,
    val countryCode: String,
    val fullName: String,
    val id: String,
    val phone: String,
    val profilePic: String,
    val vehicleNumber: String,
    val vehicleTypeId: VehicleTypeId
)