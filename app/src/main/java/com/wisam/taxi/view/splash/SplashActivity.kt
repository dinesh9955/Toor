package com.wisam.taxi.view.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import com.patchoguefd.util.Utility
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

    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1999

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
            if (Utility.checkAndRequestPermissions(
                    this@SplashActivity,
                    REQUEST_ID_MULTIPLE_PERMISSIONS
                )
            ) {
                // requestPermission();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        handlenavigation()
                    } else {
                        showDialogOK(this@SplashActivity,
                            "Allow permission for storage access!",
                            DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> requestPermission()
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                    }
                                }
                            })
                    }
                }
            } else {
            }
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
                val langintent = Intent(this, LanguageSelectionActivity::class.java)
                langintent.putExtra("intentfrom", "splash")
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




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                if (Utility.checkAdditionPermissionsCheck(
                        this@SplashActivity,
                        permissions as Array<String>,
                        grantResults,
                        REQUEST_ID_MULTIPLE_PERMISSIONS
                    )
                ) {
                    //requestPermission();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            handlenavigation()
                        } else {
                            showDialogOK(this,
                                "Allow permission for storage access!",
                                DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> requestPermission()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }
                                })
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("NewApi")
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                handlenavigation()
            } else {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(
                            java.lang.String.format(
                                "package:%s",
                                applicationContext.packageName
                            )
                        )
                    startActivityForResult(intent, 2296)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, 2296)
                }
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this@SplashActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                234
            )
        }
    }


    private fun showDialogOK(
        splashScreen: Activity,
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(splashScreen)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("CANCEL", okListener)
            .create()
            .show()
    }


    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    //Toast.makeText(this, "Allow permission for storage access!111111111", Toast.LENGTH_SHORT).show();
                    handlenavigation()
                } else {
                    //Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    showDialogOK(this,
                        "Allow permission for storage access!",
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> requestPermission()
                                DialogInterface.BUTTON_NEGATIVE -> {
                                }
                            }
                        })
                }
            }
        }
    }
}
