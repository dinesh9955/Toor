package com.wisam.taxi.model.response.resendotp

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)