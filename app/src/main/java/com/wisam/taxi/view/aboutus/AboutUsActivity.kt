package com.wisam.taxi.view.aboutus

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.common.toHtml
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import kotlinx.android.synthetic.main.activity_aboutus.*

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutus)

        getAboutUs()

        ivAboutBack.setOnClickListener {
            finish()
        }
    }

    private fun getAboutUs() {
        showprogressbar()
        val map = HashMap<String, String>()
        map["page"] = "aboutUs"
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
                            tvAboutUs.movementMethod = LinkMovementMethod.getInstance()
                            tvAboutUs.text = (t.data.aboutUs?:"").toHtml()
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