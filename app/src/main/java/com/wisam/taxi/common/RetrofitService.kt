package com.wisam.taxi.common

import com.wisam.taxi.base.WisamTaxiApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object RetrofitService {

    fun getUnsafeOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder().addInterceptor(interceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
//                    .addHeader("Authorization", WisamTaxiApplication.shrdPref.getString("auth","").toString())
                    .build()
                chain.proceed(newRequest)
            }.build()

        return builder
    }


    private val retrofit = Retrofit.Builder()
        .baseUrl(WisamTaxiApplication.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient())
//        .client(GagSsl.getUnsafeOkHttpClient())
//        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
        .build()


    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }

}