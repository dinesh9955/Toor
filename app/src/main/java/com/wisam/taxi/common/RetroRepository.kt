package com.wisam.taxi.common

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.interfaces.ApiInterface
import com.wisam.taxi.model.driverRequest.DriverGetRoutesRequest
import com.wisam.taxi.model.driverRequest.DriverSelectRoutesRequest
import com.wisam.taxi.model.driverRequest.IsAvailableRequest
import com.wisam.taxi.model.driverRequest.RateToUser
import com.wisam.taxi.model.driverResponse.getDriverRoutes.DriverRouteListResponse
import com.wisam.taxi.model.driverResponse.getRoutes.DriverGetRoutesResponse
import com.wisam.taxi.model.driverResponse.getbooking.DriverGetBookingResponse
import com.wisam.taxi.model.driverResponse.getbookingDetails.GetBookingDetailsDriver
import com.wisam.taxi.model.driverResponse.getdocuments.GetDocumentsResponse
import com.wisam.taxi.model.driverResponse.getvehicletype.GetVehicleTypeResponse
import com.wisam.taxi.model.driverResponse.login.DriverLoginResponse
import com.wisam.taxi.model.driverResponse.profile.DriverProfileResponse
import com.wisam.taxi.model.driverResponse.register.DriverSignupResponse
import com.wisam.taxi.model.driverResponse.routeslist.RoutesListResponse
import com.wisam.taxi.model.driverResponse.selectActiveRoute.SelectActiveRouteResponse
import com.wisam.taxi.model.driverResponse.updateDocuments.UpdateDocumentsResponse
import com.wisam.taxi.model.request.*
import com.wisam.taxi.model.response.booking.UsersBookingResponse
import com.wisam.taxi.model.response.getBookingDetailsUser.UserBookingDetailResponse
import com.wisam.taxi.model.response.getprofile.GetProfileResponse
import com.wisam.taxi.model.response.login.LoginResponse
import com.wisam.taxi.model.response.logout.LogoutResponse
import com.wisam.taxi.model.response.register.SignUpResponse
import com.wisam.taxi.model.response.resendotp.ResendOtpResponse
import com.wisam.taxi.model.response.socialLogin.SocialLoginResponse
import com.wisam.taxi.model.response.updateprofile.UpdateProfileResponse
import com.wisam.taxi.model.response.userhome.UserHomeResponse
import com.wisam.taxi.model.response.verifyotp.VerifyOtpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetroRepository {

    private val appApi: ApiInterface

    init {
        appApi = RetrofitService.createService(ApiInterface::class.java)
    }

    companion object {

        private var retroRepository: RetroRepository? = null
        private var retroRepositoryChat: RetroRepository? = null

        val instance: RetroRepository
            get() {
                if (retroRepository == null) {
                    retroRepository = RetroRepository()
                }
                return retroRepository!!
            }
    }

    fun getSignUp(
        language: String,
        countryCode: RequestBody?,
        phone: RequestBody?,
        email: RequestBody?,
        fullName: RequestBody?,
        profile: MultipartBody.Part?,
        address: RequestBody?,
        password: RequestBody?,
        address1: RequestBody?,
        latitude: RequestBody?,
        longitude: RequestBody?,
        deviceId: RequestBody?,
        deviceType: RequestBody?
    ): MutableLiveData<SignUpResponse> {
        val newsData = MutableLiveData<SignUpResponse>()
        appApi.postSignUp(
            language,
            countryCode,
            phone,
            email,
            fullName,
            profile,
            address,
            password,
            address1,
            latitude,
            longitude,
            deviceId,
            deviceType
        )
            .enqueue(object : Callback<SignUpResponse> {
                override fun onResponse(
                    call: Call<SignUpResponse>,
                    response: Response<SignUpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        Log.d("apiData ", "${response.body()}")
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        return newsData
    }

    fun getLogin(lang: String, loginRequest: LoginRequest): MutableLiveData<LoginResponse> {
        val newsData = MutableLiveData<LoginResponse>()

        appApi.postLogin(lang, loginRequest)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun verifyOTP(
        lang: String,
        verifyOtpRequest: VerifyOtpRequest
    ): MutableLiveData<VerifyOtpResponse> {
        val newsData = MutableLiveData<VerifyOtpResponse>()

        appApi.verifyOTP(lang, verifyOtpRequest)
            .enqueue(object : Callback<VerifyOtpResponse> {
                override fun onResponse(
                    call: Call<VerifyOtpResponse>,
                    response: Response<VerifyOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun resendOTP(
        lang: String,
        resendOTPRequest: ResendOTPRequest
    ): MutableLiveData<ResendOtpResponse> {
        val newsData = MutableLiveData<ResendOtpResponse>()

        appApi.resendOtp(lang, resendOTPRequest)
            .enqueue(object : Callback<ResendOtpResponse> {
                override fun onResponse(
                    call: Call<ResendOtpResponse>,
                    response: Response<ResendOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<ResendOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun resetPassword(
        lang: String,
        resetPasswordRequest: ResetPasswordRequest
    ): MutableLiveData<ResendOtpResponse> {
        val newsData = MutableLiveData<ResendOtpResponse>()

        appApi.resetPassword(lang, resetPasswordRequest)
            .enqueue(object : Callback<ResendOtpResponse> {
                override fun onResponse(
                    call: Call<ResendOtpResponse>,
                    response: Response<ResendOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<ResendOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun getCustomerprofile(auth: String, lang: String): MutableLiveData<GetProfileResponse> {
        val newsData = MutableLiveData<GetProfileResponse>()

        appApi.getProfile(auth, lang)
            .enqueue(object : Callback<GetProfileResponse> {
                override fun onResponse(
                    call: Call<GetProfileResponse>,
                    response: Response<GetProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<GetProfileResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun changePassword(
        auth: String,
        lang: String,
        request: ChangePasswordRequest
    ): MutableLiveData<GetProfileResponse> {
        val newsData = MutableLiveData<GetProfileResponse>()

        appApi.changePassword(auth, lang, request)
            .enqueue(object : Callback<GetProfileResponse> {
                override fun onResponse(
                    call: Call<GetProfileResponse>,
                    response: Response<GetProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<GetProfileResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun userHome(
        auth: String,
        lang: String,
        request: UserhomeRequest
    ): MutableLiveData<UserHomeResponse> {
        val newsData = MutableLiveData<UserHomeResponse>()

        appApi.userHome(auth, lang, request)
            .enqueue(object : Callback<UserHomeResponse> {
                override fun onResponse(
                    call: Call<UserHomeResponse>,
                    response: Response<UserHomeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<UserHomeResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun updateProfile(
        auth: String,
        lang: String,
        phone: RequestBody?,
        fullName: RequestBody?,
        email: RequestBody?,
        countryCode: RequestBody?,
        address: RequestBody?,
        password: RequestBody?,
        deviceToken: RequestBody?,
        deviceType: RequestBody?,
        address1: RequestBody?,
        latitude: RequestBody?,
        longitude: RequestBody?,
        profile: MultipartBody.Part?

    ): MutableLiveData<UpdateProfileResponse> {

        val newsData = MutableLiveData<UpdateProfileResponse>()

        appApi.updatePofile(
            auth,
            lang,
            phone,
            fullName,
            email,
            countryCode,
            address,
            password,
            deviceToken,
            deviceType,
            address1,
            latitude,
            longitude,
            profile
        )
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(
                    call: Call<UpdateProfileResponse>,
                    response: Response<UpdateProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        Log.d("apiData ", "${response.body()}")
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        return newsData
    }

    fun socialLogin(
        lang: String,
        socialLoginRequest: SocialLoginRequest
    ): MutableLiveData<SocialLoginResponse> {
        val newsData = MutableLiveData<SocialLoginResponse>()

        appApi.socialLogin(lang, socialLoginRequest)
            .enqueue(object : Callback<SocialLoginResponse> {
                override fun onResponse(
                    call: Call<SocialLoginResponse>,
                    response: Response<SocialLoginResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<SocialLoginResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("Exception", "" + e.printStackTrace())
                    }
                }
            })
        return newsData
    }


    fun rateToDriver(
        auth: String,
        lang: String,
        request: RateToDriver
    ): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()

        appApi.ratetoDriver(auth, lang, request)
            .enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }


    fun getusersBooking(
        token: String,
        lang: String,
        map: Map<String, String>
    ): MutableLiveData<UsersBookingResponse> {
        val newsData = MutableLiveData<UsersBookingResponse>()

        appApi.getBooking(token, lang, map)
            .enqueue(object : Callback<UsersBookingResponse> {
                override fun onResponse(
                    call: Call<UsersBookingResponse>,
                    response: Response<UsersBookingResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<UsersBookingResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        return newsData
    }


    fun userCancelBooking(
        token: String,
        lang: String,
        data: String
    ): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()
        try {

            appApi.userCancelBooking(token, lang, data)
                .enqueue(object : Callback<LogoutResponse?> {
                    override fun onResponse(
                        call: Call<LogoutResponse?>,
                        response: Response<LogoutResponse?>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("AppTag", " url();" + response.raw().request().url())
                            newsData.value = response.body()
                        } else {
                            Log.d("apiDataPro ", "${response}")
                        }
                    }

                    override fun onFailure(call: Call<LogoutResponse?>, t: Throwable) {
                        Log.d("apiDataPro ", "${t}")
                        newsData.value = null
                        try {
                            if (t.toString().contains("SocketTimeoutException")) {
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return newsData
    }

    fun usergetBookingDetails(
        token: String,
        lang: String,
        data: String
    ): MutableLiveData<UserBookingDetailResponse> {
        val newsData = MutableLiveData<UserBookingDetailResponse>()
        try {

            appApi.getBookingDetailsUser(token, lang, data)
                .enqueue(object : Callback<UserBookingDetailResponse?> {
                    override fun onResponse(
                        call: Call<UserBookingDetailResponse?>,
                        response: Response<UserBookingDetailResponse?>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("AppTag", " url();" + response.raw().request().url())
                            newsData.value = response.body()
                        } else {
                            Log.d("apiDataPro ", "${response}")
                        }
                    }

                    override fun onFailure(call: Call<UserBookingDetailResponse?>, t: Throwable) {
                        Log.d("apiDataPro ", "${t}")
                        newsData.value = null
                        try {
                            if (t.toString().contains("SocketTimeoutException")) {
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return newsData
    }

    fun getLogoutCustomer(auth: String, lang: String): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()

        appApi.customerLogout(auth, lang)
            .enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }


//
//
//
//
//    Driver Module
//
//
//
//
//
//


    fun driverSignUp(
        lang: String,
        countryCode: RequestBody?,
        phone: RequestBody?,
        fullName: RequestBody?,
        password: RequestBody?,
        latitude: RequestBody?,
        longitude: RequestBody?,
        profile: MultipartBody.Part?,
        buildingNumber: RequestBody?,
        flatNumber: RequestBody?,
        contractCopy: MultipartBody.Part?,
        license: MultipartBody.Part?,
        deviceId: RequestBody?,
        deviceType: RequestBody?,
        vehicleName: RequestBody?,
        vehicleModel: RequestBody?
    ): MutableLiveData<DriverSignupResponse> {
        val newsData = MutableLiveData<DriverSignupResponse>()

        appApi.signUpDriver(
            lang,
            countryCode,
            phone,
            fullName,
            password,
            latitude,
            longitude,
            profile,
            buildingNumber,
            flatNumber,
            contractCopy,
            license,
            deviceId,
            deviceType,
            vehicleName,
            vehicleModel
        )
            .enqueue(object : Callback<DriverSignupResponse> {
                override fun onResponse(
                    call: Call<DriverSignupResponse>,
                    response: Response<DriverSignupResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        Log.d("apiData ", "${response.body()}")
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverSignupResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        return newsData
    }

    fun driverLogin(
        lang: String,
        loginRequest: LoginRequest
    ): MutableLiveData<DriverLoginResponse> {
        val newsData = MutableLiveData<DriverLoginResponse>()

        appApi.driverLogin(lang, loginRequest)
            .enqueue(object : Callback<DriverLoginResponse> {
                override fun onResponse(
                    call: Call<DriverLoginResponse>,
                    response: Response<DriverLoginResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverLoginResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun verifyOTPDriver(
        lang: String,
        verifyOtpRequest: VerifyOtpRequest
    ): MutableLiveData<VerifyOtpResponse> {
        val newsData = MutableLiveData<VerifyOtpResponse>()

        appApi.verifyOTPDriver(lang, verifyOtpRequest)
            .enqueue(object : Callback<VerifyOtpResponse> {
                override fun onResponse(
                    call: Call<VerifyOtpResponse>,
                    response: Response<VerifyOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun resendOTPDriver(
        lang: String,
        resendOTPRequest: ResendOTPRequest
    ): MutableLiveData<ResendOtpResponse> {
        val newsData = MutableLiveData<ResendOtpResponse>()

        appApi.resendOtpDriver(lang, resendOTPRequest)
            .enqueue(object : Callback<ResendOtpResponse> {
                override fun onResponse(
                    call: Call<ResendOtpResponse>,
                    response: Response<ResendOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<ResendOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun resetPasswordDriver(
        lang: String,
        resetPasswordRequest: ResetPasswordRequest
    ): MutableLiveData<ResendOtpResponse> {
        val newsData = MutableLiveData<ResendOtpResponse>()

        appApi.resetPasswordDriver(lang, resetPasswordRequest)
            .enqueue(object : Callback<ResendOtpResponse> {
                override fun onResponse(
                    call: Call<ResendOtpResponse>,
                    response: Response<ResendOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<ResendOtpResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun getDriverprofile(auth: String, lang: String): MutableLiveData<DriverProfileResponse> {
        val newsData = MutableLiveData<DriverProfileResponse>()

        appApi.getProfileDriver(auth, lang)
            .enqueue(object : Callback<DriverProfileResponse> {
                override fun onResponse(
                    call: Call<DriverProfileResponse>,
                    response: Response<DriverProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverProfileResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun changePasswordDriver(
        auth: String,
        lang: String,
        request: ChangePasswordRequest
    ): MutableLiveData<GetProfileResponse> {
        val newsData = MutableLiveData<GetProfileResponse>()

        appApi.changePasswordDriver(auth, lang, request)
            .enqueue(object : Callback<GetProfileResponse> {
                override fun onResponse(
                    call: Call<GetProfileResponse>,
                    response: Response<GetProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<GetProfileResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun getDriverDocuments(auth: String, lang: String): MutableLiveData<GetDocumentsResponse> {
        val newsData = MutableLiveData<GetDocumentsResponse>()

        appApi.getDocumentDriver(auth, lang)
            .enqueue(object : Callback<GetDocumentsResponse> {
                override fun onResponse(
                    call: Call<GetDocumentsResponse>,
                    response: Response<GetDocumentsResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<GetDocumentsResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }


    fun updateDocuments(
        auth: String,
        lang: String,
        file: MultipartBody.Part?,
        id: RequestBody?
    ): MutableLiveData<UpdateDocumentsResponse> {
        val newsData = MutableLiveData<UpdateDocumentsResponse>()

        appApi.updateDocuments(auth, lang, file, id)
            .enqueue(object : Callback<UpdateDocumentsResponse> {
                override fun onResponse(
                    call: Call<UpdateDocumentsResponse>,
                    response: Response<UpdateDocumentsResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<UpdateDocumentsResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun selectActiveRoute(
        auth: String,
        lang: String,
        request: DriverSelectRoutesRequest
    ): MutableLiveData<SelectActiveRouteResponse> {
        val newsData = MutableLiveData<SelectActiveRouteResponse>()

        appApi.selectActiveRouteDriver(auth, lang, request)
            .enqueue(object : Callback<SelectActiveRouteResponse> {
                override fun onResponse(
                    call: Call<SelectActiveRouteResponse>,
                    response: Response<SelectActiveRouteResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<SelectActiveRouteResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun drivergetroutes(
        auth: String,
        lang: String,
        driverGetRoutesRequest: DriverGetRoutesRequest
    ): MutableLiveData<DriverGetRoutesResponse> {
        val newsData = MutableLiveData<DriverGetRoutesResponse>()

        appApi.getroutesDriver(auth, lang, driverGetRoutesRequest)
            .enqueue(object : Callback<DriverGetRoutesResponse> {
                override fun onResponse(
                    call: Call<DriverGetRoutesResponse>,
                    response: Response<DriverGetRoutesResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverGetRoutesResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun getvehicleType(auth: String, lang: String): MutableLiveData<GetVehicleTypeResponse> {
        val newsData = MutableLiveData<GetVehicleTypeResponse>()

        appApi.getvehiclTypes(auth, lang)
            .enqueue(object : Callback<GetVehicleTypeResponse> {
                override fun onResponse(
                    call: Call<GetVehicleTypeResponse>,
                    response: Response<GetVehicleTypeResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<GetVehicleTypeResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun driverupdateProfile(
        auth: String?,
        lang: String?,
        countryCode: RequestBody?,
        phone: RequestBody?,
        fullName: RequestBody?,
        password: RequestBody?,
        latitude: RequestBody?,
        longitude: RequestBody?,
        buildingNumber: RequestBody?,
        flatNumber: RequestBody?,
        deviceId: RequestBody?,
        deviceType: RequestBody?,
        profile: MultipartBody.Part?,
        vehicleTypeId: RequestBody?

    ): MutableLiveData<DriverProfileResponse> {

        val newsData = MutableLiveData<DriverProfileResponse>()

        appApi.updateDriverPofile(
            auth,
            lang,
            countryCode,
            phone,
            fullName,
            password,
            latitude,
            longitude,
            buildingNumber,
            flatNumber,
            deviceId,
            deviceType,
            profile,
            vehicleTypeId
        )
            .enqueue(object : Callback<DriverProfileResponse> {
                override fun onResponse(
                    call: Call<DriverProfileResponse>,
                    response: Response<DriverProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        Log.d("apiData ", "${response.body()}")
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverProfileResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException"))
                            WisamTaxiApplication.showToastMethod()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            })
        return newsData
    }

    fun getLogoutDriver(auth: String, lang: String): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()

        appApi.driverLogout(auth, lang)
            .enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }


    fun isAvailableDriver(
        auth: String,
        lang: String,
        isAvailableRequest: IsAvailableRequest
    ): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()

        appApi.driverisAvailable(auth, lang, isAvailableRequest)
            .enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }

    fun getDriverBooking(
        token: String,
        lang: String,
        map: Map<String, String>
    ): MutableLiveData<DriverGetBookingResponse> {
        val newsData = MutableLiveData<DriverGetBookingResponse>()

        appApi.getDriverBooking(token, lang, map)
            .enqueue(object : Callback<DriverGetBookingResponse> {
                override fun onResponse(
                    call: Call<DriverGetBookingResponse>,
                    response: Response<DriverGetBookingResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<DriverGetBookingResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        return newsData
    }

    fun getDriverroutelist(lang: String): MutableLiveData<RoutesListResponse> {
        val newsData = MutableLiveData<RoutesListResponse>()

        appApi.getdriverroutes(lang)
            .enqueue(object : Callback<RoutesListResponse> {
                override fun onResponse(
                    call: Call<RoutesListResponse>,
                    response: Response<RoutesListResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<RoutesListResponse>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        return newsData
    }

    fun getBookingDetailsDriver(
        token: String,
        lang: String,
        data: String
    ): MutableLiveData<GetBookingDetailsDriver> {
        val newsData = MutableLiveData<GetBookingDetailsDriver>()
        try {

            appApi.getBookingDetailsDriver(token, lang, data)
                .enqueue(object : Callback<GetBookingDetailsDriver?> {
                    override fun onResponse(
                        call: Call<GetBookingDetailsDriver?>,
                        response: Response<GetBookingDetailsDriver?>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("AppTag", " url();" + response.raw().request().url())
                            newsData.value = response.body()
                        } else {
                            Log.d("apiDataPro ", "${response}")
                        }
                    }

                    override fun onFailure(call: Call<GetBookingDetailsDriver?>, t: Throwable) {
                        Log.d("apiDataPro ", "${t}")
                        newsData.value = null
                        try {
                            if (t.toString().contains("SocketTimeoutException")) {
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return newsData
    }

    fun rateToCustomer(
        auth: String,
        lang: String,
        request: RateToUser
    ): MutableLiveData<LogoutResponse> {
        val newsData = MutableLiveData<LogoutResponse>()

        appApi.ratetoCustomer(auth, lang, request)
            .enqueue(object : Callback<LogoutResponse> {
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        return newsData
    }




    fun getFaqList(
        token: String,
        lang: String,
        map: Map<String, String>
    ): MutableLiveData<JsonElement> {
        val newsData = MutableLiveData<JsonElement>()
        appApi.getFaq(token, lang, map)
            .enqueue(object : Callback<JsonElement> {
                override fun onResponse(
                    call: Call<JsonElement>,
                    response: Response<JsonElement>
                ) {
                    if (response.isSuccessful) {
                        Log.d("AppTag", " url();" + response.raw().request().url())
                        newsData.value = response.body()
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Log.d("apiData ", "${t}")
                    newsData.value = null
                    try {
                        if (t.toString().contains("SocketTimeoutException")) {
                            WisamTaxiApplication.showToastMethod()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        return newsData
    }
}