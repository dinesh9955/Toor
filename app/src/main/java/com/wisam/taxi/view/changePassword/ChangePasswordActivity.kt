package com.wisam.taxi.view.changePassword

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.request.ChangePasswordRequest
import com.wisam.taxi.model.response.getprofile.GetProfileResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_changepassword.*
import kotlinx.android.synthetic.main.fragment_account.*

class ChangePasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changepassword)

        ivChangePassBack.setOnClickListener {
            hideKeyboard()
            finish()
        }

        ivChangePassCorrect.setOnClickListener {

            if (sharedPref.getString("type", "customer").equals("driver", true)) {
                if (isValid())
                    changePasswordDriver()
            } else {
                if (isValid())
                    changePassword()
            }
        }
    }

    private fun changePassword() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        var resetPassword: MutableLiveData<GetProfileResponse>

        val request =
            ChangePasswordRequest(etCurrentPass.text.toString(), etChangePassNew.text.toString())

        resetPassword = repository.changePassword(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(),
            request
        )

        resetPassword.observe(this, object : Observer<GetProfileResponse> {
            override fun onChanged(t: GetProfileResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {

                            editor.putString("password", etChangePassNew.text.toString()).apply()
                            showToast(t.response.message)
                            dismissprogressBar()
                            finish()

                        }
                        t.response.message.isNullOrEmpty() -> {
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
                                else -> {
                                    showToast(t.response.message)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }

            }
        })
    }

    private fun changePasswordDriver() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        var resetPassword: MutableLiveData<GetProfileResponse>

        val request =
            ChangePasswordRequest(etCurrentPass.text.toString(), etChangePassNew.text.toString())

        resetPassword = repository.changePasswordDriver(
            WisamTaxiApplication.shrdPref.getString(
                "auth",
                ""
            ).toString(),
            sharedPref.getString("mylang", "en").toString(), request
        )

        resetPassword.observe(this, object : Observer<GetProfileResponse> {
            override fun onChanged(t: GetProfileResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)
                try {
                    when {
                        t?.response!!.success -> {
                            editor.putString("password_driver", etChangePassNew.text.toString())
                                .apply()
                            showToast(t.response.message)
                            dismissprogressBar()
                            finish()
                        }
                        t.response.message.isNullOrEmpty() -> {
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
                                else -> {
                                    showToast(t.response.message)
                                }
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

    private fun isValid(): Boolean {

        if (etCurrentPass.text.toString().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseentercurrentpassword))
            return false
        } else if (etCurrentPass.text.toString().length < 8) {
            showToast(resources.getString(R.string.pleaseentervalidcurrentpassword))
            return false
        } else if (etChangePassNew.text.toString().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseenternewpassword))
            return false
        } else if (etChangePassNew.text.toString().length < 8) {
            showToast(resources.getString(R.string.newpasswordlength))
            return false
        } else if (etChangePassConf.text.toString().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseenterconfirmpassword))
            return false
        } else if (etChangePassConf.text.toString().length < 8) {
            showToast(resources.getString(R.string.confirmpasswordlength))
            return false
        } else if (!etChangePassNew.text.toString().equals(
                etChangePassConf.text.toString(),
                false
            )
        ) {
            showToast(resources.getString(R.string.passdonotmatch))
            return false
        }
        return true
    }
}
