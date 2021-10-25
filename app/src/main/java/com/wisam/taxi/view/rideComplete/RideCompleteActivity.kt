package com.wisam.taxi.view.rideComplete

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.request.RateToDriver
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_ridecomplete.*
import java.text.DecimalFormat

class RideCompleteActivity : BaseActivity()
{
    private var ratingvalue :Double = 5.0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ridecomplete)

        editor.putString("requestId","")
        editor.putString("remainingTime","")
        editor.apply()
        editor.commit()

        (tvRideCompleteTick.getDrawable() as Animatable).start()

        tvRideCompleteSubmit.setOnClickListener {
            rateToDriver()
        }

        ivRideCompleteRating.setOnRatingBarChangeListener { ratingBar, rating, _ ->

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
            ivRideCompleteProfile.setImageResource(R.drawable.group_445)
        else
            Picasso.get().load(WisamTaxiApplication.BASE_URL+""+profile).placeholder(R.drawable.group_445).into(ivRideCompleteProfile)


        try {

            if (intent.getStringExtra("rating").isNullOrEmpty())
                tvRideCompleteRating.text = "0.0"
            else
                tvRideCompleteRating.text = "" + DecimalFormat("#.#").format(intent.getStringExtra("rating"))

        }catch (e:Exception){
            e.printStackTrace()
        }
        tvRideCompleteName.text = ""+intent.getStringExtra("name")

    }

    override fun onBackPressed() {
        navigateFinishAffinity(HomeActivity::class.java)
    }

    private fun rateToDriver()
    {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val rateToDriver = RateToDriver(""+intent.getStringExtra("driverId"),""+intent.getStringExtra("bookingId"),
            "Awesome Driver",ratingvalue)

        val getData: MutableLiveData<LogoutResponse>
        getData = repository.rateToDriver(sharedPref.getString("auth","").toString(),
            sharedPref.getString("mylang", "en").toString(),rateToDriver)
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

                            if (t.response.message.equals("Authorization not correct", true)) {
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
                            else
                                showToast(t.response.message)
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
