package com.wisam.taxi.view.verification.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.request.ResendOTPRequest
import com.wisam.taxi.model.request.VerifyOtpRequest
import com.wisam.taxi.model.response.resendotp.ResendOtpResponse
import com.wisam.taxi.model.response.verifyotp.VerifyOtpResponse
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.*
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.digit01
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.digit02
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.digit03
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.digit04
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.ivOtpBack
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.timerAndResend
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.tvMobileNum
import kotlinx.android.synthetic.main.activity_editprofile_otpverify.verifyBtn
import kotlinx.android.synthetic.main.activity_otpverification.*

class EditProfileOtpVerifyActivity : BaseActivity() {
    private var time: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile_otpverify)

        if (sharedPref.getString("type", "customer").equals("driver", true))
        {
            callresendOTPDriver()
        } else {
            callresendOTP()
        }

        tvMobileNum.text = "+"+intent.getStringExtra("code")+" "+intent.getStringExtra("phone")
        timerAndResend.isClickable = false

        handleEditText()

        ivOtpBack.setOnClickListener {
            val intent = Intent().apply {
                putExtra("success", "")
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        timerAndResend.setOnClickListener {

            if (sharedPref.getString("type", "customer").equals("driver", true))
            {
                callresendOTPDriver()
            } else {
                callresendOTP()
            }

        }
        verifyBtn.setOnClickListener {

            if (sharedPref.getString("type", "customer").equals("driver", true))
            {
                if (isValid()) {
                    callverifyOTPDriver()
                }

            } else {
                if (isValid()) {
                    callverifyOTP()
                }
            }
        }

    }

    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("success", "")
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun callverifyOTP()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        val verifyOTP : MutableLiveData<VerifyOtpResponse>

        val otp = digit01.text.toString()+""+digit02.text.toString()+""+digit03.text.toString()+""+digit04.text.toString()

        val verifyotprequest = VerifyOtpRequest("+"+intent.getStringExtra("code"),
            intent.getStringExtra("phone")!!,otp)

        verifyOTP = repository.verifyOTP(sharedPref.getString("mylang", "en").toString(),verifyotprequest)
        verifyOTP.observe(this, object : Observer<VerifyOtpResponse> {
            override fun onChanged(t: VerifyOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            time!!.cancel()
                            dismissprogressBar()

                            val intent = Intent().apply {
                                putExtra("success", "success")
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()

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
                }

            }
        })
    }

    private fun callresendOTP()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        val resendOTP : MutableLiveData<ResendOtpResponse>


        val resendOTPRequest = ResendOTPRequest("+"+intent.getStringExtra("code"),
            intent.getStringExtra("phone")!!
        )

        resendOTP = repository.resendOTP(sharedPref.getString("mylang", "en").toString(),resendOTPRequest)
        resendOTP.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            timer()
                            timerAndResend.setTextColor(Color.parseColor("#000000"))
                            timerAndResend?.isClickable = false

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
                }

            }
        })
    }

    private fun callverifyOTPDriver()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        val verifyOTP : MutableLiveData<VerifyOtpResponse>

        val otp = digit01.text.toString()+""+digit02.text.toString()+""+digit03.text.toString()+""+digit04.text.toString()

        val verifyotprequest = VerifyOtpRequest("+"+intent.getStringExtra("code"),
            intent.getStringExtra("phone")!!,otp)

        verifyOTP = repository.verifyOTPDriver(sharedPref.getString("mylang", "en").toString(),verifyotprequest)
        verifyOTP.observe(this, object : Observer<VerifyOtpResponse> {
            override fun onChanged(t: VerifyOtpResponse?)
            {

                try {

                    when {
                        t?.response!!.success -> {
                            time!!.cancel()
                            dismissprogressBar()

                            val intent = Intent().apply {
                                putExtra("success", "success")
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()

                        }
                        t.response.message.isEmpty() -> {
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
                }
            }
        })
    }

    private fun callresendOTPDriver()
    {
        showprogressbar()
        val repository : RetroRepository = RetroRepository.instance
        val resendOTP : MutableLiveData<ResendOtpResponse>

        val resendOTPRequest = ResendOTPRequest("+"+intent.getStringExtra("code"),
            intent.getStringExtra("phone")!!
        )

        resendOTP = repository.resendOTPDriver(sharedPref.getString("mylang", "en").toString(),resendOTPRequest)
        resendOTP.observe(this, object : Observer<ResendOtpResponse> {
            override fun onChanged(t: ResendOtpResponse?)
            {
                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            timer()
                            timerAndResend.setTextColor(Color.parseColor("#000000"))
                            timerAndResend?.isClickable = false

                        }
                        t.response.message.isEmpty() -> {
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
                }
            }
        })
    }

    private fun timer(){
        time = object : CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long)
            {

                var seconds = ""+millisUntilFinished % 60000 / 1000
                var minutes = ""+millisUntilFinished / 60000

                if (seconds.toInt() <= 9)
                    seconds = "0$seconds"

                if (minutes.toInt() <= 9)
                    minutes = "0$minutes"

                timerAndResend?.text = "$minutes:$seconds"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                timerAndResend.setTextColor(Color.parseColor("#0b8793"))
                timerAndResend?.text = ""+getString(R.string.resend)
                timerAndResend?.isClickable = true
            }
        }.start()
    }


    private fun handleEditText() {

        digit01?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (digit01.text.toString().isNotEmpty())
                    digit02?.requestFocus()
                else
                    digit01.requestFocus()
            }
        })
        digit02?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (digit02.text.toString().isNotEmpty())
                    digit03.requestFocus()
                else
                    digit02.requestFocus()
            }
        })
        digit03?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (digit03.text.toString().isNotEmpty())
                    digit04.requestFocus()
                else
                    digit03.requestFocus()
            }
        })

        digit04?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        digit01.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN){
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        digit01.text.clear()
                        digit01.requestFocus()
                    }
                }}
            return@setOnKeyListener false
        }
        digit02.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN){
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        if (digit02.text.toString().isNotEmpty())
                        {
                            digit02.text.clear()
                            digit02.requestFocus()
                        } else
                        {
                            digit01.text.clear()
                            digit01.requestFocus()
                        }
                    }
                }}
            return@setOnKeyListener false
        }
        digit03.setOnKeyListener { _, keyCode, event ->

            if (event.action == KeyEvent.ACTION_DOWN)
            {
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        if (digit03.text.toString().isNotEmpty())
                        {
                            digit03.text.clear()
                            digit03.requestFocus()
                        } else{
                            digit02.text.clear()
                            digit02.requestFocus()
                        }
                    }
                }}
            return@setOnKeyListener false
        }
        digit04.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN)
            {
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        if (digit04.text.toString().isNotEmpty())
                        {
                            digit04.text.clear()
                            digit04.requestFocus()
                        } else
                        {
                            digit03.text.clear()
                            digit03.requestFocus()
                        }
                    }
                }
            }
            return@setOnKeyListener false
        }
    }

    private fun isValid():Boolean{

        if (digit01.text.toString().trim().isEmpty() || digit02.text.toString().trim().isEmpty() || digit03.text.toString().trim().isEmpty()
            || digit04.text.toString().trim().isEmpty()){
            showToast(resources.getString(R.string.entervalidotp))
            return false
        }
        return true
    }
}
