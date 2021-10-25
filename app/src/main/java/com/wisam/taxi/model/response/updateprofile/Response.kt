package com.wisam.taxi.model.response.updateprofile

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)