package com.wisam.taxi.model.driverResponse.register

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)