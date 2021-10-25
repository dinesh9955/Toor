package com.wisam.taxi.model.request

data class ResetPasswordRequest(
    var countryCode : String,
    var phone : String,
    var password : String
)