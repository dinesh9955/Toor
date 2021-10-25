package com.wisam.taxi.common

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import java.util.*

object LocaleManager {

    val SELECTED_LANGUAGE = "MEW_CURRENT_-- USER_LANGUAGE"
    var mSharedPreference: SharedPreferences? = null

    var mEnglishFlag = "en"
    var mArabicFlag = "ar"

    fun setLocale(context: Context?): Context {
        return updateResources(context!!, getCurrentLanguage(context)!!)
    }

    inline fun setNewLocale(context: Context, language: String) {
        persistLanguagePreference(context, language)
        updateResources(context, language)
    }

    inline fun getCurrentLanguage(context: Context?): String? {

        if (mSharedPreference == null)
            mSharedPreference = PreferenceHelper.defaultPrefs(context!!)

        var mCurrentLanguage: String? = mSharedPreference!!.getString(SELECTED_LANGUAGE,"en")
        return mCurrentLanguage
    }

    fun persistLanguagePreference(context: Context, language: String) {
        if (mSharedPreference == null)
            mSharedPreference = PreferenceHelper.defaultPrefs(context)

        mSharedPreference!!.edit().putString(SELECTED_LANGUAGE,language).apply()
    }

    fun updateResources(context: Context, language: String): Context {

        var contextFun = context

        var locale = if (language.equals(mEnglishFlag,true)){
            Locale(language,"US")
        }else{
            Locale(language,"IQ")
        }
        Locale.setDefault(locale)

        var resources = context.resources
        var configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(Locale("en","US"))
        contextFun = context.createConfigurationContext(configuration)

        Log.e("LanguageSwitch"," "+language+" "+resources.getString(R.string.easy_bookingsubTitle))
        return contextFun
    }

    fun fixUpLocale(ctx: Context, newLocale: Locale) {
        val res = ctx.resources
        val config = res.configuration
        val curLocale = getLocale(config)
        if (curLocale != newLocale) {
            Locale.setDefault(newLocale)
            val conf = Configuration(config)
            conf.setLocale(newLocale)
            res.updateConfiguration(conf, res.displayMetrics);
        }
    }

    private fun getLocale(config: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            config.locale;
        }
    }

}