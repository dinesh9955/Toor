package com.wisam.taxi.view.driverModule.routes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getDriverRoutes.Data
import com.wisam.taxi.model.driverResponse.getDriverRoutes.DriverRouteListResponse
import com.wisam.taxi.model.driverResponse.routeslist.RoutesData
import com.wisam.taxi.model.driverResponse.routeslist.RoutesListResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.driverModule.chooseActive.ChooseActiveActivity
import com.wisam.taxi.view.driverModule.routes.adapter.RouterRvAdapter
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_routes.*
import java.lang.Exception

class RoutesActivity : BaseActivity() {

    lateinit var adapter : RouterRvAdapter
    lateinit var routesList : ArrayList<RoutesData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)

        routesList = ArrayList()
        getroutesList()

        if(switchRoutes.isChecked)
        {
            tvSwitchRoutesOff.visibility =View.INVISIBLE
            tvActive.visibility = View.VISIBLE
            tvRoutesNext.setBackgroundResource(R.drawable.round_corner_bg)
        }else
        {
            tvSwitchRoutesOff.visibility =View.VISIBLE
            tvActive.visibility = View.INVISIBLE
            tvRoutesNext.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
        }


        adapter = RouterRvAdapter(this.applicationContext, routesList)
        rvRoutes.adapter = adapter
        rvRoutes.layoutManager = LinearLayoutManager(
            this.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )

        tvRoutesNext.setOnClickListener {
            try {
                if(switchRoutes.isChecked) {
                    navigateFinishAffinity(HomeActivity::class.java)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        switchRoutes.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    tvSwitchRoutesOff.visibility = View.INVISIBLE
                    tvActive.visibility = View.VISIBLE
                    tvRoutesNext.setBackgroundResource(R.drawable.round_corner_bg)
                }
                else
                {
                    tvSwitchRoutesOff.visibility = View.VISIBLE
                    tvActive.visibility = View.INVISIBLE
                    tvRoutesNext.setBackgroundResource(R.drawable.roundcorner_disabled_bg)
                }
            }

        })
    }

    private fun getroutesList()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        var logoutdriver : MutableLiveData<RoutesListResponse>

        logoutdriver = repository.getDriverroutelist(sharedPref.getString("mylang", "en").toString())
        logoutdriver.observe(this, object : Observer<RoutesListResponse> {
            override fun onChanged(t: RoutesListResponse?)
            {
               dismissprogressBar()
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {
                    when {
                        t?.response!!.success -> {
                            routesList.addAll(t.data.list)
                            adapter.notifyDataSetChanged()
                        }
                        t.response.message.isEmpty() -> { }
                        else -> {
                            if (t.response.message.equals("Authorization not correct",true))
                            {
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
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                    Log.d("APIRESPONSE",""+e.toString())
                }

            }
        })
    }
}
