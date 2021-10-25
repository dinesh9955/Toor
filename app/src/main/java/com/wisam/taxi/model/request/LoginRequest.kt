package com.wisam.taxi.model.request

data class LoginRequest(
    var countryCode : String,
    var phone : String,
    var password : String,
    var deviceId : String,
    var deviceType : String
)