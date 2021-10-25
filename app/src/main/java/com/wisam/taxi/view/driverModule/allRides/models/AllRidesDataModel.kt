package com.wisam.taxi.view.driverModule.allRides.models

data class AllRidesDataModel(
    var sourceAddress :String,
    var destinationAddress :String,
    var time :String,
    var status :String,
    var distance :String
)