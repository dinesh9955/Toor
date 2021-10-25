package com.wisam.taxi.view.home.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.LocaleManager
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.interfaces.SocketListener
import com.wisam.taxi.model.driverRequest.DriverGetRoutesRequest
import com.wisam.taxi.model.driverRequest.IsAvailableRequest
import com.wisam.taxi.model.driverResponse.getRoutes.DriverGetRoutesResponse
import com.wisam.taxi.model.driverResponse.getbooking.DriverGetBookingResponse
import com.wisam.taxi.model.driverResponse.profile.DriverProfileResponse
import com.wisam.taxi.model.request.UserhomeRequest
import com.wisam.taxi.model.response.booking.UsersBookingResponse
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.model.response.userhome.Route
import com.wisam.taxi.model.response.userhome.UserHomeResponse
import com.wisam.taxi.socket.AppSocketListener
import com.wisam.taxi.socket.responses.sendBooking.SendBookingResponse
import com.wisam.taxi.view.booking.activity.BookingConfirmActivity
import com.wisam.taxi.view.booking.activity.BookingProcessingActivity
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.allRides.activity.AllRidesActivity
import com.wisam.taxi.view.driverModule.chooseActive.ChooseActiveActivity
import com.wisam.taxi.view.driverModule.routes.RoutesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.home.adapter.DriverExploreRvAdapter
import com.wisam.taxi.view.home.adapter.ExploreRvAdapter
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_explore.*
import org.json.JSONArray
import org.json.JSONObject

class ExploreFragment : Fragment(), SocketListener, RvClickPostion{

    lateinit var adapter: ExploreRvAdapter
    lateinit var adapterdriver: DriverExploreRvAdapter
    lateinit var exploreList: ArrayList<Route>
    lateinit var driverexploreList: ArrayList<com.wisam.taxi.model.driverResponse.getRoutes.Route>
    private lateinit var jsonObject: JSONObject
    var position: Int = 0
    private var isLoading: Boolean? = true
    private var pageExplore: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        LocaleManager.setNewLocale(context!!, LocaleManager.getCurrentLanguage(context)!!)
        exploreList = ArrayList()
        driverexploreList = ArrayList()

        jsonObject = JSONObject()

        AppSocketListener.getInstance().restartSocket()
        AppSocketListener.getInstance().setActiveSocketListener(this)

        swp_rvExplore.setColorSchemeColors(ContextCompat.getColor(context!!,R.color.colorPrimaryDark))

        if ((activity as HomeActivity).sharedPref.getString("type", "").equals("driver", true))
        {

            if (switchExplore.isChecked)
            {
                tvSwitchChooseOff.visibility = View.INVISIBLE
                calldriverIsAvailable(1, "first")
            }
            else
            {
                tvSwitchChooseOff.visibility = View.INVISIBLE
                calldriverIsAvailable(0, "first")
            }

            switchExplore.visibility = View.VISIBLE
            tvswitchExploreActive.visibility = View.VISIBLE

            getDriverBooking()

        }
        else
        {
            switchExplore.visibility = View.GONE
            tvSwitchChooseOff.visibility = View.GONE
            tvswitchExploreActive.visibility = View.GONE

            if (!(activity as HomeActivity).sharedPref.getString("requestId", "").isNullOrEmpty())
            {
                val mills: Long = System.currentTimeMillis() - (activity as HomeActivity).sharedPref.getString("remainingTime", "")!!.toLong()
                val hours: Int = (mills / (1000 * 60 * 60)).toInt()
                val mins = (mills / (1000 * 60) % 60).toInt()

                if (hours <= 0 && mins < 5)
                {
                    getUserBooking()

                    Handler().postDelayed({

                        (activity as HomeActivity).dismissprogressBar()
                        val bookingprocIntent = Intent(context, BookingProcessingActivity::class.java)
                        bookingprocIntent.putExtra("driverId", "")
                        bookingprocIntent.putExtra("requestId",(activity as HomeActivity).sharedPref.getString("requestId", ""))
                        activity!!.startActivity(bookingprocIntent)

                    }, 1500)

                }
                else
                {

                    (activity as HomeActivity).editor.putString("requestId","")
                    (activity as HomeActivity).editor.putString("remainingTime","")
                    (activity as HomeActivity).editor.apply()
                    (activity as HomeActivity).editor.commit()
                    getUserBooking()

                }
            }
            else
                getUserBooking()
        }

        switchExplore.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {

            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                if (isChecked)
                    calldriverIsAvailable(1, "second")
                else if (!isChecked)
                    calldriverIsAvailable(0, "second")

            }

        })

        swp_rvExplore.setOnRefreshListener {

            if ((activity as HomeActivity).sharedPref.getString("type", "").equals("driver", true))
            {
                pageExplore = 1
                drivergetRoutes("second", pageExplore)
                loadDriverExplore()
            }
            else
            {
                pageExplore = 1
                callcustomerHome("second", pageExplore)
                loadUserExplore()
            }

        }
    }

    private fun drivergetRoutes(type: String, page: Int) {
        if (type.equals("first", true))
            (activity as HomeActivity).showprogressbar()

        val repository: RetroRepository = RetroRepository.instance
        val drivergetRoutes: MutableLiveData<DriverGetRoutesResponse>

        val driverGetRoutesRequest = DriverGetRoutesRequest(page)

        drivergetRoutes = repository.drivergetroutes(
            (activity as HomeActivity).sharedPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),
            driverGetRoutesRequest
        )

        drivergetRoutes.observe(viewLifecycleOwner, object : Observer<DriverGetRoutesResponse> {
            override fun onChanged(t: DriverGetRoutesResponse?) {
                if (type.equals("first", true))
                    (activity as HomeActivity).dismissprogressBar()
                else
                    swp_rvExplore.isRefreshing = false

                try {

                    when {
                        t?.response!!.success -> {

                            if (type.equals("second", true))
                                driverexploreList.clear()

                            driverexploreList.addAll(t.data.routes)
                            adapterdriver.notifyDataSetChanged()

                            if (driverexploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE
                            else
                                nodata_tv.visibility = View.GONE

                            isLoading = false
                            callgetProfile()

                            rvExplore!!.addOnScrollListener(object :
                                RecyclerView.OnScrollListener() {
                                override fun onScrolled(
                                    recyclerView: RecyclerView,
                                    dx: Int,
                                    dy: Int
                                ) {
                                    if (dy > 0 && driverexploreList.size < t.data.count) {

                                        if (isLoading == false) {
                                            val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                                            val visibleItemCount = layoutManager!!.childCount
                                            val totalItemCount = layoutManager.itemCount
                                            val firstVisibleItemPosition =
                                                layoutManager.findLastVisibleItemPosition()

                                            val endHasBeenReached =
                                                firstVisibleItemPosition + 1 >= totalItemCount

                                            if (totalItemCount >= 10 && firstVisibleItemPosition >= 0 && endHasBeenReached) {
                                                swp_rvExplore.isRefreshing = true
                                                pageExplore = pageExplore.plus(1)
                                                drivergetRoutes("third", pageExplore)
                                                isLoading = true
                                            }
                                        }
                                    }
                                }
                            })
                        }
                        t.response.message.isEmpty() -> {
                            if (driverexploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE

                        }
                        else -> {

                            if (driverexploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
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

                    if (driverexploreList.size <= 0)
                        nodata_tv.visibility = View.VISIBLE

                    e.printStackTrace()
                }

            }
        })
    }

    private fun calldriverIsAvailable(value: Int, calltime: String) {
        if (calltime.isNotEmpty() && !calltime.equals("first", true)) {
            (activity as HomeActivity).showprogressbar()
        }
        val repository: RetroRepository = RetroRepository.instance
        val isAvailable: MutableLiveData<LogoutResponse>
        val isAvailableRequest = IsAvailableRequest(value)

        isAvailable = repository.isAvailableDriver(
            (activity as HomeActivity).sharedPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(), isAvailableRequest)

        isAvailable.observe(viewLifecycleOwner, object : Observer<LogoutResponse> {
            override fun onChanged(t: LogoutResponse?) {

                try {

                    when {
                        t?.response!!.success -> {
                            if (calltime.isNotEmpty() && !calltime.equals("first", true)) {
                                (activity as HomeActivity).dismissprogressBar()
                            }

                            if (value == 0) {
                                (activity as HomeActivity).navigateFinishAffinity(RoutesActivity::class.java)
                            }
                        }
                        t.response.message.isEmpty() -> {
                            if (calltime.isNotEmpty() && !calltime.equals("first", true)) {
                                (activity as HomeActivity).dismissprogressBar()
                            }
                        }
                        else -> {
                            if (calltime.isNotEmpty() && !calltime.equals("first", true)) {
                                (activity as HomeActivity).dismissprogressBar()
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (calltime.isNotEmpty() && !calltime.equals("first", true)) {
                        (activity as HomeActivity).dismissprogressBar()
                    }
                    e.printStackTrace()
                }
            }
        })
    }

    private fun callcustomerHome(type: String, page: Int) {

        if (type.equals("first", true))
            (activity as HomeActivity).showprogressbar()

        val repository: RetroRepository = RetroRepository.instance
        val userHome: MutableLiveData<UserHomeResponse>
        val userhomeRequest = UserhomeRequest(
            (activity as HomeActivity).sharedPref.getString("lat", "20.265")!!.toDouble(),
            (activity as HomeActivity).sharedPref.getString("long", "15.233")!!.toDouble(), page
        )

        userHome = repository.userHome(
            (activity as HomeActivity).sharedPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(), userhomeRequest)

        userHome.observe(viewLifecycleOwner, object : Observer<UserHomeResponse> {
            override fun onChanged(t: UserHomeResponse?) {

                if (type.equals("first", true))
                    (activity as HomeActivity).dismissprogressBar()
                else
                    swp_rvExplore.isRefreshing = false

                try {
                    when {
                        t?.response!!.success -> {

                            if (type.equals("second", true))
                                exploreList.clear()

                            exploreList.addAll(t.data.routes)

                            adapter.notifyDataSetChanged()
                            if (exploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE
                            else
                                nodata_tv.visibility = View.GONE

                            isLoading = false

                            rvExplore!!.addOnScrollListener(object :
                                RecyclerView.OnScrollListener() {
                                override fun onScrolled(
                                    recyclerView: RecyclerView,
                                    dx: Int,
                                    dy: Int
                                ) {
                                    if (dy > 0 && exploreList.size < t.data.count) {

                                        if (isLoading == false) {
                                            val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                                            val visibleItemCount = layoutManager!!.childCount
                                            val totalItemCount = layoutManager.itemCount
                                            val firstVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                                            val endHasBeenReached = firstVisibleItemPosition + 1 >= totalItemCount

                                            if (totalItemCount >= 10 && firstVisibleItemPosition >= 0 && endHasBeenReached) {
                                                swp_rvExplore.isRefreshing = true
                                                pageExplore = pageExplore.plus(1)
                                                callcustomerHome("third", pageExplore)
                                                isLoading = true
                                            }
                                        }
                                    }
                                }
                            })

                        }
                        t.response.message.isEmpty() -> {
                            if (exploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE

                        }
                        else ->
                        {
                            if (exploreList.size <= 0)
                                nodata_tv.visibility = View.VISIBLE

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> (activity as HomeActivity).showToast(t.response.message)
                            }
                        }
                    }
                }
                catch (e: Exception) {
                    if (exploreList.size <= 0)
                        nodata_tv.visibility = View.VISIBLE

                    e.printStackTrace()
                }

            }
        })
    }

    private fun emitListeners() {
        try {
            if (AppSocketListener.getInstance().isSocketConnected) {
                AppSocketListener.getInstance().off("sendBooking", onsendBooking)
                AppSocketListener.getInstance().addOnHandler("sendBooking", onsendBooking)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private val onsendBooking = object : Emitter.Listener {
        override fun call(vararg args: Any) {

            Log.i("onsendBooking", "call: sendBooking   " + args[0].toString())

            val sendBookingResponse = Gson().fromJson(args[0].toString(), SendBookingResponse::class.java)

            activity!!.runOnUiThread {
                if (sendBookingResponse.sucess)
                {
                    Handler().postDelayed({

                        val bookingprocIntent = Intent(context, BookingProcessingActivity::class.java)
                        bookingprocIntent.putExtra("driverId", exploreList[position].driver[0]._id)
                        bookingprocIntent.putExtra("requestId", sendBookingResponse.requestId)
                        activity!!.startActivity(bookingprocIntent)
                        activity!!.finishAffinity()

                    }, 150)
                }
                else
                {
                    swp_rvExplore.isRefreshing = true
                    onSwipeToRefresh()
                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.nodriverfoundnearyou))
                }
            }
        }
    }

    private fun callgetProfile() {
        val repository: RetroRepository = RetroRepository.instance
        val resetPassword: MutableLiveData<DriverProfileResponse>

        resetPassword = repository.getDriverprofile(
            WisamTaxiApplication.shrdPref.getString("auth",
                ""
            ).toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString()
        )
        resetPassword.observe(viewLifecycleOwner, object : Observer<DriverProfileResponse> {
            override fun onChanged(t: DriverProfileResponse?) {
                try {
                    when {
                        t?.response!!.success ->
                        {
                            if (t.data.route.isNotEmpty()) {
                                var remainingtime = System.currentTimeMillis() - t.data.activateTime
                                remainingtime = 900000 - remainingtime

                                if (remainingtime > 0) {

                                    val intent = Intent(context, ChooseActiveActivity::class.java)
                                    intent.putExtra("route", "" + t.data.route)
                                    intent.putExtra("remainingtime", "" + remainingtime)
                                    intent.putExtra("bookingData", (activity as HomeActivity).bookingData)
                                    activity!!.startActivity(intent)

                                    if ((activity as HomeActivity).bookingData.isNotEmpty())
                                        activity!!.finishAffinity()

                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    private fun loadUserExplore() {
        adapter = ExploreRvAdapter(context!!, exploreList, this)
        rvExplore.adapter = adapter
        rvExplore.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun loadDriverExplore() {
        adapterdriver = DriverExploreRvAdapter(context!!, driverexploreList, this)
        rvExplore.adapter = adapterdriver
        rvExplore.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onSocketConnected() {
        emitListeners()
    }

    override fun onSocketDisconnected() {
    }

    override fun onSocketConnectionError() {
    }

    override fun onSocketConnectionTimeOut() {
    }

    override fun onItemClicked(user: Int) {
        position = user

        if ((activity as HomeActivity).sharedPref.getString("type", "").equals("driver", true)) {
            val intent = Intent(context, ChooseActiveActivity::class.java)
            intent.putExtra("route", driverexploreList[user]._id)
            intent.putExtra("remainingtime", "")
            activity!!.startActivity(intent)
        }
        else
        {
            try {
                if (exploreList[user].driver.size > 0)
                {
                    val driverIDObj = JSONObject()
                    val driverIDArrayObj = JSONArray()

                    for (i in exploreList[user].driver.indices)
                    {
                        driverIDObj.put("driverId",""+exploreList[user].driver[i]._id)
                        driverIDArrayObj.put(driverIDObj.get("driverId"))
                    }
                    jsonObject.put("drivers", driverIDArrayObj)
//                    jsonObject.put("driver", exploreList[user].driver[0]._id)
                    jsonObject.put("driver", "")
                    jsonObject.put("route", exploreList[user]._id)
                    jsonObject.put("userId", (activity as HomeActivity).sharedPref.getString("id", "").toString())
//                    jsonObject.put("seats", exploreList[user].driver[0].vehicletypes.seats)
                    jsonObject.put("seats", 0)
                    jsonObject.put("note", "")
                    jsonObject.put("couponCode", "")
                    jsonObject.put("couponDiscount", 0)
                    jsonObject.put("totalDiscount", 0)
                    jsonObject.put("subTotalAmount", 0)
                    jsonObject.put("totalAmount", exploreList[user].totalFare)
                    jsonObject.put("tax", 0)

                    (activity as HomeActivity).sendObjectToSocket(jsonObject, "sendBooking")

                    emitListeners()

                }
                else
                {
                    swp_rvExplore.isRefreshing = true
                    onSwipeToRefresh()
                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.noActivedriver))
                }

            }
            catch (e: Exception){
                e.printStackTrace()
                (activity as HomeActivity).showToast(context!!.getString(R.string.unknownerroroccured))
            }
        }
    }

    private fun onSwipeToRefresh(){
        if ((activity as HomeActivity).sharedPref.getString("type", "").equals("driver", true))
        {
            pageExplore = 1
            drivergetRoutes("second", pageExplore)
            loadDriverExplore()
        }
        else
        {
            pageExplore = 1
            callcustomerHome("second", pageExplore)
            loadUserExplore()
        }
    }

    private fun getUserBooking() {

        (activity as HomeActivity).showprogressbar()
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["type"] = "ongoin"

        val repository: RetroRepository = RetroRepository.instance
        val getBookings: MutableLiveData<UsersBookingResponse>

        getBookings = repository.getusersBooking(WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(), map)

        getBookings.observe(viewLifecycleOwner, object : Observer<UsersBookingResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onChanged(t: UsersBookingResponse?) {
                (activity as HomeActivity).dismissprogressBar()

                try {
                    when {
                        t?.response!!.success -> {
                            if (t.data.bookingList.size > 0)
                            {
                                val intent = Intent(context!!, BookingConfirmActivity::class.java)
                                intent.putExtra("bookingId",t.data.bookingList[0]._id)
                                intent.putExtra("intentfrom","booking")
                                activity!!.startActivity(intent)
                                activity!!.finish()
                            }
                            else
                            {
                                callcustomerHome("first", pageExplore)
                                loadUserExplore()
                            }
                        }
                        t.response.message.isEmpty() -> { }
                        else -> {

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
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
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }
            }
        })
    }

    private fun getDriverBooking() {

        (activity as HomeActivity).showprogressbar()

        val map = HashMap<String, String>()
        map["page"] = "1"
        map["type"] = "ongoin"

        val repository: RetroRepository = RetroRepository.instance
        val getBookings: MutableLiveData<DriverGetBookingResponse>

        getBookings = repository.getDriverBooking(WisamTaxiApplication.shrdPref.getString("auth", "").toString(), (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),map)

        getBookings.observe(viewLifecycleOwner, object : Observer<DriverGetBookingResponse> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onChanged(t: DriverGetBookingResponse?) {
                (activity as HomeActivity).dismissprogressBar()
                try {

                    when {
                        t?.response!!.success -> {

                           if (t.data.bookings.size <= 0)
                           {
                               drivergetRoutes("first", pageExplore)
                               loadDriverExplore()
                           }
                            else
                           {
                               val allridesintent = Intent(context, AllRidesActivity::class.java)
                               allridesintent.putExtra("bookingId", t.data.bookings[0].id)
                               allridesintent.putExtra("intentfrom","booking")
                               activity!!.startActivity(allridesintent)
                           }

                        }
                        t.response.message.isEmpty() -> { }
                        else -> {
                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
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
                    e.printStackTrace()
                }
            }
        })
    }

}
