package com.wisam.taxi.view.home.fragments


import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squareup.picasso.Picasso

import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverRequest.DriverSelectRoutesRequest
import com.wisam.taxi.model.driverResponse.selectActiveRoute.SelectActiveRouteResponse
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.services.TimerService
import com.wisam.taxi.services.restApi.MyForgroundTimer
import com.wisam.taxi.view.aboutus.AboutUsActivity
import com.wisam.taxi.view.changePassword.ChangePasswordActivity
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.documents.DocumentsActivity
import com.wisam.taxi.view.faq.FaqActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.home.adapter.SettingsRvAdapter
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import com.wisam.taxi.view.home.model.SettingsDataModel
import com.wisam.taxi.view.languageSelect.LanguageSelectionActivity
import com.wisam.taxi.view.welcome.activity.WelcomeLoginActivity
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment(), RvClickPostion {

    lateinit var adapter: SettingsRvAdapter
    private lateinit var settingsDataList: ArrayList<SettingsDataModel>
    private lateinit var exploreModel: SettingsDataModel
    private lateinit var logoutCancelTv: TextView
    private lateinit var logoutTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        settingsDataList = ArrayList()

        prepareData()

        adapter = SettingsRvAdapter(context!!.applicationContext, settingsDataList, this)
        rvSettings.adapter = adapter
        rvSettings.layoutManager = LinearLayoutManager(context?.applicationContext, LinearLayoutManager.VERTICAL, false)
    }

    private fun prepareData() {
        exploreModel = SettingsDataModel(getString(R.string.changepassword))
        settingsDataList.add(exploreModel)

        if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
            exploreModel = SettingsDataModel(getString(R.string.documents))
            settingsDataList.add(exploreModel)
        }

        exploreModel = SettingsDataModel(getString(R.string.language))
        settingsDataList.add(exploreModel)

        exploreModel = SettingsDataModel(getString(R.string.about))
        settingsDataList.add(exploreModel)

        exploreModel = SettingsDataModel(getString(R.string.fAQ))
        settingsDataList.add(exploreModel)

        exploreModel = SettingsDataModel(getString(R.string.logout))
        settingsDataList.add(exploreModel)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                10 -> activity?.recreate()
            }
        }
    }

    override fun onItemClicked(position: Int) {

        if (position == 0) {
            startActivity(Intent(context, ChangePasswordActivity::class.java))
        } else if (position == 1) {
            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
                startActivity(Intent(context, DocumentsActivity::class.java))
            } else {
                val langintent = Intent(context, LanguageSelectionActivity::class.java)
                langintent.putExtra("intentfrom", "setting")
                startActivityForResult(langintent, 10)
            }
        } else if (position == 2) {
            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
                val langintent = Intent(context, LanguageSelectionActivity::class.java)
                langintent.putExtra("intentfrom", "setting")
                startActivityForResult(langintent, 10)
            } else {
                (activity as HomeActivity).navigate(AboutUsActivity::class.java)
            }
        } else if (position == 3) {
            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
                (activity as HomeActivity).navigate(AboutUsActivity::class.java)
            } else {
                (activity as HomeActivity).navigate(FaqActivity::class.java)
            }
        } else if (position == 4) {
            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
                (activity as HomeActivity).navigate(FaqActivity::class.java)
            }
            else {
                try {
                    when {
                        (activity as HomeActivity).sharedPref.getString("logintype", "")
                            .equals("google", true) -> {
                            (activity as HomeActivity).mGoogleSignInClient.signOut()
                                .addOnCompleteListener {
                                    showLogoutDialog()
                                }
                        }
                        (activity as HomeActivity).sharedPref.getString("logintype", "")
                            .equals("fb", true) -> {
                            LoginManager.getInstance().logOut()
                            showLogoutDialog()
                        }
                        else -> showLogoutDialog()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } else if (position == 5) {
            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true)) {
                if (isMyServiceRunning(MyForgroundTimer::class.java)) {
                    WisamTaxiApplication.shrdPref.edit().putLong("driver_time", 0).apply()
                    WisamTaxiApplication.shrdPref.edit().putString("driver_isactive", "false")
                        .apply()
                    WisamTaxiApplication.shrdPref.edit().putString("stopservice", "driver").apply()
                    activity!!.stopService(Intent(context, MyForgroundTimer::class.java))
                }
                selectActiveRouteDriver("")
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun selectActiveRouteDriver(routeId: String) {
        (activity as HomeActivity).showprogressbar()

        val repository: RetroRepository = RetroRepository.instance
        val drivergetRoutes: MutableLiveData<SelectActiveRouteResponse>

        val request = DriverSelectRoutesRequest(routeId)


        drivergetRoutes = repository.selectActiveRoute(
            WisamTaxiApplication.shrdPref.getString(
                "auth",
                ""
            ).toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),request
        )
        drivergetRoutes.observe(this, object : Observer<SelectActiveRouteResponse> {
            override fun onChanged(t: SelectActiveRouteResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)
                try {
                    when {
                        t?.response!!.success -> {
                            (activity as HomeActivity).dismissprogressBar()
                            showLogoutDialog()
                        }
                        t.response.message.isEmpty() -> {
                            (activity as HomeActivity).dismissprogressBar()
                        }
                        else -> {
                            (activity as HomeActivity).dismissprogressBar()

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
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }

            }
        })
    }

    private fun calldriverLogout() {
        (activity as HomeActivity).showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val logoutdriver: MutableLiveData<LogoutResponse>

        logoutdriver = repository.getLogoutDriver(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString()
        )
        logoutdriver.observe(this, object : Observer<LogoutResponse> {
            override fun onChanged(t: LogoutResponse?) {
                (activity as HomeActivity).dismissprogressBar()

                try {
                    when {
                        t?.response!!.success -> {
                            WisamTaxiApplication.editor.putString("auth", "").apply()
                            (activity as HomeActivity).clearnotification()
                            (activity as HomeActivity).navigateFinishAffinity(
                                WelcomeLoginActivity::class.java
                            )
                        }
                        t.response.message.isEmpty() -> {
                        }
                        else -> {

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast("Your session has expired")
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
                    e.printStackTrace()
                }

            }
        })
    }

    private fun callcustomerLogout() {
        (activity as HomeActivity).showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val logoutCustomer: MutableLiveData<LogoutResponse>

        logoutCustomer = repository.getLogoutCustomer(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString()
        )
        logoutCustomer.observe(this, object : Observer<LogoutResponse> {
            override fun onChanged(t: LogoutResponse?) {

                try {

                    when {
                        t?.response!!.success -> {
                            (activity as HomeActivity).dismissprogressBar()

                            (activity as HomeActivity).clearnotification()
                            WisamTaxiApplication.editor.putString("auth", "").apply()
                            WisamTaxiApplication.editor.putString("logintype", "").apply()

                            (activity as HomeActivity).navigateFinishAffinity(
                                WelcomeLoginActivity::class.java
                            )

                        }
                        t.response.message.isNullOrEmpty() -> {
                            (activity as HomeActivity).dismissprogressBar()
                        }
                        else -> {
                            (activity as HomeActivity).dismissprogressBar()

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast("Your session has expired")
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
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }

            }
        })
    }

    fun showLogoutDialog() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.logout_alertdialog, null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView)
        //show dialog
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        logoutCancelTv = mDialogView.findViewById<View>(R.id.logoutCancelTv) as TextView
        logoutTv = mDialogView.findViewById<View>(R.id.logoutTv) as TextView

        logoutCancelTv.setOnClickListener {
            mAlertDialog.dismiss()
        }

        logoutTv.setOnClickListener {

            if (WisamTaxiApplication.shrdPref.getString("type", "driver").equals("driver", true))
                calldriverLogout()
            else
                callcustomerLogout()

            mAlertDialog.dismiss()
        }
    }

}
