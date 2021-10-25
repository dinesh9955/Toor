package com.wisam.taxi.model.driverRequest

data class RateToUser (
    var userId:String,
    var bookingId:String,
    var review:String,
    var rating:Double
)