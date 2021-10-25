package com.wisam.taxi.view.home.fragments


import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getbooking.DriverGetBookingResponse
import com.wisam.taxi.model.response.booking.Booking
import com.wisam.taxi.model.response.booking.UsersBookingResponse
import com.wisam.taxi.view.booking.activity.BookingConfirmActivity
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.allRides.activity.AllRidesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.home.adapter.DriverMyTripRvAdapter
import com.wisam.taxi.view.home.adapter.MyTripRvAdapter
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import com.wisam.taxi.view.tripHistory.TripHistoryActivity
import kotlinx.android.synthetic.main.fragment_mytrip.*

class MyTripFragment : Fragment(), RvClickPostion {
    private var isOngoing: Boolean = true
    private var pageOngoing: String = "1"
    private var pagePast: String = "1"
    private var isLoading: Boolean? = true
    lateinit var adapter: MyTripRvAdapter
    lateinit var adapterdriver: DriverMyTripRvAdapter
    lateinit var myTripsDataList: ArrayList<Booking>
    lateinit var myPastTripsDataList: ArrayList<Booking>
    lateinit var drivermyTripsDataList: ArrayList<com.wisam.taxi.model.driverResponse.getbooking.Booking>
    lateinit var drivermyPastTripsDataList: ArrayList<com.wisam.taxi.model.driverResponse.getbooking.Booking>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mytrip, container, false)
    }

    override fun onResume() {
        super.onResume()

        myTripsDataList.clear()
        drivermyTripsDataList.clear()

        pageOngoing = "1"

        if (WisamTaxiApplication.shrdPref.getString("type", "").equals("driver", true)) {
            getDriverBooking(pageOngoing, "ongoin")
            loadDriveronGoingTrip()
        } else if (WisamTaxiApplication.shrdPref.getString("type", "").equals("customer", true)) {
            getUserBooking(pageOngoing, "ongoin")
            loadonGoingTrip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        myTripsDataList = ArrayList()
        myPastTripsDataList = ArrayList()
        drivermyTripsDataList = ArrayList()
        drivermyPastTripsDataList = ArrayList()

        tvOnGoing.setOnClickListener {
            if (!isOngoing)
            {
                if (WisamTaxiApplication.shrdPref.getString("type", "").equals("driver", true)) {
                    loadDriveronGoingTrip()
                } else if (WisamTaxiApplication.shrdPref.getString("type", "").equals(
                        "customer",
                        true
                    )
                ) {
                    loadonGoingTrip()
                }

                if (myTripsDataList.size <= 0 && drivermyTripsDataList.size <= 0) {
                    if (WisamTaxiApplication.shrdPref.getString("type", "").equals(
                            "driver",
                            true
                        )
                    ) {
                        getDriverBooking(pageOngoing, "ongoin")
                    } else if (WisamTaxiApplication.shrdPref.getString(
                            "type",
                            ""
                        ).equals("customer", true)
                    ) {
                        getUserBooking(pageOngoing, "ongoin")
                    }
                }
            }
        }
        tvPast.setOnClickListener {
            if (isOngoing)
            {
                if (WisamTaxiApplication.shrdPref.getString("type", "").equals("driver", true)) {
                    loadDriverPastTrip()
                } else if (WisamTaxiApplication.shrdPref.getString("type", "").equals(
                        "customer",
                        true
                    )
                ) {
                    loadPastTrip()
                }
                if (myPastTripsDataList.size <= 0 && drivermyPastTripsDataList.size <= 0) {
                    if (WisamTaxiApplication.shrdPref.getString("type", "").equals(
                            "driver",
                            true
                        )
                    ) {
                        getDriverBooking(pagePast, "past")
                    } else if (WisamTaxiApplication.shrdPref.getString(
                            "type",
                            ""
                        ).equals("customer", true)
                    ) {
                        getUserBooking(pagePast, "past")
                    }
                }
            }
        }

    }

    private fun loadonGoingTrip() {
        isOngoing = true
        nobookingtvUI(myTripsDataList)

        tvOnGoing.setTextColor(Color.parseColor("#0b8793"))
        tvPast.setTextColor(Color.parseColor("#c1c1c1"))

        rvMyTripsOngoing.visibility = View.VISIBLE
        rvMyTripsPast.visibility = View.INVISIBLE

        adapter = MyTripRvAdapter(context!!.applicationContext, myTripsDataList, this,"ongoing",
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        rvMyTripsOngoing.adapter = adapter
        rvMyTripsOngoing.layoutManager = LinearLayoutManager(
            context?.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )
        rvMyTripsOngoing.setHasFixedSize(true)
        rvMyTripsOngoing.isNestedScrollingEnabled = false
    }

    private fun loadDriveronGoingTrip() {
        isOngoing = true
        nobookingtvUIDriver(drivermyTripsDataList)

        tvOnGoing.setTextColor(Color.parseColor("#0b8793"))
        tvPast.setTextColor(Color.parseColor("#c1c1c1"))

        rvMyTripsOngoing.visibility = View.VISIBLE
        rvMyTripsPast.visibility = View.INVISIBLE

        adapterdriver = DriverMyTripRvAdapter(context!!.applicationContext, drivermyTripsDataList, this,"ongoing",
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        rvMyTripsOngoing.adapter = adapterdriver
        rvMyTripsOngoing.layoutManager = LinearLayoutManager(
            context?.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )
        rvMyTripsOngoing.setHasFixedSize(true)
        rvMyTripsOngoing.isNestedScrollingEnabled = false
    }

    private fun loadPastTrip() {
        isOngoing = false
        nobookingtvUI(myPastTripsDataList)

        tvPast.setTextColor(Color.parseColor("#0b8793"))
        tvOnGoing.setTextColor(Color.parseColor("#c1c1c1"))
        rvMyTripsPast.visibility = View.VISIBLE
        rvMyTripsOngoing.visibility = View.INVISIBLE

        adapter = MyTripRvAdapter(context!!.applicationContext, myPastTripsDataList, this,"past",
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        rvMyTripsPast.adapter = adapter
        rvMyTripsPast.layoutManager = LinearLayoutManager(
            context?.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )
        rvMyTripsPast.setHasFixedSize(true)
        rvMyTripsPast.isNestedScrollingEnabled = false
    }

    private fun loadDriverPastTrip() {
        isOngoing = false
        nobookingtvUIDriver(drivermyPastTripsDataList)

        tvPast.setTextColor(Color.parseColor("#0b8793"))
        tvOnGoing.setTextColor(Color.parseColor("#c1c1c1"))
        rvMyTripsPast.visibility = View.VISIBLE
        rvMyTripsOngoing.visibility = View.INVISIBLE

        adapterdriver =
            DriverMyTripRvAdapter(context!!.applicationContext, drivermyPastTripsDataList, this,"past",
                (activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        rvMyTripsPast.adapter = adapterdriver
        rvMyTripsPast.layoutManager = LinearLayoutManager(
            context?.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )
        rvMyTripsPast.setHasFixedSize(true)
        rvMyTripsPast.isNestedScrollingEnabled = false
    }

    private fun getUserBooking(page: String, type: String) {
        val map = HashMap<String, String>()
        map["page"] = page
        map["type"] = type

        if (page.equals("1",true))
        {
            loadingPB.visibility = View.GONE
            (activity as HomeActivity).showprogressbar()
        }else{
            loadingPB.visibility = View.VISIBLE
        }

        val repository: RetroRepository = RetroRepository.instance
        var getBookings: MutableLiveData<UsersBookingResponse>

        getBookings = repository.getusersBooking(
            WisamTaxiApplication.shrdPref.getString(
                "auth",
                ""
            ).toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),
            map
        )
        getBookings.observe(viewLifecycleOwner, object : Observer<UsersBookingResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onChanged(t: UsersBookingResponse?) {
                if (!page.equals("1",true))
                {
                    loadingPB.visibility = View.GONE
                }else{
                    (activity as HomeActivity).dismissprogressBar()
                }
                Log.d("ACCOUNT", "" + t?.response?.message)
                try {
                    when {
                        t?.response!!.success -> {
                            if (!type.equals("past", true))
                            {
                                myTripsDataList.addAll(t.data.bookingList)
                                nobookingtvUI(myTripsDataList)

                            } else {
                                myPastTripsDataList.addAll(t.data.bookingList)
                                nobookingtvUI(myPastTripsDataList)
                            }
                            adapter.notifyDataSetChanged()

                            isLoading = false

                            if (!page.equals(""+t.data.count,true))
                            {
                                if (type.equals("past", true))
                                {
                                    rvMyTripsPast!!.addOnScrollListener(object :
                                        RecyclerView.OnScrollListener() {
                                        override fun onScrolled(
                                            recyclerView: RecyclerView,
                                            dx: Int,
                                            dy: Int
                                        ) {
                                            if (dy > 0 && page.toInt() != t.data.count) {

                                                if (isLoading == false)
                                                {
                                                    val layoutManager =
                                                        LinearLayoutManager::class.java.cast(
                                                            recyclerView.layoutManager
                                                        )
                                                    val visibleItemCount = layoutManager!!.childCount
                                                    val totalItemCount = layoutManager.itemCount
                                                    val firstVisibleItemPosition =
                                                        layoutManager.findLastVisibleItemPosition()

                                                    val endHasBeenReached = firstVisibleItemPosition + 1 >= totalItemCount

                                                    if (totalItemCount >= 12 && firstVisibleItemPosition >= 0 && endHasBeenReached) {

                                                        isLoading = true

                                                        pagePast = pagePast.toInt().plus(1).toString()
                                                        getUserBooking(pagePast, "past")
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                                else if (type.equals("ongoin",true))
                                {
                                    rvMyTripsOngoing!!.addOnScrollListener(object :
                                        RecyclerView.OnScrollListener() {
                                        override fun onScrolled(
                                            recyclerView: RecyclerView,
                                            dx: Int,
                                            dy: Int
                                        ) {
                                            if (dy > 0 && page.toInt() != t.data.count) {

                                                if (isLoading == false) {
                                                    val layoutManager =
                                                        LinearLayoutManager::class.java.cast(
                                                            recyclerView.layoutManager
                                                        )
                                                    val visibleItemCount = layoutManager!!.childCount
                                                    val totalItemCount = layoutManager.itemCount
                                                    val firstVisibleItemPosition =
                                                        layoutManager.findLastVisibleItemPosition()

                                                    val endHasBeenReached =
                                                        firstVisibleItemPosition + 1 >= totalItemCount

                                                    if (totalItemCount >= 12 && firstVisibleItemPosition >= 0 && endHasBeenReached) {
                                                        isLoading = true

                                                        pageOngoing =
                                                            pageOngoing.toInt().plus(1).toString()
                                                        getUserBooking(pageOngoing, "ongoin")

                                                    }
                                                }

                                            }
                                        }
                                    })
                                }
                            }
                        }
                        t.response.message.isEmpty() -> {
                            nobooking_tv.visibility = View.VISIBLE
                        }
                        else -> {
                            nobooking_tv.visibility = View.VISIBLE

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(
                                        ChooseUserTypeActivity::class.java
                                    )
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> (activity as HomeActivity).showToast(t.response.message)
                            }

                        }
                    }
                } catch (e: Exception) {
                    nobooking_tv.visibility = View.VISIBLE
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }
            }
        })
    }

    fun nobookingtvUI(datalist: ArrayList<Booking>) {
        if (datalist.size <= 0) {
            nobooking_tv.visibility = View.VISIBLE
        } else {
            nobooking_tv.visibility = View.GONE
        }
    }

    fun nobookingtvUIDriver(datalist: ArrayList<com.wisam.taxi.model.driverResponse.getbooking.Booking>) {
        if (datalist.size <= 0) {
            nobooking_tv.visibility = View.VISIBLE
        } else {
            nobooking_tv.visibility = View.GONE
        }
    }

    private fun getDriverBooking(page: String, type: String) {
        val map = HashMap<String, String>()
        map["page"] = page
        map["type"] = type

        if (page.equals("1",true))
        {
            loadingPB.visibility = View.GONE
            (activity as HomeActivity).showprogressbar()
        }else{
            loadingPB.visibility = View.VISIBLE
        }

        val repository: RetroRepository = RetroRepository.instance
        val getBookings: MutableLiveData<DriverGetBookingResponse>

        getBookings = repository.getDriverBooking(
            WisamTaxiApplication.shrdPref.getString(
                "auth",
                ""
            ).toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),map
        )
        getBookings.observe(viewLifecycleOwner, object : Observer<DriverGetBookingResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onChanged(t: DriverGetBookingResponse?) {

                if (!page.equals("1",true))
                    loadingPB.visibility = View.GONE
                else
                    (activity as HomeActivity).dismissprogressBar()

                try {

                    when {
                        t?.response!!.success -> {

                            if (!type.equals("past", true)) {
                                drivermyTripsDataList.addAll(t.data.bookings)
                                nobookingtvUIDriver(drivermyTripsDataList)
                            } else {
                                drivermyPastTripsDataList.addAll(t.data.bookings)
                                nobookingtvUIDriver(drivermyPastTripsDataList)
                            }
                            adapterdriver.notifyDataSetChanged()

                            isLoading = false

                            if (!page.equals(""+t.data.count,true))
                            {
                                if (type.equals("past", true))
                                {
                                    rvMyTripsPast!!.addOnScrollListener(object :
                                        RecyclerView.OnScrollListener() {
                                        override fun onScrolled(
                                            recyclerView: RecyclerView,
                                            dx: Int,
                                            dy: Int
                                        ) {
                                            if (dy > 0 && page.toInt() != t.data.count) {

                                                if (isLoading == false)
                                                {
                                                    val layoutManager =
                                                        LinearLayoutManager::class.java.cast(
                                                            recyclerView.layoutManager
                                                        )
                                                    val visibleItemCount = layoutManager!!.childCount
                                                    val totalItemCount = layoutManager.itemCount
                                                    val firstVisibleItemPosition =
                                                        layoutManager.findLastVisibleItemPosition()

                                                    val endHasBeenReached = firstVisibleItemPosition + 1 >= totalItemCount

                                                    if (totalItemCount >= 12 && firstVisibleItemPosition >= 0 && endHasBeenReached) {

                                                        isLoading = true

                                                        pagePast = pagePast.toInt().plus(1).toString()
                                                        getDriverBooking(pagePast, "past")
                                                    }
                                                }
                                            }
                                        }
                                    })
                                }
                                else if (type.equals("ongoin",true))
                                {
                                    rvMyTripsOngoing!!.addOnScrollListener(object :
                                        RecyclerView.OnScrollListener() {
                                        override fun onScrolled(
                                            recyclerView: RecyclerView,
                                            dx: Int,
                                            dy: Int
                                        ) {
                                            if (dy > 0 && page.toInt() != t.data.count) {

                                                if (isLoading == false) {
                                                    val layoutManager =
                                                        LinearLayoutManager::class.java.cast(
                                                            recyclerView.layoutManager
                                                        )
                                                    val visibleItemCount = layoutManager!!.childCount
                                                    val totalItemCount = layoutManager.itemCount
                                                    val firstVisibleItemPosition =
                                                        layoutManager.findLastVisibleItemPosition()

                                                    val endHasBeenReached =
                                                        firstVisibleItemPosition + 1 >= totalItemCount

                                                    if (totalItemCount >= 12 && firstVisibleItemPosition >= 0 && endHasBeenReached) {
                                                        Log.d(
                                                            "last_item",
                                                            " ${visibleItemCount + totalItemCount} " + " $totalItemCount " + endHasBeenReached
                                                        )
                                                        isLoading = true

                                                        pageOngoing = pageOngoing.toInt().plus(1).toString()
                                                        getDriverBooking(pageOngoing, "ongoin")

                                                    }
                                                }

                                            }
                                        }
                                    })
                                }
                            }

                        }
                        t.response.message.isNullOrEmpty() -> {
                            nobooking_tv.visibility = View.VISIBLE
                        }
                        else -> {
                            nobooking_tv.visibility = View.VISIBLE
                            if (t.response.message.equals("Authorization not correct", true)) {
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                (activity as HomeActivity).clearnotification()
                                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                (activity as HomeActivity).navigateFinishAffinity(
                                    ChooseUserTypeActivity::class.java
                                )
                            }
                            else if (t.response.logout == 1){
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                (activity as HomeActivity).clearnotification()
                                (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
                                (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else
                                (activity as HomeActivity).showToast(t.response.message)
                        }
                    }
                } catch (e: Exception) {
                    nobooking_tv.visibility = View.VISIBLE
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onItemClicked(user: Int) {

        if (!isOngoing) {
            if (WisamTaxiApplication.shrdPref.getString("type", "").equals("driver", true)) {
                var tripHistoryIntent = Intent(context, TripHistoryActivity::class.java)
                tripHistoryIntent.putExtra("bookingId", drivermyPastTripsDataList[user].id)
                activity!!.startActivity(tripHistoryIntent)
            } else if (WisamTaxiApplication.shrdPref.getString("type", "").equals(
                    "customer",
                    true
                )
            ) {
                var tripHistoryIntent = Intent(context, TripHistoryActivity::class.java)
                tripHistoryIntent.putExtra("bookingId", myPastTripsDataList[user].id)
                activity!!.startActivity(tripHistoryIntent)
            }
        }
        else {
            if (WisamTaxiApplication.shrdPref.getString("type", "").equals("driver", true)) {
                val allridesintent = Intent(context, AllRidesActivity::class.java)
                allridesintent.putExtra("bookingId", drivermyTripsDataList[user].id)
                allridesintent.putExtra("intentfrom","trip")
                activity!!.startActivity(allridesintent)
            } else if (WisamTaxiApplication.shrdPref.getString("type", "").equals("customer", true))
            {
                val bookingDetailsintent = Intent(context, BookingConfirmActivity::class.java)
                bookingDetailsintent.putExtra("bookingId", myTripsDataList[user].id)
                bookingDetailsintent.putExtra("intentfrom", "trip")
                activity!!.startActivity(bookingDetailsintent)
            }
        }
    }

}
