package com.wisam.taxi.model.request

data class SocialLoginRequest (
    var phone :String,
    var fullName :String,
    var email :String,
    var countryCode :String,
    var address :String,
    var address1 :String,
    var password :String,
    var deviceId :String,
    var deviceType :String,
    var facebookId :String,
    var googleId :String
)