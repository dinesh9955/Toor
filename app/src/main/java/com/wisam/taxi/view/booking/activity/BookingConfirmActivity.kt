package com.wisam.taxi.view.booking.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.model.response.getBookingDetailsUser.UserBookingDetailResponse
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.socket.responses.driverChangeStatus.DriverChangeStatus
import com.wisam.taxi.socket.responses.usercancelresponse.UserCancelResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.rideComplete.RideCompleteActivity
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_bookingconfirm.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BookingConfirmActivity : BaseActivity(), SocketListener {
    var isPermit: Boolean = false
    var phoneNo: String = ""
    var driverId: String = ""
    var bookingId: String = ""
    lateinit var jsonObject : JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookingconfirm)

        WisamTaxiApplication.shrdPref = getSharedPreferences("com.wisam.taxi", Context.MODE_PRIVATE)
        WisamTaxiApplication.editor = WisamTaxiApplication.shrdPref.edit()

        if (intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true))
            ivBookingConfBack.visibility = View.VISIBLE

        editor.putString("requestId","")
        editor.putString("remainingTime","")
        editor.apply()
        editor.commit()

        jsonObject = JSONObject()

        AppSocketListener.getInstance().restartSocket()
        AppSocketListener.getInstance().setActiveSocketListener(this)

        if (!intent.getStringExtra("bookingId").isNullOrEmpty())
            bookingDetails(intent.getStringExtra("bookingId")!!)

        bookingId = ""+intent.getStringExtra("bookingId")

        ivBookingConfBack.setOnClickListener {
            if (!intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true)) {
//                navigateFinishAffinity(HomeActivity::class.java)
            }
            else
                finish()
        }

        tvBooingConfCall.setOnClickListener {
            try {

                val callintent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNo"))
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    showLocationDialog(getString(R.string.turn_oncall))
                else
                    startActivity(callintent)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvCallStaus2.setOnClickListener {
            try {
                val callintent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNo"))
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showLocationDialog(getString(R.string.turn_oncall))
                } else {
                    startActivity(callintent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvBooingConfCancel.setOnClickListener {

            jsonObject.put("bookingId",""+bookingId)
            sendObjectToSocket(jsonObject, "customerCancelBooking")

//            bookingCancel("" + intent.getStringExtra("bookingId"))
        }
    }

    override fun onResume() {
        super.onResume()
        initBlock()
    }

    override fun onBackPressed() {
        if (!intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true)
        ) {
//            navigateFinishAffinity(HomeActivity::class.java)
        } else {
            finish()
        }
    }

    private fun initBlock() {
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@BookingConfirmActivity,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                showLocationDialog(getString(R.string.turn_oncall))
            }
            else
            {
                ActivityCompat.requestPermissions(
                    this@BookingConfirmActivity, arrayOf(
                        Manifest.permission.CALL_PHONE
                    ),
                    PermissionCodes.CALLREQUESTCODE
                )
            }
        } else {
            isPermit = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionCodes.CALLREQUESTCODE -> {
                var i = 0
                while (i < grantResults.size) {
                    if (grantResults.isNotEmpty() && grantResults[i] === PackageManager.PERMISSION_GRANTED) {
                        isPermit = true
                    } else {
                    }
                    i++
                }
            }
        }
    }

    private fun bookingCancel(data: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance

        val getData: MutableLiveData<LogoutResponse>
        getData = repository.userCancelBooking(sharedPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(),data)
        getData.observe(this, object : Observer<LogoutResponse> {
            override fun onChanged(t: LogoutResponse?) {
                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            showToast(t.response.message)
                            if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
                                intent.getStringExtra("intentfrom").equals("booking", true)
                            ) {
                                navigateFinishAffinity(HomeActivity::class.java)
                            } else {
                                finish()
                            }
                        }
                        t.response.message.isEmpty() -> {
                            if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
                                intent.getStringExtra("intentfrom").equals("booking", true)
                            ) {
                                navigateFinishAffinity(HomeActivity::class.java)
                            } else {
                                finish()
                            }
                            showToast(t.response.message)
                            dismissprogressBar()
                        }
                        else -> {
                            showToast(t.response.message)

                            if (t.response.message.equals("Authorization not correct", true)) {
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                               clearnotification()
                                showToast(resources.getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else if (t.response.logout == 1){
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                               clearnotification()
                                showToast(resources.getString(R.string.sessionexpired))
                               navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else
                                showToast(t.response.message)

                            if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
                                intent.getStringExtra("intentfrom").equals("booking", true))
                                navigateFinishAffinity(HomeActivity::class.java)
                            else
                                finish()
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()

                    if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
                        intent.getStringExtra("intentfrom").equals("booking", true)
                    )
                        navigateFinishAffinity(HomeActivity::class.java)
                    else
                        finish()

                    e.printStackTrace()
                }


            }
        })
    }

    private fun bookingDetails(data: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance

        val getData: MutableLiveData<UserBookingDetailResponse>
        getData = repository.usergetBookingDetails(sharedPref.getString("auth", "").toString(),
                sharedPref.getString("mylang", "en").toString(),data)

        getData.observe(this, object : Observer<UserBookingDetailResponse> {
            override fun onChanged(t: UserBookingDetailResponse?) {

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            clMain.visibility = View.VISIBLE
                            clNoData.visibility = View.GONE

                            try {
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val bookingTime = sdf.format(t.data.date)
                                val bookingdate = sdf.parse(bookingTime)
                                sdf = SimpleDateFormat("EEEE dd MMM, yyyy")
                                val date = sdf.format(bookingdate)
                                sdf = SimpleDateFormat("h:mm a")
                                val startTime = sdf.format(bookingdate)


                                val tiemInMin = getDistance( t.data.source.latitude, t.data.source.longitude, t.data.destination.latitude
                                    , t.data.destination.longitude)

                                phoneNo = t.data.driverId.phone
                                tvSource.text = t.data.source.address
                                tvDestination.text = t.data.destination.address
                                tvBooingConfSource.text = t.data.source.address
                                tvBooingConfDestination.text = t.data.destination.address
                                tvDrivername.text = t.data.driverId.fullName
                                tvBooingConfRating.text = "" + DecimalFormat("#.#").format(t.data.ratings)
                                tvBooingConfPas.text = "" + t.data.seats + " "+resources.getString(R.string.passengers)
                                tvBooingConfTotalFairVal.text = "" + t.data.totalAmount
                                tvBooingConfValue.text = "" + t.data.totalAmount
                                tvBooingConfDurationVal.text = "$tiemInMin "+getString(R.string.stringmin)
                                tvBooingConfTimeVa.text = "$tiemInMin "+getString(R.string.stringmin)
                                tvBooingConfTime.text = "$date, $startTime"

                                val profile =""+t.data.driverId.profilePic

                                if (profile.isEmpty())
                                    ivBooingConfProfile.setImageResource(R.drawable.group_445)
                                else
                                    Picasso.get().load(WisamTaxiApplication.BASE_URL+""+profile).placeholder(R.drawable.group_445).into(ivBooingConfProfile)

                                driverId = t.data.driverId.id


                                tvBooingConfDistanceVal.text = "" + DecimalFormat("#######.##").format(t.data.distance) + getString(R.string.stringkm)

                                AppSocketListener.getInstance().restartSocket()
                                AppSocketListener.getInstance().setActiveSocketListener(this@BookingConfirmActivity)

                                if (t.data.status == 2  || t.data.status == 3)
                                {
                                    tvBooingConfCancel.visibility = View.INVISIBLE
                                    tvBooingConfCall.visibility = View.INVISIBLE
                                    tvCallStaus2.visibility = View.VISIBLE
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                            clMain.visibility = View.GONE
                            clNoData.visibility = View.VISIBLE
                        }
                        else -> {
                            dismissprogressBar()
                            clMain.visibility = View.GONE
                            clNoData.visibility = View.VISIBLE

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(resources.getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(resources.getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> showToast(t.response.message)

                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    clMain.visibility = View.GONE
                    clNoData.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }
        })
    }

    private fun emitListeners() {
        try {
            if (AppSocketListener.getInstance().isSocketConnected) {

                AppSocketListener.getInstance().off("customerCancelBooking", oncustomerCancelBooking)
                AppSocketListener.getInstance().addOnHandler("customerCancelBooking", oncustomerCancelBooking)

                AppSocketListener.getInstance().off("driverChangeStatus$driverId", onDriverChangeStatus)
                AppSocketListener.getInstance().addOnHandler("driverChangeStatus$driverId", onDriverChangeStatus)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private val oncustomerCancelBooking = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("socketResponse", "call: userCancelRequest   " + args[0].toString())
            val userCancelResponse = Gson().fromJson(args[0].toString(), UserCancelResponse::class.java)
            runOnUiThread {

                if (userCancelResponse.sucess)
                {
                    if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
                        intent.getStringExtra("intentfrom").equals("booking", true)
                    ) {
                        navigateFinishAffinity(HomeActivity::class.java)
                    } else {
                        finish()
                    }
                }

            }
        }
    }

    private val onDriverChangeStatus = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onDriverChangeStatus", "call: driverChangeStatus   " + args[0].toString())
            val driverChangeStatus: DriverChangeStatus = Gson().fromJson(args[0].toString(), DriverChangeStatus::class.java)
            runOnUiThread {

                if (driverChangeStatus.sucess)
                {
                    tvBooingConfCancel.visibility = View.INVISIBLE
                    tvBooingConfCall.visibility = View.INVISIBLE
                    tvCallStaus2.visibility = View.VISIBLE

                    if (driverChangeStatus.status == 3)
                    {
                        val intent = Intent(this@BookingConfirmActivity, RideCompleteActivity::class.java)
                        intent.putExtra("bookingId", bookingId)
                        intent.putExtra("driverId", driverChangeStatus.booking.driverId._id)
                        intent.putExtra("name", driverChangeStatus.booking.driverId.fullName)
                        intent.putExtra("profilePic", driverChangeStatus.booking.driverId.profilePic)
                        intent.putExtra("rating",""+driverChangeStatus.booking.driverRating)
                        startActivity(intent)
                    }
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

    override fun onStart() {
        super.onStart()
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//            (mMessageReceiver),
//            IntentFilter("mNotiDataReceiver")
//        )
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.extras?.getString("notimessage")?:""
            val id = intent?.extras?.getString("id")?:""

//            if (data.equals("Ride Reached", true)) {
//
//            }
        }

    }

}
