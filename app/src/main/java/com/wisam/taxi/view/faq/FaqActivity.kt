package com.wisam.taxi.view.faq

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getbooking.DriverGetBookingResponse
import com.wisam.taxi.model.response.logout.FAQData
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.view.aboutus.FaqListAdapter
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.allRides.activity.AllRidesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.home.adapter.SettingsRvAdapter
import kotlinx.android.synthetic.main.activity_faq.*
import kotlinx.android.synthetic.main.fragment_setting.*

class FaqActivity : BaseActivity() {

    val faqList : ArrayList<FAQData> = ArrayList()
    lateinit var adapter : FaqListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        setUpFaqRv()
        getFaqList()

        ivFaqBack.setOnClickListener {
            finish()
        }
    }

    private fun setUpFaqRv() {
        adapter = FaqListAdapter(this,faqList)
        rvFaq.adapter = adapter
        rvFaq.setHasFixedSize(true)
        rvFaq.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }


    private fun getFaqList() {
        showprogressbar()
        val map = HashMap<String, String>()
        map["page"] = "FAQ"
        val repository: RetroRepository = RetroRepository.instance
        val getBookings: MutableLiveData<JsonElement>
        getBookings = repository.getFaqList(sharedPref.getString("auth", "").toString(), sharedPref.getString("mylang", "en").toString(), map)
        getBookings.observe(this, object : Observer<JsonElement> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onChanged(apiResponse: JsonElement?) {
                dismissprogressBar()
                try {
                    val t = Gson().fromJson(apiResponse , LogoutResponse::class.java)
                    when {
                        t?.response!!.success -> {
                            faqList.clear()
                            faqList.addAll(t.data.FAQ)
                            adapter.notifyDataSetChanged()
                        }
                        t.response.message.isEmpty() -> {
                            Log.d("ACCOUNT", t.response.message)
                        }
                        else -> {
                            if (t.response.message.equals("Authorization not correct", true)) {
                                editor.putString("auth", "").apply()
                                clearnotification()
                                showToast(getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else if (t.response.logout == 1) {
                                editor.putString("auth", "").apply()
                                clearnotification()
                                showToast(getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }
            }
        })
    }
}