package com.wisam.taxi.model.response.userhome

data class Vehicletypes(
    val __v: Int,
    val _id: String,
    val basefare: Int,
    val createdAt: String,
    val date: Long,
    val fare: Int,
    val image: String,
    val name: String,
    val seatType: String,
    val seats: Int,
    val status: Int,
    val updatedAt: String
)