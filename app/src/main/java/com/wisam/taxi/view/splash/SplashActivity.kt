package com.wisam.taxi.view.splash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.GoogleService
import com.wisam.taxi.common.InternetConnectivityService
import com.wisam.taxi.socket.SocketIOService
import com.wisam.taxi.socket.SocketUrls
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.routes.RoutesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.languageSelect.LanguageSelectionActivity
import io.socket.client.IO
import io.socket.client.Socket
import java.io.IOException
import java.net.URISyntaxException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashActivity : BaseActivity() {
    lateinit var mSocket: Socket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        WisamTaxiApplication.shrdPref = getSharedPreferences("com.wisam.taxi", Context.MODE_PRIVATE)
        WisamTaxiApplication.editor = WisamTaxiApplication.shrdPref.edit()

        val background = Intent(this, SocketIOService::class.java)
        startService(background)

        try {
            mSocket = IO.socket(SocketUrls.SOCKET_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        try {
            val intent2 = Intent(context, InternetConnectivityService::class.java)
            startService(intent2)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        //######################To Get the KeyHash for Facebook#################################################
        try {
            val info = packageManager.getPackageInfo(
                "com.wisam.taxi",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
//===========================To Get the KeyHash for Facebook==================================================


        Handler().postDelayed({
            handlenavigation()
        }, 1500)


    }

    fun getSocket(): Socket {
        return mSocket
    }

    private fun handlenavigation() {

        if (!sharedPref.getString("auth", "").isNullOrEmpty())
        {
            if (sharedPref.getString("type", "customer").equals("customer", true))
                navigateWithFinish(HomeActivity::class.java)
            else
                navigateWithFinish(RoutesActivity::class.java)
        }
        else
        {
            if (!sharedPref.getString("isFirst", "").isNullOrEmpty())
                navigateWithFinish(ChooseUserTypeActivity::class.java)
            else
            {
                val langintent = Intent(this,LanguageSelectionActivity::class.java)
                langintent.putExtra("intentfrom","splash")
                startActivity(langintent)
                finish()
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Log.d("mylocationtry", sharedPref.getString("city", "")!!)
//
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(GoogleService.str_receiver))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
}
