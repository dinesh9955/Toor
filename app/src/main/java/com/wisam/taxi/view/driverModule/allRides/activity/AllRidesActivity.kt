package com.wisam.taxi.view.driverModule.allRides.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.model.driverRequest.DriverSelectRoutesRequest
import com.wisam.taxi.model.driverResponse.getbookingDetails.GetBookingDetailsDriver
import com.wisam.taxi.model.driverResponse.selectActiveRoute.SelectActiveRouteResponse
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.socket.responses.driverChangeStatus.DriverChangeStatus
import com.wisam.taxi.socket.responses.driverchangestatusId.CustomerRideCancelResponse
import com.wisam.taxi.socket.responses.newBooking.NewBookingResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.allRides.adapter.AllRidesRvAdapter
import com.wisam.taxi.view.driverModule.allRides.models.AllRidesDataModel
import com.wisam.taxi.view.driverModule.rateToCustomer.RateToCustomerActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_allrides.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class AllRidesActivity : BaseActivity(), RvClickPostion, SocketListener {
    lateinit var adapter: AllRidesRvAdapter
    private lateinit var allRidesList: ArrayList<AllRidesDataModel>
    var isComplete: Boolean = false
    var position: Int = 0
    private lateinit var jsonObject: JSONObject
    lateinit var newBookingResponse: NewBookingResponse
    var phoneNo: String = ""
    var userId: String = ""
    var bookingId: String = ""
    var name: String = ""
    var profilePic: String = ""
    var rating: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allrides)

        editor.putString("bookingdata","").apply()
        editor.commit()

        if (!intent.getStringExtra("bookingId").isNullOrEmpty())
            bookingDetails(intent.getStringExtra("bookingId")?:"")

        bookingId = intent.getStringExtra("bookingId")?:""


        AppSocketListener.getInstance().restartSocket()
        AppSocketListener.getInstance().setActiveSocketListener(this)

        ivAllRidesBack.setOnClickListener {
            if (isComplete)
            {
                if (!intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true))
                    navigateFinishAffinity(HomeActivity::class.java)
                else
                    finish()
            }
        }
        allRidesList = ArrayList()
        jsonObject = JSONObject()

        adapter = AllRidesRvAdapter(this, allRidesList, this)
        rvAllRides.adapter = adapter
        rvAllRides.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        tvAllRidesDone.setOnClickListener {
            if (isComplete)
            {
                val intent = Intent(this@AllRidesActivity,RateToCustomerActivity::class.java)
                intent.putExtra("bookingId",bookingId)
                intent.putExtra("userId",userId)
                intent.putExtra("name", name)
                intent.putExtra("profilePic", profilePic)
                intent.putExtra("rating", ""+rating)
                startActivity(intent)
            }
        }

        tvAcceptRideAccept.setOnClickListener {

            jsonObject.put("requestId", newBookingResponse.requestId)
            jsonObject.put("status", 1)
            jsonObject.put("userId", newBookingResponse.userId)
            sendObjectToSocket(jsonObject, "requestAction")

        }

        tvBooingConfCancel.setOnClickListener {

            jsonObject.put("requestId", newBookingResponse.requestId)
            jsonObject.put("status", 2)
            jsonObject.put("userId", newBookingResponse.userId)
            sendObjectToSocket(jsonObject, "requestAction")

            emitListeners()

            clRideReqest.visibility = View.INVISIBLE
            clTranspbg.visibility = View.INVISIBLE

            val bottom = AnimationUtils.loadAnimation(this@AllRidesActivity, R.anim.top_to_bottom)
            clRideReqest.animation = bottom
        }

        cvAllridesPhone.setOnClickListener {
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

        tvAllRidesStatwithGreen.setOnClickListener {

            jsonObject.put("bookingId", "" + intent.getStringExtra("bookingId"))
            jsonObject.put("status", 2)
            sendObjectToSocket(jsonObject, "driverChangeStatus")
            emitListeners()

        }
        tvAllRidesStatus.setOnClickListener {

        }
        tvAllRidesStatwithBg.setOnClickListener {

            jsonObject.put("bookingId", "" + intent.getStringExtra("bookingId"))
            jsonObject.put("status", 3)
            sendObjectToSocket(jsonObject, "driverChangeStatus")

            emitListeners()
        }
    }

    override fun onResume() {
        super.onResume()
        initBlock()
    }

    private fun initBlock() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@AllRidesActivity, Manifest.permission.CALL_PHONE)) {
//                showLocationDialog(getString(R.string.turn_oncall))
            }
            else
                ActivityCompat.requestPermissions(this@AllRidesActivity, arrayOf(Manifest.permission.CALL_PHONE), PermissionCodes.CALLREQUESTCODE)

        }
    }

    private fun selectActiveRouteDriver(routeId: String) {
        showprogressbar()

        val repository: RetroRepository = RetroRepository.instance
        val drivergetRoutes: MutableLiveData<SelectActiveRouteResponse>
        val request = DriverSelectRoutesRequest(routeId)
        drivergetRoutes = repository.selectActiveRoute(WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(),request)

        drivergetRoutes.observe(this, object : Observer<SelectActiveRouteResponse> {
            override fun onChanged(t: SelectActiveRouteResponse?) {
                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            finish()
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()

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
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                }

            }
        })
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
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
//                    if (grantResults.isNotEmpty() && grantResults[i] === PackageManager.PERMISSION_GRANTED) {
//                    }
//                    else {
//                    }
                    i++
                }
            }
        }
    }

    override fun onBackPressed()
    {
        if (isComplete)
        {
            if (!intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true))
                navigateFinishAffinity(HomeActivity::class.java)
            else
                finish()
        }
    }


    private fun bookingDetails(data: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance

        val getData: MutableLiveData<GetBookingDetailsDriver>
        getData = repository.getBookingDetailsDriver(sharedPref.getString("auth", "").toString(),
                sharedPref.getString("mylang", "en").toString(),data)
        getData.observe(this, object : Observer<GetBookingDetailsDriver> {
            override fun onChanged(t: GetBookingDetailsDriver?) {

                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            clMain.visibility = View.VISIBLE
                            clNoData.visibility = View.GONE
                            tvAllRidesDone.visibility = View.VISIBLE

                            try {
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val bookingTime = sdf.format(t.data.date)
                                val bookingTimeDate = sdf.parse(bookingTime)

                                sdf = SimpleDateFormat("MMM dd, yyyy")
                                val date = sdf.format(bookingTimeDate)

                                sdf = SimpleDateFormat("h:mm a")
                                val startTime = sdf.format(bookingTimeDate)

                                phoneNo = t.data.userId.phone
                                tvallRidesSource.text = t.data.source.address
                                tvAllRidesDestination.text = t.data.destination.address
                                ivAllRidesRvAddress.text = t.data.userId.address + " " + t.data.userId.address1
                                ivAllRidesRvName.text = t.data.userId.fullName
                                tvAllRidesTime.text = "$date, $startTime"


                                tvAllRidesDistance.text = "" + DecimalFormat("#######.##").format(t.data.distance) + " "+resources.getString(R.string.stringkm)

                                val profile =""+t.data.userId.profilePic

                                if (profile.isEmpty())
                                    ivCustomerProfile.setImageResource(R.drawable.group_445)
                                else
                                    Picasso.get().load(WisamTaxiApplication.BASE_URL+""+profile).placeholder(R.drawable.group_445).into(ivCustomerProfile)

                                userId = t.data.userId._id
                                name = t.data.userId.fullName
                                profilePic = t.data.userId.profilePic
                                rating = t.data.ratings

                                when (t.data.status) {
                                    2 -> {
                                        tvAllRidesStatus.visibility = View.INVISIBLE
                                        tvAllRidesStatwithGreen.visibility = View.INVISIBLE
                                        tvAllRidesStatwithBg.visibility = View.VISIBLE
                                        tvAllRidesStatwithBg.text = resources.getString(R.string.complete)
                                        tvAllRidesStatwithGreen.text = resources.getString(R.string.complete)
                                        tvAllRidesStatus.text = resources.getString(R.string.complete)
                                        tvAllRidesDone.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
                                        tvAllRidesDone.isClickable = false
                                    }
                                    0 -> {
                                        tvAllRidesStatus.visibility = View.INVISIBLE
                                        tvAllRidesStatwithGreen.visibility = View.VISIBLE
                                        tvAllRidesStatwithBg.visibility = View.INVISIBLE
                                        tvAllRidesStatwithGreen.text = resources.getString(R.string.start)
                                        tvAllRidesStatus.text = resources.getString(R.string.start)
                                        tvAllRidesStatwithBg.text = resources.getString(R.string.start)
                                        tvAllRidesDone.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
                                        tvAllRidesDone.isClickable = false
                                    }
                                    else -> {
                                        ivAllRidesBack.visibility = View.VISIBLE
                                        tvAllRidesStatus.visibility = View.VISIBLE
                                        tvAllRidesStatwithGreen.visibility = View.INVISIBLE
                                        tvAllRidesStatwithBg.visibility = View.INVISIBLE
                                        tvAllRidesStatus.text = resources.getString(R.string.completed)
                                        tvAllRidesStatwithBg.text = resources.getString(R.string.completed)
                                        tvAllRidesStatwithGreen.text = resources.getString(R.string.completed)
                                        tvAllRidesDone.setBackgroundResource(R.drawable.round_corner_bg)
                                        tvAllRidesDone.isClickable = true
                                        isComplete = true
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                            isComplete = true

                            clMain.visibility = View.INVISIBLE
                            clNoData.visibility = View.VISIBLE
                            tvAllRidesDone.visibility = View.INVISIBLE

                        }
                        else -> {
                            dismissprogressBar()
                            clMain.visibility = View.INVISIBLE
                            clNoData.visibility = View.VISIBLE
                            tvAllRidesDone.visibility = View.INVISIBLE
                            isComplete = true
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
                    isComplete = true
                    clMain.visibility = View.INVISIBLE
                    clNoData.visibility = View.VISIBLE
                    tvAllRidesDone.visibility = View.INVISIBLE

                    e.printStackTrace()
                }
            }
        })
    }


    override fun onItemClicked(pos: Int) {
    }

    override fun onSocketConnected() {
        jsonObject.put("driverId", sharedPref.getString("id_driver", "").toString())
        sendObjectToSocket(jsonObject, "addDriver")
        emitListeners()
    }

    override fun onSocketDisconnected() {
    }

    override fun onSocketConnectionError() {
    }

    override fun onSocketConnectionTimeOut() {
    }

    private fun emitListeners() {
        try {
            if (AppSocketListener.getInstance().isSocketConnected) {
                AppSocketListener.getInstance().off("newBooking", onNewBooking)
                AppSocketListener.getInstance().addOnHandler("newBooking", onNewBooking)

                AppSocketListener.getInstance().off("requestAction", onRequestAction)
                AppSocketListener.getInstance().addOnHandler("requestAction", onRequestAction)

                AppSocketListener.getInstance().off("driverChangeStatus", onDriverChangeStatus)
                AppSocketListener.getInstance().addOnHandler("driverChangeStatus", onDriverChangeStatus)

                AppSocketListener.getInstance().off("driverChangeStatus"+sharedPref.getString("id_driver", ""),
                    onDriverChangeStatusID)
                AppSocketListener.getInstance().addOnHandler("driverChangeStatus"+sharedPref.getString("id_driver", ""),
                    onDriverChangeStatusID)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }


    private val onNewBooking = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onNewBooking", "call: newBooking   " + args[0].toString())
            newBookingResponse = Gson().fromJson(args[0].toString(), NewBookingResponse::class.java)
//            runOnUiThread {
//
//                tvAcceptRideName.text = "" + newBookingResponse.user.fullName
//                Picasso.get()
//                    .load(WisamTaxiApplication.BASE_URL + "" + newBookingResponse.user.profilePic)
//                    .placeholder(circularProgressDrawable).into(ivAcceptRide)
//
//                clTranspbg.visibility = View.VISIBLE
//                clRideReqest.visibility = View.VISIBLE
//
//                val top = AnimationUtils.loadAnimation(
//                    this@AllRidesActivity,
//                    R.anim.bottom_to_top_animation
//                )
//                clRideReqest.animation = top
//
//                top.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationRepeat(animation: Animation?) {
//                    }
//
//                    override fun onAnimationEnd(animation: Animation?) {
//                    }
//
//                    override fun onAnimationStart(animation: Animation?) {
//                    }
//                })
//
//            }
        }
    }

    private val onRequestAction = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onRequestAction", "call: requestAction   " + args[0].toString())
//            var userOnLineResponse: GetSumResponse = Gson().fromJson(args[0].toString(), GetSumResponse::class.java)
            runOnUiThread {

            }
        }
    }

    private val onDriverChangeStatus = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onDriverChangeStatus", "call: driverChangeStatus   " + args[0].toString())
            val driverChangeStatus: DriverChangeStatus = Gson().fromJson(args[0].toString(), DriverChangeStatus::class.java)

            runOnUiThread {
                if (driverChangeStatus.sucess)
                    bookingDetails(intent.getStringExtra("bookingId")!!)

            }
        }
    }

    private val onDriverChangeStatusID = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onDriverChangeStatus", "call: driverChangeStatusID   " + args[0].toString())
            val customerRideCanceResponse = Gson().fromJson(args[0].toString(), CustomerRideCancelResponse::class.java)

            runOnUiThread {
                if (customerRideCanceResponse.sucess && customerRideCanceResponse.status == 11)
                {
                    showToast(resources.getString(R.string.ridecanceledbycustomer))

                    if (!intent.getStringExtra("intentfrom").isNullOrEmpty() && intent.getStringExtra("intentfrom").equals("booking", true))
                        navigateFinishAffinity(HomeActivity::class.java)
                    else
                        finish()

                }
            }
        }
    }
}
