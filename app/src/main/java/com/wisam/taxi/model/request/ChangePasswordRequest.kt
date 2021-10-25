package com.wisam.taxi.model.request

data class ChangePasswordRequest (
    var oldPassword : String,
    var password :String
)