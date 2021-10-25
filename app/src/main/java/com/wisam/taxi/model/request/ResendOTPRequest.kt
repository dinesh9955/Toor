package com.wisam.taxi.model.request

data class ResendOTPRequest(
    var countryCode : String,
    var phone : String
)