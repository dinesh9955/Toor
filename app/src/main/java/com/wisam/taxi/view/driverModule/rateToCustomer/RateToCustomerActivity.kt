package com.wisam.taxi.view.driverModule.rateToCustomer

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Animatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverRequest.RateToUser
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_ratetocustomer.*

class RateToCustomerActivity : BaseActivity()
{
    private var ratingvalue :Double = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ratetocustomer)

        (tvRateToCustomerTick.drawable as Animatable).start()

        tvRateToCustomerSubmit.setOnClickListener {
            rateToCustomer()
        }

        ivRateToCustomerRating.setOnRatingBarChangeListener { ratingBar, rating, _ ->

            if (rating<1.0f)
            {
                ratingBar.rating = 1f
                ratingvalue = 1.0
            }
            else
                ratingvalue = rating.toDouble()
        }

        val profile =""+intent.getStringExtra("profilePic")

        if (profile.isEmpty())
            ivRateToCustomerProfile.setImageResource(R.drawable.group_445)
        else
            Picasso.get().load(WisamTaxiApplication.BASE_URL+""+profile).placeholder(R.drawable.group_445).into(ivRateToCustomerProfile)

        try {

            if (intent.getStringExtra("rating").isNullOrEmpty())
                tvRateToCustomerRating.text = "0.0"
            else
                tvRateToCustomerRating.text = ""+intent.getStringExtra("rating")

        }catch (e:Exception){
            e.printStackTrace()
        }

        tvRateToCustomerName.text = ""+intent.getStringExtra("name")

    }

    override fun onBackPressed() {
    }

    private fun rateToCustomer() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val rateToCustomer = RateToUser(""+intent.getStringExtra("userId"),""+intent.getStringExtra("bookingId"),
            "Good Customer",ratingvalue)

        val getData: MutableLiveData<LogoutResponse>
        getData = repository.rateToCustomer(sharedPref.getString("auth","").toString(),
            sharedPref.getString("mylang", "en").toString(),rateToCustomer)
        getData.observe(this, object : Observer<LogoutResponse> {
            override fun onChanged(t: LogoutResponse?) {
                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            clearnotification()
                            navigateFinishAffinity(HomeActivity::class.java)
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> showToast(t.response.message)
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
}
