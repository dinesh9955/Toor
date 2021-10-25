package com.wisam.taxi.interfaces

import com.google.gson.JsonElement
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
import retrofit2.http.*

interface ApiInterface {

    @Multipart
    @POST("/v1/user/register")
    fun postSignUp(
        @Header("language") language: String?,
        @Part("countryCode") countryCode: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("fullName") fullName: RequestBody?,
        @Part profile: MultipartBody.Part?,
        @Part("address") address: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("address1") address1: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("deviceId") deviceId: RequestBody?,
        @Part("deviceType") deviceType: RequestBody?
        ): Call<SignUpResponse>

    @POST("/v1/user/login")
    fun postLogin(@Header("language") language: String?,@Body request: LoginRequest): Call<LoginResponse>

    @POST("/v1/user/verifyOtp")
    fun verifyOTP( @Header("language") language: String?,@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("/v1/user/resendOtp")
    fun resendOtp( @Header("language") language: String?,@Body request: ResendOTPRequest): Call<ResendOtpResponse>

    @PUT("/v1/user/resetpassword")
    fun resetPassword(@Header("language") language: String?,@Body request: ResetPasswordRequest): Call<ResendOtpResponse>

    @GET("/v1/user/profile")
    fun getProfile(@Header("Authorization") token: String,@Header("language") language: String?): Call<GetProfileResponse>

    @PUT("/v1/user/changepassword")
    fun changePassword(@Header("Authorization") token: String,@Header("language") language: String?,@Body changePasswordRequest: ChangePasswordRequest): Call<GetProfileResponse>

    @POST("/v1/user/home")
    fun userHome(@Header("Authorization") token: String,@Header("language") language: String?,@Body userhomeRequest: UserhomeRequest): Call<UserHomeResponse>

    @POST("/v1/user/sociallogin")
    fun socialLogin(@Header("language") language: String?,@Body request: SocialLoginRequest): Call<SocialLoginResponse>

    @GET("/v1/user/bookings")
    fun getBooking(@Header("Authorization") token: String,@Header("language") language: String?, @QueryMap map: Map<String, String>): Call<UsersBookingResponse>

    @GET("/v1/user/booking/{id}")
    fun getBookingDetailsUser(@Header("Authorization") token: String,@Header("language") language: String?, @Path("id") id: String): Call<UserBookingDetailResponse>

    @Multipart
    @POST("/v1/user/profile")
    fun updatePofile(
        @Header("Authorization") auth: String?,
        @Header("language") language: String?,
        @Part("phone") phone: RequestBody?,
        @Part("fullName") fullName: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("countryCode") countryCode: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("deviceToken") deviceToken: RequestBody?,
        @Part("deviceType") deviceType: RequestBody?,
        @Part("address1") address1: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part profile: MultipartBody.Part?
    ): Call<UpdateProfileResponse>

    @GET("/v1/user/cancelBooking/{id}")
    fun userCancelBooking(@Header("Authorization") token: String, @Header("language") language: String?, @Path("id") id: String): Call<LogoutResponse>

    @POST("/v1/driver/rating")
    fun ratetoDriver(@Header("Authorization") token: String, @Header("language") language: String?, @Body rateToDriver: RateToDriver): Call<LogoutResponse>

    @GET("/v1/user/logout")
    fun customerLogout(@Header("Authorization") token: String,@Header("language") language: String?): Call<LogoutResponse>


    /*
    /
    /
    // Driver Module
    /
    /
     */

    @Multipart
    @POST("/v1/driver/register")
    fun signUpDriver(
        @Header("language") language: String?,
        @Part("countryCode") countryCode: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("fullName") fullName: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part profile: MultipartBody.Part?,
        @Part("buildingNumber") buildingNumber: RequestBody?,
        @Part("flatNumber") flatNumber: RequestBody?,
        @Part contractCopy: MultipartBody.Part?,
        @Part license: MultipartBody.Part?,
        @Part("deviceId") deviceId: RequestBody?,
        @Part("deviceType") deviceType: RequestBody?,
//        @Part("vehicleTypeId") vehicleTypeId: RequestBody?,
        @Part("vehicleName") vehicleName: RequestBody?,
        @Part("vehicleModel") vehicleModel: RequestBody?
    ): Call<DriverSignupResponse>

    @POST("/v1/driver/login")
    fun driverLogin(@Header("language") language: String?,@Body request: LoginRequest): Call<DriverLoginResponse>

    @POST("/v1/driver/verifyOtp")
    fun verifyOTPDriver(@Header("language") language: String?,@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("/v1/driver/resendOtp")
    fun resendOtpDriver(@Header("language") language: String?,@Body request: ResendOTPRequest): Call<ResendOtpResponse>

    @PUT("/v1/driver/resetpassword")
    fun resetPasswordDriver(@Header("language") language: String?,@Body request: ResetPasswordRequest): Call<ResendOtpResponse>

    @GET("/v1/driver")
    fun getProfileDriver(@Header("Authorization") token: String,@Header("language") language: String?): Call<DriverProfileResponse>

    @PUT("/v1/driver/changepassword")
    fun changePasswordDriver(@Header("Authorization") token: String,@Header("language") language: String?,@Body changePasswordRequest: ChangePasswordRequest): Call<GetProfileResponse>

    @GET("/v1/driver/document")
    fun getDocumentDriver(@Header("Authorization") token: String,@Header("language") language: String?): Call<GetDocumentsResponse>

    @Multipart
    @POST("/v1/driver/document")
    fun updateDocuments(
        @Header("Authorization") token: String,
        @Header("language") language: String?,
        @Part file: MultipartBody.Part?,
        @Part("id") id: RequestBody?
    ): Call<UpdateDocumentsResponse>

    @GET("/v1/driver/vehicletypes")
    fun getvehiclTypes(@Header("Authorization") token: String,@Header("language") language: String?): Call<GetVehicleTypeResponse>

    @POST("/v1/driver/home")
    fun getroutesDriver(@Header("Authorization") token: String,@Header("language") language: String?,@Body request: DriverGetRoutesRequest): Call<DriverGetRoutesResponse>

    @POST("/v1/driver/activeRoute")
    fun selectActiveRouteDriver(@Header("Authorization") token: String,@Header("language") language: String?,@Body request: DriverSelectRoutesRequest):
            Call<SelectActiveRouteResponse>

    @Multipart
    @POST("/v1/driver")
    fun updateDriverPofile(
        @Header("Authorization") auth: String?,
        @Header("language") language: String?,
        @Part("countryCode") countryCode: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("fullName") fullName: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("buildingNumber") buildingNumber: RequestBody?,
        @Part("flatNumber") flatNumber: RequestBody?,
        @Part("deviceId") deviceId: RequestBody?,
        @Part("deviceType") deviceType: RequestBody?,
        @Part profile: MultipartBody.Part?,
        @Part("vehicleTypeId") vehicleTypeId: RequestBody?
    ): Call<DriverProfileResponse>

    @GET("/v1/driver/logout")
    fun driverLogout(@Header("Authorization") token: String,@Header("language") language: String?): Call<LogoutResponse>

    @PUT("/v1/driver/isAvailable")
    fun driverisAvailable(@Header("Authorization") token: String,@Header("language") language: String?,@Body isAvailableRequest: IsAvailableRequest): Call<LogoutResponse>

    @GET("/v1/driver/bookings")
    fun getDriverBooking(@Header("Authorization") token: String,@Header("language") language: String?, @QueryMap map: Map<String, String>): Call<DriverGetBookingResponse>

    @GET("/v1/driver/routeList")
    fun getdriverroutes(@Header("language") language: String?): Call<RoutesListResponse>

    @GET("/v1/driver/cancelBooking/{id}")
    fun driverCancelBooking(@Header("Authorization") token: String, @Path("id") id: String): Call<LogoutResponse>

    @GET("/v1/driver/booking/{id}")
    fun getBookingDetailsDriver(@Header("Authorization") token: String,@Header("language") language: String?, @Path("id") id: String): Call<GetBookingDetailsDriver>

    @POST("/v1/user/rating")
    fun ratetoCustomer(@Header("Authorization") token: String ,@Header("language") language: String?, @Body rateToUser: RateToUser): Call<LogoutResponse>

    @GET("/v1/admin/pages")
    fun getFaq(@Header("Authorization") token: String,@Header("language") language: String?, @QueryMap map: Map<String, String>): Call<JsonElement>

}