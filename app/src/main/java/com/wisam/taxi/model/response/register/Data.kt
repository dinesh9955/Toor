package com.wisam.taxi.model.response.register

data class Data(
    val __v: Int,
    val _id: String,
    val address: String,
    val address1: String,
    val authToken: String,
    val countryCode: String,
    val createdAt: String,
    val date: Long,
    val email: String,
    val facebookId: String,
    val fullName: String,
    val googleId: String,
    val hash: String,
    val id: String,
    val isSocialRegister: Int,
    val isVerified: Boolean,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val profilePic: String,
    val sendNoti: Int,
    val status: Int,
    val updatedAt: String
)