package com.wisam.taxi.view.languageSelect

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.LocaleManager
import com.wisam.taxi.common.PreferenceHelper
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import kotlinx.android.synthetic.main.activity_languageselection.*
import java.util.*

class LanguageSelectionActivity : BaseActivity() {
    private lateinit var mAlertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_languageselection)

        if (sharedPref.getString("mylang","en").equals("ar",true)) {
            radiobtneng.setImageResource(R.drawable.ellipse_506)
            radiobtnarab.setImageResource(R.drawable.group_22856)
        }
        else {
            radiobtneng.setImageResource(R.drawable.group_22856)
            radiobtnarab.setImageResource(R.drawable.ellipse_506)
        }

        if (!intent.getStringExtra("intentfrom").isNullOrEmpty() &&
            !intent.getStringExtra("intentfrom").equals("setting", true))
        {
            ivLangBack.visibility = View.INVISIBLE
            radiobtneng.setImageResource(R.drawable.ellipse_506)
            radiobtnarab.setImageResource(R.drawable.ellipse_506)
        }
        else
            ivLangBack.visibility = View.VISIBLE

        ivLangBack.setOnClickListener {
            finish()
        }

        clEnglish.setOnClickListener {
            if (!sharedPref.getString("isFirst", "").isNullOrEmpty())
            {
                if (sharedPref.getString("mylang", "en").equals("ar", true))
                    showDialog("en")
            }
            else
                showDialog("en")

        }

        clArabic.setOnClickListener {
            if (sharedPref.getString("mylang","en").equals("en",true))
                showDialog("ar")
        }

    }

    private fun englishSelected() {
        radiobtneng.setImageResource(R.drawable.group_22856)
        radiobtnarab.setImageResource(R.drawable.ellipse_506)

        changeLanguage("en",this)

        if (intent.getStringExtra("intentfrom").equals("setting", true)) {
            val intent = Intent().apply {
                putExtra("isLanguage", "yes")
            }
            setResult(Activity.RESULT_OK, intent)
            mAlertDialog.dismiss()
            finish()
        } else {
            navigateWithFinish(ChooseUserTypeActivity::class.java)
        }
    }

    private fun arabicSelected() {
        radiobtneng.setImageResource(R.drawable.ellipse_506)
        radiobtnarab.setImageResource(R.drawable.group_22856)
        changeLanguage("ar",this)


        if (intent.getStringExtra("intentfrom").equals("setting", true))
        {
            val intent = Intent().apply {
                putExtra("isLanguage", "yes")
            }
            setResult(Activity.RESULT_OK, intent)
            mAlertDialog.dismiss()
            finish()
        } else {
            navigateWithFinish(ChooseUserTypeActivity::class.java)
        }
    }

    private fun showDialog(type: String) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.logout_alertdialog, null)

        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val logoutCancelTv = mDialogView.findViewById<View>(R.id.logoutCancelTv) as TextView
        val logoutTitle = mDialogView.findViewById<View>(R.id.logoutTitle) as TextView
        val tvLogoutSubtitle = mDialogView.findViewById<View>(R.id.tvLogoutSubtitle) as TextView
        val logoutTv = mDialogView.findViewById<View>(R.id.logoutTv) as TextView

        logoutTitle.text = resources.getString(R.string.pleaseconfirm)
        tvLogoutSubtitle.text = resources.getString(R.string.areyousuretocontinue)
        logoutTv.text = resources.getString(R.string.continuee)

        logoutCancelTv.setOnClickListener {
            mAlertDialog.dismiss()
        }

        logoutTv.setOnClickListener {
            if (type.equals("ar", true))
            {
                editor.putString("isFirst","launched").apply()
                arabicSelected()
            } else {
                editor.putString("isFirst","launched").apply()
                englishSelected()
            }
        }
    }
}
