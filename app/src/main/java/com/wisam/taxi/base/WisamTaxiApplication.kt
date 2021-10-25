package com.wisam.taxi.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.wisam.taxi.R
import com.wisam.taxi.common.LocaleManager
import com.wisam.taxi.socket.AppSocketListener

class WisamTaxiApplication : Application()

{
    companion object {
        lateinit var mContext: Context

//        var BASE_URL =  "http://appgrowthcompany.com:3008"
        var BASE_URL =  "https://api.toorapp.com"

        lateinit var shrdPref: SharedPreferences
        lateinit var editor: SharedPreferences.Editor

        fun showToastMethod() {
            Toast.makeText(mContext, mContext.getString(R.string.somethingwentwrongtryagain), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate() {
        super.onCreate()
        mContext = this

        initializeSocket(applicationContext)
    }

    private fun initializeSocket(applicationContext: Context) {
        try {
            AppSocketListener.getInstance().initialize(applicationContext)
        } catch (ex: Exception) { ex.printStackTrace() }
    }

    private fun destroySocketListener() {
        try {
            AppSocketListener.getInstance().destroy()
        } catch (ex: Exception) { ex.printStackTrace() }

    }
    override fun onTerminate() {
        super.onTerminate()
        destroySocketListener()
    }

}