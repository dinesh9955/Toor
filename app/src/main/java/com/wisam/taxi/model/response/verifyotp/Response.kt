package com.wisam.taxi.model.response.verifyotp

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)