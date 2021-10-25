package com.wisam.taxi.view.booking.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.socket.responses.bookingAccept.BookingAcceptResponse
import com.wisam.taxi.socket.responses.usercancelresponse.UserCancelResponse
import com.wisam.taxi.view.home.activity.HomeActivity
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_bookprocess.*
import org.json.JSONObject
import java.lang.Exception

class BookingProcessingActivity : BaseActivity(), SocketListener {
    var cdt: CountDownTimer? = null
    lateinit var jsonObject : JSONObject
    var isAccept : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookprocess)
        jsonObject = JSONObject()

        startTimer()

        editor.putString("requestId",""+intent.getStringExtra("requestId")).apply()

        tvBookingCancel.setOnClickListener {
            editor.putString("requestId","")
            editor.putString("remainingTime","")
            editor.apply()
            editor.commit()

            jsonObject.put("requestId",""+intent.getStringExtra("requestId"))
            jsonObject.put("status", 0)
            sendObjectToSocket(jsonObject, "userCancelRequest")
        }
    }

    override fun onResume() {
        super.onResume()

        AppSocketListener.getInstance().restartSocket()
        AppSocketListener.getInstance().setActiveSocketListener(this)

    }

    private fun startTimer() {
        cdt = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val handler = Handler()

                kotlin.run {
                    handler.post(object : Runnable {
                        override fun run() {
                            editor.putString("remainingTime",""+System.currentTimeMillis()).apply()
                            var seconds = "" + millisUntilFinished % 60000 / 1000
                            var minutes = "" + millisUntilFinished / 60000

                            if (seconds.toInt() <= 9)
                                seconds = "0$seconds"

                            if (minutes.toInt() <= 9)
                                minutes = "0$minutes"

                            Log.d("Time","$minutes min $seconds sec")
                        }
                    })
                }
            }
            override fun onFinish() {
                Log.i("TimerService", "Timer finished")
                Toast.makeText(this@BookingProcessingActivity, "Request time out", Toast.LENGTH_SHORT).show()

                jsonObject.put("requestId",""+intent.getStringExtra("requestId"))
                jsonObject.put("status", 0)
                sendObjectToSocket(jsonObject, "userCancelRequest")
            }
        }
        cdt!!.start()
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        cdt!!.cancel()
        super.onDestroy()
    }

    override fun onPause() {
//        unregisterReceiver(mMessageReceiver)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            (mMessageReceiver), IntentFilter("mNotiDataReceiver")
        )
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            try {
                val data = intent!!.extras!!.getString("notimessage")
                val id = intent.extras!!.getString("id")
                Log.d("broadcastdata", "" + data)

                if (data.equals("Ride Confirmed", true))
                {
//                Handler().postDelayed({

                    if (!isAccept)
                    {

                        val intent = Intent(this@BookingProcessingActivity,BookingConfirmActivity::class.java)
                        intent.putExtra("bookingId",""+id)
                        intent.putExtra("intentfrom","booking")
                        startActivity(intent)
                        finish()

                    }

//                },200)

                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    private fun emitListeners() {
        try {
            if (AppSocketListener.getInstance().isSocketConnected)
            {

                AppSocketListener.getInstance().off("userCancelRequest", onuserCancelRequest)
                AppSocketListener.getInstance().addOnHandler("userCancelRequest", onuserCancelRequest)

                AppSocketListener.getInstance().off("bookingAccepted"+sharedPref.getString("id", "").toString(), onbookingAccepted)
                AppSocketListener.getInstance().addOnHandler("bookingAccepted"+sharedPref.getString("id", "").toString(), onbookingAccepted)

            }
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private val onbookingAccepted = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("socketResponse", "call: bookingAccepted   " + args[0].toString())
            val bookingAcceptResponse = Gson().fromJson(args[0].toString(), BookingAcceptResponse::class.java)
            runOnUiThread {
                cdt!!.cancel()

                if (bookingAcceptResponse.sucess && bookingAcceptResponse.acceptStatus == 1)
                {
                    editor.putString("requestId","")
                    editor.putString("remainingTime","")
                    editor.apply()
                    editor.commit()

                    isAccept = true
                    val intent = Intent(this@BookingProcessingActivity,BookingConfirmActivity::class.java)
                    intent.putExtra("bookingId",bookingAcceptResponse.data._id)
                    intent.putExtra("intentfrom","booking")
                    startActivity(intent)
                    finish()
                }
                else
                {
                    editor.putString("requestId","")
                    editor.putString("remainingTime","")
                    editor.apply()
                    editor.commit()

                    isAccept = false
                    showToast(getString(R.string.rideiscanceledbydriver))
                    navigateFinishAffinity(HomeActivity::class.java)
                }
            }
        }
    }

    private val onuserCancelRequest = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("socketResponse", "call: userCancelRequest   " + args[0].toString())
            val userCancelResponse = Gson().fromJson(args[0].toString(), UserCancelResponse::class.java)
            runOnUiThread {

                if (userCancelResponse.sucess)
                {
                    editor.putString("requestId","")
                    editor.putString("remainingTime","")
                    editor.apply()
                    editor.commit()

                    cdt!!.cancel()
                    isAccept = false
                    navigateFinishAffinity(HomeActivity::class.java)
                }

            }
        }
    }

    override fun onSocketConnected() {
        val jsonObjectAdd = JSONObject()
        jsonObjectAdd.put("userId", sharedPref.getString("id", "").toString())
        sendObjectToSocket(jsonObjectAdd, "addUser")
        emitListeners()
    }

    override fun onSocketDisconnected() {
    }

    override fun onSocketConnectionError() {
    }

    override fun onSocketConnectionTimeOut() {
    }
}
