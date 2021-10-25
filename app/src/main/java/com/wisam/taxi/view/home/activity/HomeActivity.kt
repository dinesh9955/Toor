package com.wisam.taxi.view.home.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.common.LocaleManager
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.services.TimerService
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.view.home.fragments.*
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject


class HomeActivity : BaseActivity(), SocketListener
{
    var bookingData : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        AppSocketListener.getInstance().setActiveSocketListener(this)

        val jsonObjectAdd = JSONObject()

        if (sharedPref.getString("type","").equals("driver",true))
        {
            jsonObjectAdd.put("driverId", sharedPref.getString("id_driver", "").toString())
            sendObjectToSocket(jsonObjectAdd, "addDriver")

            bookingData = sharedPref.getString("bookingdata","")?:""
        }
        else
        {
            jsonObjectAdd.put("userId", sharedPref.getString("id", "").toString())
            sendObjectToSocket(jsonObjectAdd, "addUser")
        }


        loadFrag(ExploreFragment())
        onExploreLoad()
        editor.putString("screen","home").apply()

        clivExplore.setOnClickListener {

            if (!sharedPref.getString("screen","").equals("home",true))
            {
                onExploreLoad()
                loadFrag(ExploreFragment())
                editor.putString("screen","home").apply()
            }

        }

        clMyTripBtn.setOnClickListener {
            if (!sharedPref.getString("screen","").equals("trip",true)) {
                onMyTripLoad()
                loadFrag(MyTripFragment())
                editor.putString("screen", "trip").apply()
            }
        }


        clSettingsButton.setOnClickListener {
            if (!sharedPref.getString("screen","").equals("setting",true)) {
                onSettingsLoad()
                loadFrag(SettingFragment())
                editor.putString("screen", "setting").apply()
            }
        }


        clprofileBtn.setOnClickListener {
            if (!sharedPref.getString("screen","").equals("account",true))
            {
                onAccountLoad()
                if (sharedPref.getString("type", "driver").equals("driver", true)) {
                    loadFrag(DriverAccountFragment())
                } else {
                    loadFrag(AccountFragment())
                }
                editor.putString("screen", "account").apply()
            }
        }
    }


    private fun onExploreLoad() {
        ivExplore.setImageResource(R.drawable.glyph_copy)
        tvExplore.setTextColor(Color.parseColor("#0b8793"))

        ivMyTrip.setImageResource(R.drawable.shape)
        tvMyTrip.setTextColor(Color.parseColor("#000000"))

        ivSettings.setImageResource(R.drawable.settings)
        tvSettings.setTextColor(Color.parseColor("#000000"))

        ivprofileBtn.setImageResource(R.drawable.account003)
        tvprofileBtn.setTextColor(Color.parseColor("#000000"))
    }

    private fun onMyTripLoad() {
        ivExplore.setImageResource(R.drawable.glyph)
        tvExplore.setTextColor(Color.parseColor("#000000"))

        ivMyTrip.setImageResource(R.drawable.trip_copy)
        tvMyTrip.setTextColor(Color.parseColor("#0b8793"))

        ivSettings.setImageResource(R.drawable.settings)
        tvSettings.setTextColor(Color.parseColor("#000000"))

        ivprofileBtn.setImageResource(R.drawable.account003)
        tvprofileBtn.setTextColor(Color.parseColor("#000000"))
    }

    private fun onSettingsLoad() {
        ivExplore.setImageResource(R.drawable.glyph)
        tvExplore.setTextColor(Color.parseColor("#000000"))

        ivMyTrip.setImageResource(R.drawable.shape)
        tvMyTrip.setTextColor(Color.parseColor("#000000"))

        ivSettings.setImageResource(R.drawable.setting)
        tvSettings.setTextColor(Color.parseColor("#0b8793"))

        ivprofileBtn.setImageResource(R.drawable.account003)
        tvprofileBtn.setTextColor(Color.parseColor("#000000"))
    }

    private fun onAccountLoad() {
        ivExplore.setImageResource(R.drawable.glyph)
        tvExplore.setTextColor(Color.parseColor("#000000"))

        ivMyTrip.setImageResource(R.drawable.shape)
        tvMyTrip.setTextColor(Color.parseColor("#000000"))

        ivSettings.setImageResource(R.drawable.settings)
        tvSettings.setTextColor(Color.parseColor("#000000"))

        ivprofileBtn.setImageResource(R.drawable.account)
        tvprofileBtn.setTextColor(Color.parseColor("#0b8793"))
    }


    private fun loadFrag(fragment: Fragment) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.homeFrameLayout, fragment)
        transaction.commit()
    }

    private val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateGUI(intent!!)
        }
    }

    override fun onResume() {
        setLanguage(this)
        super.onResume()
        registerReceiver(br, IntentFilter(TimerService.COUNTDOWN_BR))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(br)
    }

    override fun onStop() {
        try {
            unregisterReceiver(br)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }


    private fun updateGUI(intent: Intent) {
        if (intent.extras != null) {
            val millisUntilFinished = intent.getLongExtra("countdown", 0)
        }
    }

    override fun onSocketConnected() {
    }

    override fun onSocketDisconnected() {
    }

    override fun onSocketConnectionError() {
    }

    override fun onSocketConnectionTimeOut() {
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.homeFrameLayout) is ExploreFragment)
            super.onBackPressed()
        else
        {
            loadFrag(ExploreFragment())
            onExploreLoad()
            editor.putString("screen","home").apply()
        }
    }

}
