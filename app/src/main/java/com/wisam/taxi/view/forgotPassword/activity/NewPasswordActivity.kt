package com.wisam.taxi.view.forgotPassword.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.request.ResetPasswordRequest
import com.wisam.taxi.model.response.resendotp.ResendOtpResponse
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.verification.activity.OtpVerificationActivity
import com.wisam.taxi.view.welcome.activity.WelcomeActivity
import com.wisam.taxi.view.welcome.activity.WelcomeLoginActivity
import kotlinx.android.synthetic.main.activity_newpassword.*

class NewPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newpassword)

        etNewPassword.isCursorVisible = true
        etNewPassword.requestFocus()


        ivNewPasswordBack.setOnClickListener {
            navigateWithFinish(WelcomeLoginActivity::class.java)
        }

        tvConfirmPasswordSave.setOnClickListener {

            if (sharedPref.getString("type", "customer").equals("driver", true))
            {
                if (isValid())
                {
                    callresetPasswordDriver()
                }

            } else {
                if (isValid())
                {
                    callresetPassword()
                }
            }

        }
    }

    override fun onBackPressed() {
        navigateWithFinish(WelcomeLoginActivity::class.java)
    }

    private fun callresetPassword()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        var resetPassword : MutableLiveData<ResendOtpResponse>


        var resetPasswordRequest = ResetPasswordRequest("+"+intent.getStringExtra("code"),""+intent.getStringExtra("mobile_no")
        ,etNewPassword.text.toString())

        resetPassword = repository.resetPassword(sharedPref.getString("mylang", "en").toString(),resetPasswordRequest)
        resetPassword.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            editor.putString("password",etNewPassword.text.toString()).apply()
                            showToast(t.response.message)
                            dismissprogressBar()
                            navigateFinishAffinity(WelcomeLoginActivity::class.java)
                        }
                        t.response.message.isNullOrEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            Toast.makeText(context, t.response.message, Toast.LENGTH_SHORT).show()
                            dismissprogressBar()
                        }
                    }
                }
                catch (e:Exception)
                {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE",""+e.toString())
                }

            }
        })
    }

    private fun callresetPasswordDriver()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        var resetPassword : MutableLiveData<ResendOtpResponse>


        var resetPasswordRequest = ResetPasswordRequest("+"+intent.getStringExtra("code"),""+intent.getStringExtra("mobile_no")
            ,etNewPassword.text.toString())

        resetPassword = repository.resetPasswordDriver(sharedPref.getString("mylang", "en").toString(),resetPasswordRequest)
        resetPassword.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            editor.putString("password_driver",etNewPassword.text.toString()).apply()
                            showToast(t.response.message)
                            dismissprogressBar()
                            navigateFinishAffinity(WelcomeLoginActivity::class.java)
                        }
                        t.response.message.isNullOrEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            Toast.makeText(context, t.response.message, Toast.LENGTH_SHORT).show()
                            dismissprogressBar()
                        }
                    }
                }
                catch (e:Exception)
                {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE",""+e.toString())
                }

            }
        })
    }

    private fun isValid():Boolean{

        if (etNewPassword.text.toString().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseenterpassword))
            return false
        }else if (etNewPassword.text.toString().length < 8) {
            showToast(resources.getString(R.string.passwordlength))
            return false
        } else if (etNewConfirmPassword.text.toString().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseenterconfirmpassword))
            return false
        }
        else if (etNewConfirmPassword.text.toString().length < 8) {
            showToast(resources.getString(R.string.confirmpasswordlength))
            return false
        }
        else if (!etNewPassword.text.toString().equals(etNewConfirmPassword.text.toString(), false))
        {
            showToast(resources.getString(R.string.passdonotmatch))
            return false
        }
        return true
    }
}
