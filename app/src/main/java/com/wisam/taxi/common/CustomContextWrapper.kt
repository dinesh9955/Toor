package com.wisam.taxi.common

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

class CustomContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale): ContextWrapper {
            var mContext = context
            var res = mContext.resources
            var configuration = res.configuration

            when {
                Build.VERSION.SDK_INT > Build.VERSION_CODES.N -> {
                    configuration.setLocale(newLocale)
                    var localList = LocaleList(newLocale)
                    LocaleList.setDefault(localList)
//                    configuration.locales = localList
                    mContext = mContext.createConfigurationContext(configuration)
                }
                Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                    configuration.setLocale(newLocale)
                    mContext = mContext.createConfigurationContext(configuration)
                }
                else -> {
                    configuration.locale = newLocale
                    res.updateConfiguration(configuration, res.displayMetrics)
                }
            }
            return ContextWrapper(mContext)
        }
    }
}