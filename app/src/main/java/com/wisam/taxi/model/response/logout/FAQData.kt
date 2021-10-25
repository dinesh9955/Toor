package com.wisam.taxi.model.response.logout

data class FAQData (
    val _id:String?=null,
    val question:String?=null,
    val answer:String?=null,
    var isVisible:Boolean? = true
)