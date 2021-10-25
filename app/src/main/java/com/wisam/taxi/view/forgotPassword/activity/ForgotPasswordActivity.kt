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
import com.wisam.taxi.model.request.ResendOTPRequest
import com.wisam.taxi.model.response.resendotp.ResendOtpResponse
import com.wisam.taxi.view.verification.activity.OtpVerificationActivity
import com.ybs.countrypicker.CountryPicker
import kotlinx.android.synthetic.main.activity_forgotpassword.*
import kotlinx.android.synthetic.main.activity_forgotpassword.clCodePicker
import kotlinx.android.synthetic.main.activity_otpverification.*
import kotlinx.android.synthetic.main.activity_signupasdriver.*

class ForgotPasswordActivity : BaseActivity()
{
    var countrycode :String = "964"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)


        etForgotmobile.isCursorVisible = true
        etForgotmobile.requestFocus()

        ivForgotBack.setOnClickListener {
            finish()
        }

        tvForgotNext.setOnClickListener {

            if (etForgotmobile.text.toString().isNotEmpty())
            {
                if (sharedPref.getString("type", "customer").equals("driver", true))
                    callresendOTPDriver()
                else
                    callresendOTP()
            }
            else
                showToast(getString(R.string.pleaseentermobileNo))
        }


        clCodePicker.setOnClickListener {
            picker = CountryPicker.newInstance("Select Country");
            picker.setListener { name, code, p2, p3 ->
                try {
                    edtForgotPassCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                    countrycode = ""+edtForgotPassCode.selectedCountryCodeAsInt.toString()
                }catch (e:Exception){
                    e.printStackTrace()
                }
                picker.dismiss()
            }
            picker.show(supportFragmentManager, "COUNTRY_PICKER");
        }
    }

    private fun callresendOTP() {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        var resendOTP : MutableLiveData<ResendOtpResponse>


        var resendOTPRequest = ResendOTPRequest("+$countrycode",etForgotmobile.text.toString())

        resendOTP = repository.resendOTP(sharedPref.getString("mylang", "en").toString(),resendOTPRequest)

        resendOTP.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                            intent.putExtra("intentfrom", "forgotpass")
                            intent.putExtra("code", countrycode)
                            intent.putExtra("phone", etForgotmobile.text.toString())
                            startActivity(intent)
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


    private fun callresendOTPDriver()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        val resendOTP : MutableLiveData<ResendOtpResponse>


        val resendOTPRequest = ResendOTPRequest("+$countrycode",etForgotmobile.text.toString())

        resendOTP = repository.resendOTPDriver(sharedPref.getString("mylang", "en").toString(),resendOTPRequest)
        resendOTP.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                            intent.putExtra("intentfrom", "forgotpass")
                            intent.putExtra("code", countrycode)
                            intent.putExtra("phone", etForgotmobile.text.toString())
                            startActivity(intent)

                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            showToast(t.response.message)
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
}
