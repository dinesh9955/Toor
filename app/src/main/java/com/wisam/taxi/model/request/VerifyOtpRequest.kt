package com.wisam.taxi.model.request

data class VerifyOtpRequest(
    var countryCode : String,
    var phone :String,
    var otp :String
)