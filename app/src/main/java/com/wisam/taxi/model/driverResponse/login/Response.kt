package com.wisam.taxi.model.driverResponse.login

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)