package com.wisam.taxi.model.response.userhome

data class Route(
    val __v: Int,
    val _createdDate: String,
    val _id: String,
    val _isDeleted: Boolean,
    val _updatedDate: String,
    val driver: ArrayList<Driver>,
    val endPoint: List<EndPoint>,
    val endTime: String,
    val name: String,
    val startPoint: List<StartPoint>,
    val startTime: String,
    val stopPoints: List<Any>,
    val stops: List<Any>,
    val totalFare: Double
)