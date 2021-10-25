package com.wisam.taxi.interfaces

import android.location.Location

interface LocationResult {
    fun gotLocation(location: Location)
}