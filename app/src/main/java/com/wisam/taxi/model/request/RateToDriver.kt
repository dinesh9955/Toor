package com.wisam.taxi.model.request

data class RateToDriver(
    var driverId:String,
    var bookingId:String,
    var review:String,
    var rating:Double
)