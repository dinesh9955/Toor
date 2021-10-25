package com.wisam.taxi.model.response.login

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)