package com.wisam.taxi.model.driverResponse.getdocuments

data class Response(
    val isUser: Int,
    val logout: Int,
    val message: String,
    val success: Boolean
)