package com.wisam.taxi.view.welcome.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.view.driverModule.routes.RoutesActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        (tvWelcomeTick.drawable as Animatable).start()

        Handler().postDelayed({

            if (sharedPref.getString("type","customer").equals("driver",true))
                navigateFinishAffinity(WelcomeLoginActivity::class.java)
            else
            {

                if (!sharedPref.getString("logintype","").isNullOrEmpty() && (sharedPref.getString("logintype","").equals("fb",true)
                            || sharedPref.getString("logintype","").equals("google",true) ||
                            !sharedPref.getString("logintype","").equals("simple",true)))
                    navigateFinishAffinity(HomeActivity::class.java)
                else
                    navigateFinishAffinity(WelcomeLoginActivity::class.java)

            }
        },1500)
    }
}
