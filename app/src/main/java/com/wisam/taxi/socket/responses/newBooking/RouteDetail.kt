package com.wisam.taxi.socket.responses.newBooking

data class RouteDetail(
    val __v: Int,
    val _createdDate: String,
    val _id: String,
    val _isDeleted: Boolean,
    val _updatedDate: String,
    val endPoint: EndPoint,
    val endTime: Long,
    val name: String,
    val startPoint: StartPoint,
    val startTime: Long?=null,
    val stopPoints: List<Any>,
    val totalFare: Int
)