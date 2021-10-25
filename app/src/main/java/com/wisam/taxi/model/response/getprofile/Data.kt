package com.wisam.taxi.model.response.getprofile

data class Data(
    val __v: Int,
    val _id: String,
    val address: String,
    val address1: String,
    val authToken: String,
    val countryCode: String,
    val createdAt: String,
    val email: String,
    val fullName: String,
    val id: String,
    val isSocialRegister: Int,
    val isVerified: Boolean,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val profilePic: String,
    val updatedAt: String
)