package com.wisam.taxi.view.chooseUserType

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.GoogleService
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.welcome.activity.WelcomeLoginActivity
import kotlinx.android.synthetic.main.activity_chooseusertype.*

class ChooseUserTypeActivity : BaseActivity()
{
    var isPermit :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooseusertype)

        tvSplashLoginAsC.setOnClickListener {

            Log.d("IsPermit",""+isPermit)

            if (isPermit)
            {
                editor.putString("type","customer").apply()
                navigateWithFinish(WelcomeLoginActivity::class.java)
            }else
            {
                showLocationDialog(getString(R.string.turn_onLocation))
            }
        }

        tvSplashLoginAsD.setOnClickListener {
            Log.d("IsPermit",""+isPermit)
            if (isPermit)
            {
                editor.putString("type","driver").apply()
                navigateWithFinish(WelcomeLoginActivity::class.java)
            }else{
                showLocationDialog(getString(R.string.turn_onLocation))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initBlock()
    }

    private fun initBlock() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
//            &&
//            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ChooseUserTypeActivity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this@ChooseUserTypeActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
//                &&
//                ActivityCompat.shouldShowRequestPermissionRationale(this@ChooseUserTypeActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    )
            {
                showLocationDialog(getString(R.string.turn_onLocation))
            }
            else
            {
                ActivityCompat.requestPermissions(this@ChooseUserTypeActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
//                    ,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                    PermissionCodes.LOCATIONREQUESTCODE)
            }
        }
        else
        {
            Log.d("IsPermit",""+isPermit)
            isPermit = true
            var intent = Intent(this, GoogleService::class.java)
            startService(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionCodes.LOCATIONREQUESTCODE ->
            {
                var i = 0
                while (i < grantResults.size)
                {
                    if (grantResults.isNotEmpty() && grantResults[i] === PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("IsPermit",""+isPermit)
                        isPermit = true
                        var intent = Intent(this, GoogleService::class.java)
                        startService(intent)
                    }
                    else
                    {
                        showLocationDialog(getString(R.string.turn_onLocation))
                    }
                    i++
                }
            }
        }
    }
}
