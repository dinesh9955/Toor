package com.wisam.taxi.view.tripHistory

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getbookingDetails.GetBookingDetailsDriver
import com.wisam.taxi.model.response.getBookingDetailsUser.UserBookingDetailResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_triphistory.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TripHistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triphistory)

        if (sharedPref.getString("type", "").equals("driver", true)) {
            if (!intent.getStringExtra("bookingId").isNullOrEmpty())
                bookingDetailsDriver(intent.getStringExtra("bookingId")!!)
        }
        else if (sharedPref.getString("type", "").equals("customer", true))
        {
            if (!intent.getStringExtra("bookingId").isNullOrEmpty())
                bookingDetails(intent.getStringExtra("bookingId")!!)
        }


        ivTripHistoryBack.setOnClickListener {
            finish()
        }
    }
    private fun bookingDetails(data: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance

        var getData: MutableLiveData<UserBookingDetailResponse>
        getData = repository.usergetBookingDetails(sharedPref.getString("auth","").toString(),
            sharedPref.getString("mylang", "en").toString(),data)
        getData.observe(this, object : Observer<UserBookingDetailResponse> {
            override fun onChanged(t: UserBookingDetailResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            clNoData.visibility = View.INVISIBLE
                            svMain.visibility = View.VISIBLE
                            try {
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                                var bookingstartTime = sdf.format(t.data.bookingStartTime)
                                var bookingendTime = sdf.format(t.data.bookingEndTime)

//                                var bookingstartTime = sdf.format(System.currentTimeMillis())
//                                var bookingendTime = sdf.format(System.currentTimeMillis() + 1000 * 60 * 30)

                                var startDate = sdf.parse(bookingstartTime)
                                var enddate = sdf.parse(bookingendTime)

                                sdf = SimpleDateFormat("EEEE dd MMM, yyyy")
                                val date = sdf.format(startDate)

                                sdf = SimpleDateFormat("h:mm a")
                                val startTime = sdf.format(startDate)

                                var diffInMillies = enddate.time - startDate.time

                                val numOfDays = (diffInMillies / (1000 * 60 * 60 * 24))
                                val hours = (diffInMillies / (1000 * 60 * 60))
                                val minutes = (diffInMillies / (1000 * 60))
                                val seconds = (diffInMillies / 1000)

                                Log.d("TimeDifference", "$numOfDays $hours $minutes $seconds")

//                                var timeDifference = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(diffInMillies) -
//                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffInMillies)))


                                val timeInMin = getDistance( t.data.source.latitude, t.data.source.longitude, t.data.destination.latitude
                                    , t.data.destination.longitude)

                                tvSource.text = t.data.source.address
                                tvDestination.text = t.data.destination.address
                                tvTripHistorySource.text = t.data.source.address
                                tvTripHistoryDestination.text = t.data.destination.address
                                tvDriverfullname.text = t.data.driverId.fullName
                                tvTripHistoryRating.text = ""+ DecimalFormat("#.#").format(t.data.ratings)
                                tvTripHistoryPas.text = ""+t.data.seats+" "+resources.getString(R.string.passengers)
                                tvTripHistoryEstPayVal.text = ""+t.data.totalAmount
                                tvTripHistoryValue.text = ""+t.data.totalAmount
                                tvTripHistoryTotalFairVal.text = ""+t.data.totalAmount
//                                tvTripHistoryDistanceVal.text = ""+t.data.distance+" KM"
                                tvTripHistoryDurationVal.text = "$timeInMin "+resources.getString(R.string.stringmin)
                                tvTripHistoryTimeVa.text = "$timeInMin "+resources.getString(R.string.stringmin)
                                tvTripHistoryTime.text = "$date, $startTime"

                                val profile =""+t.data.driverId.profilePic

                                if (profile.isEmpty())
                                {
                                    ivTripHistoryProfile.setImageResource(R.drawable.group_445)
                                }else
                                {
                                    Picasso.get()
                                        .load(WisamTaxiApplication.BASE_URL+""+profile)
                                        .placeholder(R.drawable.group_445)
                                        .into(ivTripHistoryProfile)
                                }

                                val distance = haversine( t.data.source.latitude, t.data.source.longitude, t.data.destination.latitude
                                    , t.data.destination.longitude)

                                Log.d("Distance","Distance :- $distance")

                                tvTripHistoryDistanceVal.text = ""+DecimalFormat("#######.##").format(t.data.distance)+""+resources.getString(R.string.stringkm)

                                when (t.data.status) {
                                    2 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.start)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    3 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.completed)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    6 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.completed)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    11 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.cancel)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#ff3030"))
                                    }
                                    12 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.cancel)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#ff3030"))
                                    }
                                }

                            }
                            catch (e:Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        t.response.message.isNullOrEmpty() -> {
                            Log.d("ACCOUNT", t.response.message)
                            dismissprogressBar()

                            clNoData.visibility = View.VISIBLE
                            svMain.visibility = View.INVISIBLE
                        }
                        else -> {

                            clNoData.visibility = View.VISIBLE
                            svMain.visibility = View.INVISIBLE

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
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    clNoData.visibility = View.VISIBLE
                    svMain.visibility = View.INVISIBLE
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }
            }
        })
    }

    private fun bookingDetailsDriver(data: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance

        var getData: MutableLiveData<GetBookingDetailsDriver>
        getData = repository.getBookingDetailsDriver(sharedPref.getString("auth","").toString(),
            sharedPref.getString("mylang", "en").toString(),data)
        getData.observe(this, object : Observer<GetBookingDetailsDriver> {
            override fun onChanged(t: GetBookingDetailsDriver?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            clNoData.visibility = View.INVISIBLE
                            svMain.visibility = View.VISIBLE

                            try {
                                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                                var bookingstartTime = sdf.format(t.data.bookingStartTime)
                                var bookingendTime = sdf.format(t.data.bookingEndTime)

//                                var bookingstartTime = sdf.format(System.currentTimeMillis())
//                                var bookingendTime = sdf.format(System.currentTimeMillis() + 1000 * 60 * 30)

                                var startDate = sdf.parse(bookingstartTime)
                                var enddate = sdf.parse(bookingendTime)

                                sdf = SimpleDateFormat("EEEE dd MMM, yyyy")
                                val date = sdf.format(startDate)

                                sdf = SimpleDateFormat("h:mm a")
                                val startTime = sdf.format(startDate)

                                var diffInMillies = enddate.time - startDate.time

                                val numOfDays = (diffInMillies / (1000 * 60 * 60 * 24))
                                val hours = (diffInMillies / (1000 * 60 * 60))
                                val minutes = (diffInMillies / (1000 * 60))
                                val seconds = (diffInMillies / 1000)

                                Log.d("TimeDifference", "$numOfDays $hours $minutes $seconds")

//                                var timeDifference = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(diffInMillies) -
//                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffInMillies)))
                                val timeInMin =  getDistance( t.data.source.latitude, t.data.source.longitude, t.data.destination.latitude
                                    , t.data.destination.longitude)

                                tvSource.text = t.data.source.address
                                tvDestination.text = t.data.destination.address
                                tvTripHistorySource.text = t.data.source.address
                                tvTripHistoryDestination.text = t.data.destination.address
                                tvDriverfullname.text = t.data.userId.fullName
                                tvTripHistoryRating.text = ""+ DecimalFormat("#.#").format(t.data.userRating)
                                tvTripHistoryPas.text = ""+t.data.seats+" "+resources.getString(R.string.passengers)
                                tvTripHistoryEstPayVal.text = ""+t.data.totalAmount
                                tvTripHistoryValue.text = ""+t.data.totalAmount
                                tvTripHistoryTotalFairVal.text = ""+t.data.totalAmount
                                tvTripHistoryDurationVal.text = "$timeInMin "+resources.getString(R.string.stringmin)
                                tvTripHistoryTimeVa.text = "$timeInMin "+resources.getString(R.string.stringmin)
                                tvTripHistoryTime.text = "$date, $startTime"


                                val profile =""+t.data.userId.profilePic

                                if (profile.isNullOrEmpty())
                                {
                                    ivTripHistoryProfile.setImageResource(R.drawable.group_445)
                                }else
                                {
                                    Picasso.get()
                                        .load(WisamTaxiApplication.BASE_URL+""+profile)
                                        .placeholder(R.drawable.group_445)
                                        .into(ivTripHistoryProfile)
                                }

                                val distance = haversine( t.data.source.latitude, t.data.source.longitude, t.data.destination.latitude
                                    , t.data.destination.longitude)

                                Log.d("Distance","Distance :- $distance , $timeInMin ")

                                tvTripHistoryDistanceVal.text = ""+DecimalFormat("#######.##").format(t.data.distance)+ " "+resources.getString(R.string.stringkm)

                                when (t.data.status) {
                                    2 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.start)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    3 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.completed)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    6 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.completed)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#009a24"))
                                    }
                                    11 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.cancel)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#ff3030"))
                                    }
                                    12 -> {
                                        tvTripHistoryStatusVal.text = resources.getString(R.string.cancel)
                                        tvTripHistoryStatusVal.setTextColor(Color.parseColor("#ff3030"))
                                    }
                                }

                            }
                            catch (e:Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        t.response.message.isNullOrEmpty() -> {
                            Log.d("ACCOUNT", t.response.message)
                            dismissprogressBar()
                            clNoData.visibility = View.VISIBLE
                            svMain.visibility = View.INVISIBLE
                        }
                        else -> {

                            clNoData.visibility = View.VISIBLE
                            svMain.visibility = View.INVISIBLE

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
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()

                    clNoData.visibility = View.VISIBLE
                    svMain.visibility = View.INVISIBLE

                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }
            }
        })
    }
}
