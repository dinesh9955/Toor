package com.wisam.taxi.view.driverModule.chooseActive

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.model.driverRequest.DriverSelectRoutesRequest
import com.wisam.taxi.model.driverResponse.selectActiveRoute.SelectActiveRouteResponse
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.socket.responses.newBooking.NewBookingResponse
import com.wisam.taxi.socket.responses.requestAction.RequestActionResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.allRides.activity.AllRidesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chooseactive.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat


class ChooseActiveActivity : BaseActivity(), SocketListener {

    private var time: CountDownTimer? = null
    private lateinit var jsonObject: JSONObject
    private lateinit var newBookingResponse: NewBookingResponse
    var remainingtimemili: Long = 900000
    var click: Int = 0
    private var isFromNoti:Boolean = false

    companion object{
        var  isAppOpen:Boolean = false
    }


    override fun onResume() {
        super.onResume()
        isAppOpen = true
        try { notificationManager.cancel(89) }catch (e:Exception){e.printStackTrace()}
    }

    override fun onPause() {
        super.onPause()
        isAppOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooseactive)

        jsonObject = JSONObject()

        AppSocketListener.getInstance().restartSocket()
        AppSocketListener.getInstance().setActiveSocketListener(this)

        if (!intent.getStringExtra("route").isNullOrEmpty())
        {
            if (!intent.getStringExtra("remainingtime").isNullOrEmpty() &&
                !intent.getStringExtra("remainingtime").equals("0",true))
            {
                val remainingtime = ""+intent.getStringExtra("remainingtime")
                switchChooseActive.isChecked = true
                startTime(remainingtime.toLong())
                tvSwitchChooseActive.visibility = View.VISIBLE
            }
            else {
                switchChooseActive.isChecked = false
                tvSwitchChooseActive.visibility = View.INVISIBLE
            }
        }

        ivChooseActiveBack.setOnClickListener {
            if (!switchChooseActive.isChecked)
            {
                if (isFromNoti)
                    navigateFinishAffinity(HomeActivity::class.java)
                else
                    finish()
            }
        }

        tvAcceptRideAccept.setOnClickListener {
            click = 1
            jsonObject.put("requestId", newBookingResponse.requestId)
            jsonObject.put("status", 1)
            jsonObject.put("userId", newBookingResponse.userId)
            sendObjectToSocket(jsonObject, "requestAction")
            emitListeners()
        }

        tvBooingConfCancel.setOnClickListener {

            click = 0
            jsonObject.put("requestId", newBookingResponse.requestId)
            jsonObject.put("status", 2)
            jsonObject.put("userId", newBookingResponse.userId)
            sendObjectToSocket(jsonObject, "requestAction")

        }


        try {

            switchChooseActive.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                {
                    selectActiveRouteDriver("" + intent.getStringExtra("route"))
                    tvSwitchChooseActive.visibility = View.VISIBLE
                    tvChooseActiveNext.setBackgroundResource(R.drawable.round_corner_bg)
                    try { time!!.cancel() }catch (e:Exception){e.printStackTrace()}

                } else {
                    selectActiveRouteDriver("")
                    time!!.cancel()
                    tvSwitchChooseActive.visibility = View.INVISIBLE
                    tvChooseActiveNext.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
                    remainingtimemili = 900000
                    tvChooseActiveTime?.text = "15:00 "+getString(R.string.stringmin)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (intent.hasExtra("bookingData"))
        {
            if (!intent.getStringExtra("bookingData").isNullOrEmpty())
            {
                isFromNoti = true

                Handler(Looper.getMainLooper()).postDelayed({
                    onNewBookingRequestArrived(intent.getStringExtra("bookingData")?:"")
                },500)
            }
        }
    }


    fun startTime(remaintime: Long) {
        time = object : CountDownTimer(remaintime, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {

                var seconds = "" + millisUntilFinished % 60000 / 1000
                var minutes = "" + millisUntilFinished / 60000

                if (seconds.toInt() <= 9)
                    seconds = "0$seconds"

                if (minutes.toInt() <= 9)
                    minutes = "0$minutes"

                tvChooseActiveTime?.text = "$minutes:$seconds "+getString(R.string.stringmin)
                remainingtimemili = millisUntilFinished
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                tvChooseActiveTime?.text = "15:00 "+getString(R.string.stringmin)
                selectActiveRouteDriver("")

                tvSwitchChooseActive.visibility = View.INVISIBLE
                tvChooseActiveNext.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
                remainingtimemili = 900000
                switchChooseActive.isChecked = false

            }
        }.start()
    }

    private fun emitListeners() {
        try {
            if (AppSocketListener.getInstance().isSocketConnected) {
                AppSocketListener.getInstance().off("newBooking", onNewBooking)
                AppSocketListener.getInstance().addOnHandler("newBooking", onNewBooking)

                AppSocketListener.getInstance().off("requestAction", onRequestAction)
                AppSocketListener.getInstance().addOnHandler("requestAction", onRequestAction)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private val onNewBooking = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onNewBooking", "call: newBooking   " + args[0].toString())
            runOnUiThread {
              onNewBookingRequestArrived(args[0].toString())
            }
        }
    }

    fun onNewBookingRequestArrived(bookingdata:String){
        try {
            newBookingResponse = Gson().fromJson(bookingdata, NewBookingResponse::class.java)
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val bookingstartTime = sdf.format(newBookingResponse.routeDetail.startTime)
            val startDate = sdf.parse(bookingstartTime)
            sdf = SimpleDateFormat("h:mm a")
            val timeformat = sdf.format(startDate)

            val distance = haversine( newBookingResponse.routeDetail.startPoint.coordinates[0], newBookingResponse.routeDetail.startPoint.coordinates[1],
                newBookingResponse.routeDetail.endPoint.coordinates[0], newBookingResponse.routeDetail.endPoint.coordinates[1])

            val timeInMin = getDistance( newBookingResponse.routeDetail.startPoint.coordinates[0], newBookingResponse.routeDetail.startPoint.coordinates[1],
                newBookingResponse.routeDetail.endPoint.coordinates[0], newBookingResponse.routeDetail.endPoint.coordinates[1])

            tvArriveAt.text = resources.getString(R.string.you_will_arrive)+" $timeformat"
            tvAcceptRideArriveTime.text = "$timeInMin "+getString(R.string.stringmin)+" - "+DecimalFormat("#######.##").format(distance) + getString(R.string.stringkm)
            tvAcceptRideName.text = "" + newBookingResponse.user.fullName

            val profilePic = "" + newBookingResponse.user.profilePic

            if (profilePic.isEmpty())
                ivAcceptRide.setImageResource(R.drawable.group_445)
            else
                Picasso.get().load(WisamTaxiApplication.BASE_URL + "" + profilePic).placeholder(R.drawable.group_445).into(ivAcceptRide)

            clTranspbg.visibility = View.VISIBLE
            clRideReqest.visibility = View.VISIBLE
            switchChooseActive.isClickable = false

            val top = AnimationUtils.loadAnimation(
                this@ChooseActiveActivity,
                R.anim.bottom_to_top_animation
            )
            clRideReqest.animation = top

            top.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }catch (e:Exception){e.printStackTrace()}
    }

    private val onRequestAction = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("onRequestAction", "call: requestAction   " + args[0].toString())
            val requestActionResponse = Gson().fromJson(args[0].toString(), RequestActionResponse::class.java)
            runOnUiThread {
                if (requestActionResponse.sucess && requestActionResponse.message.equals("true", true) && click == 1)
                {
                    if (switchChooseActive.isChecked)
                    {
                        time!!.cancel()
                        hidePopup()

                        val allridesintent = Intent(context, AllRidesActivity::class.java)
                        allridesintent.putExtra("bookingId", requestActionResponse.data._id)
                        allridesintent.putExtra("intentfrom","booking")
                        startActivity(allridesintent)
                        finish()
                    }
                }
                else if (requestActionResponse.sucess && requestActionResponse.message.equals("Booking successfully canceled by customer",true)){
                    showToast(resources.getString(R.string.ridecanceledbycustomer))
                    hidePopup()
                }
                else if (!requestActionResponse.sucess && requestActionResponse.message.equals("User not looking for ride anymore ",true)
                    && click == 1)
                {
                    showToast(resources.getString(R.string.ridecanceledbycustomer))
                    hidePopup()
                }
                else
                {
                    showToast(requestActionResponse.message)
                    hidePopup()
                }
            }
        }
    }

    private fun hidePopup() {

        editor.putString("bookingdata","").apply()
        editor.commit()

        clRideReqest.visibility = View.INVISIBLE
        clTranspbg.visibility = View.INVISIBLE
        switchChooseActive.isClickable = true

        val bottom = AnimationUtils.loadAnimation(this@ChooseActiveActivity, R.anim.top_to_bottom)
        clRideReqest.animation = bottom
    }

    private fun selectActiveRouteDriver(routeId: String) {
        showprogressbar()

        val repository: RetroRepository = RetroRepository.instance
        val drivergetRoutes: MutableLiveData<SelectActiveRouteResponse>

        val request = DriverSelectRoutesRequest(routeId)


        drivergetRoutes = repository.selectActiveRoute(
            sharedPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(),request
        )
        drivergetRoutes.observe(this, object : Observer<SelectActiveRouteResponse> {
            override fun onChanged(t: SelectActiveRouteResponse?) {
                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            if (switchChooseActive.isChecked)
                                startTime(remainingtimemili)

                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()

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
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onBackPressed() {
        if (!switchChooseActive.isChecked)
        {
            if (isFromNoti)
                navigateFinishAffinity(HomeActivity::class.java)
            else
                super.onBackPressed()
        }
    }


    override fun onSocketConnected() {
        jsonObject.put("driverId", sharedPref.getString("id_driver", "").toString())
        sendObjectToSocket(jsonObject, "addDriver")
        emitListeners()
    }

    override fun onSocketDisconnected() {}

    override fun onSocketConnectionError() {}

    override fun onSocketConnectionTimeOut() {}

}
