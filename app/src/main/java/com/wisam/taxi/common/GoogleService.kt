package com.wisam.taxi.common

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.wisam.taxi.base.WisamTaxiApplication
import java.util.*
import kotlin.collections.ArrayList

class GoogleService : Service(),LocationListener
{
    companion object {
        var str_receiver = "com.wisam.taxi.service.receiver"
    }

    internal var isGPSEnable = false
    internal var isNetworkEnable = false
    internal var locationManager: LocationManager? = null
    internal var location: Location? = null
    private val mHandler = Handler()
    private var mTimer: Timer? = null
    internal var notify_interval: Long = 5000
    internal lateinit var intent: Intent


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mTimer = Timer()
        mTimer!!.schedule(TimerTaskToGetLocation(),5,notify_interval)
        intent = Intent(str_receiver)
    }


//    override fun onLocationChanged(p0: Location) {
//        TODO("Not yet implemented")
//    }

    override fun onLocationChanged(location: Location)
    {

        WisamTaxiApplication.editor.putString("lat",""+location!!.latitude).apply()
        WisamTaxiApplication.editor.putString("long",""+location!!.longitude).apply()

//        Log.e("MyLocation","lat :"+location.latitude + "long :"+location.longitude)
        val gcd = Geocoder(applicationContext, Locale.getDefault())

        var addresses: List<Address>? = ArrayList()
        try {

            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)

            val cityName = addresses!![0].getAddressLine(0)
            val stateName = addresses[0].getAddressLine(1)
            val countryName = addresses[0].getAddressLine(2)



        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
//
//
//    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//    }
//
//    override fun onProviderEnabled(provider: String?) {
//    }
//
//    override fun onProviderDisabled(provider: String?) {
//    }

    private fun fn_update(location: Location) {

        intent.putExtra("latutide", location.latitude.toString() + "")
        intent.putExtra("longitude", location.longitude.toString() + "")
        sendBroadcast(intent)
    }

    private inner class TimerTaskToGetLocation : TimerTask() {
        override fun run()
        {
            mHandler.post { fn_getlocation() }
        }
    }

    private fun fn_getlocation()
    {
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                        &&
                        checkSelfPermission( Manifest.permission.ACCESS_BACKGROUND_LOCATION ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                }
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000,
                    0f,
                    this
                )
                if (locationManager != null) {
                    location =
                        locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (location != null)
                    {
                        fn_update(location!!)
                    }
                }

            }
            if (isGPSEnable) {
                location = null
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    0f,
                    this
                )
                if (locationManager != null) {
                    location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {
                        fn_update(location!!)
                    }
                }
            }
        }

    }

}