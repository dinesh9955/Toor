package com.wisam.taxi.model.response.booking

data class UserId(
    val _id: String,
    val countryCode: String,
    val fullName: String,
    val id: String,
    val phone: String,
    val profilePic: String
)