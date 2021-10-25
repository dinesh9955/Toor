package com.wisam.taxi.view.welcome.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.facebook.*
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.hbb20.CountryCodePicker
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.login.DriverLoginResponse
import com.wisam.taxi.model.request.LoginRequest
import com.wisam.taxi.model.request.SocialLoginRequest
import com.wisam.taxi.model.response.login.LoginResponse
import com.wisam.taxi.model.response.socialLogin.SocialLoginResponse
import com.wisam.taxi.model.response.updateprofile.UpdateProfileResponse
import com.wisam.taxi.view.driverModule.routes.RoutesActivity
import com.wisam.taxi.view.forgotPassword.activity.ForgotPasswordActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.register.SignUpAsCustActivity
import com.wisam.taxi.view.register.SignUpAsDriverActivity
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.verification.activity.EditProfileOtpVerifyActivity
import com.wisam.taxi.view.verification.activity.OtpVerificationActivity
import com.ybs.countrypicker.CountryPicker
import com.ybs.countrypicker.CountryPickerListener
import kotlinx.android.synthetic.main.activity_welcomelogin.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.*

class WelcomeLoginActivity : BaseActivity() {
    //Google Login Request Code
    private val RC_SIGN_IN = 101
    private var deviceToken: String = ""
    private var mAuth: FirebaseAuth? = null
    private var callbackManager: CallbackManager? = null
    var facebookToken: String? = ""
    private var countrycode: String = "964"
    private lateinit var mAlertDialog: AlertDialog
    private var imageString: String? = ""
    private var imagefile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcomelogin)

        try {
            if (sharedPref.getString("type", "").equals("customer", true))
            {
                cvWelcomeGoogle.visibility = View.VISIBLE
                cvWelcomeGoogle.isClickable = true
                cvWelcomeFacebook.visibility = View.VISIBLE
                cvWelcomeFacebook.isClickable = true

                etWelcomeMobile.setText(sharedPref.getString("mobile_no_remem", "").toString())
                etPassword.setText(sharedPref.getString("password_remem", "").toString())
                edtWelcomeCountryCode.setCountryForPhoneCode(sharedPref.getString("country_code_remem", "964")!!.toInt())
                countrycode = sharedPref.getString("country_code", "964").toString()

            }
            else if (sharedPref.getString("type", "").equals("driver", true))
            {
                tvLoginAs.text = getString(R.string.login_as_a_driver)

                cvWelcomeGoogle.visibility = View.INVISIBLE
                cvWelcomeGoogle.isClickable = false
                cvWelcomeFacebook.visibility = View.INVISIBLE
                cvWelcomeFacebook.isClickable = false

                etWelcomeMobile.setText(sharedPref.getString("mobile_no_driver_remem", "").toString())
                etPassword.setText(sharedPref.getString("password_driver_remem", "").toString())
                edtWelcomeCountryCode.setCountryForPhoneCode(sharedPref.getString("country_code_driver_remem", "964")!!.toInt())
                countrycode = "" + sharedPref.getString("country_code_driver", "964")
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }

        etWelcomeMobile.isCursorVisible = true
        etWelcomeMobile.requestFocus()


        clCodePicker.setOnClickListener {
            picker = CountryPicker.newInstance("Select Country");
            picker.setListener { name, code, p2, p3 ->
                try {
                    edtWelcomeCountryCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                }catch (e:Exception){
                    e.printStackTrace()
                }
                picker.dismiss()
            }
            picker.show(supportFragmentManager, "COUNTRY_PICKER")
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.w("DeviceToken", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                deviceToken = task.result?.token!!
                Log.e("DeviceToken", "getInstanceId :- $deviceToken")

            })

        callbackManager = CallbackManager.Factory.create()
        mAuth = FirebaseAuth.getInstance()

        cvWelcomeGoogle.setOnClickListener {

            showprogressbar()
            editor.putString("logintype", "google").apply()
            signIn()
        }

        cvWelcomeFacebook.setOnClickListener {
            showprogressbar()
            editor.putString("logintype", "fb").apply()
            facebookSignIn()
        }


        ivWelcomeBack.setOnClickListener {
            navigateWithFinish(ChooseUserTypeActivity::class.java)
        }

        tvForgotPassword.setOnClickListener {
            navigate(ForgotPasswordActivity::class.java)
        }

        tvWelcomeLogin.setOnClickListener {

            if (sharedPref.getString("type", "customer").equals("driver", true)) {

                if (isValid()) {
                    callDriverLoginApi()
                }

            } else {
                if (isValid()) {
                    editor.putString("logintype", "simple").apply()
                    callLoginApi()
                }
            }
        }

        tvWelcomeImNewSignup.setOnClickListener {
            navigateToSignUp()
        }

        edtWelcomeCountryCode.setOnCountryChangeListener {
            Log.w(
                "code",
                "" + edtWelcomeCountryCode.selectedCountryCodeAsInt + " " + edtWelcomeCountryCode.selectedCountryCode
            )

            countrycode = "" + edtWelcomeCountryCode.selectedCountryCode
        }

    }

    private fun callLoginApi() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val forgot: MutableLiveData<LoginResponse>

        val loginRequest = LoginRequest(
            "+$countrycode",
            etWelcomeMobile.text.toString(),
            etPassword.text.toString(),
            deviceToken,
            "android"
        )

        forgot = repository.getLogin(sharedPref.getString("mylang", "en").toString(),loginRequest)
        forgot.observe(this, object : Observer<LoginResponse> {
            override fun onChanged(t: LoginResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)
                try {
                    when {
                        t?.response!!.success -> {
                            if (checkBox.isChecked) {
                                editor.putString("country_code_remem", countrycode)
                                editor.putString("mobile_no_remem", etWelcomeMobile.text.toString())
                                editor.putString("password_remem", etPassword.text.toString())
                            }
                            else
                            {
                                editor.putString("country_code_remem", "")
                                editor.putString("mobile_no_remem", "")
                                editor.putString("password_remem", "")
                            }
                            editor.putString("country_code", countrycode)
                            editor.putString("mobile_no", etWelcomeMobile.text.toString())
                            editor.putString("password", etPassword.text.toString())
                            editor.putString("logintype", "simple").apply()
                            editor.putString("auth", "SEC " + t.data.authToken)
                            editor.putString("name", "" + t.data.fullName)
                            editor.putString("address", "" + t.data.address)
                            editor.putString("address1", "" + t.data.address1)
                            editor.putString("email", "" + t.data.email)
                            editor.putString("profilePic", "" + t.data.profilePic)
                            editor.putString("id", "" + t.data.id)
                            editor.apply()
                            editor.commit()

                            dismissprogressBar()
                            navigateFinishAffinity(HomeActivity::class.java)
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()
                            if (t.response.message.equals("User is not verified yet.", true)) {
                               showToast(""+t.response.message)
                                val intent = Intent(this@WelcomeLoginActivity, EditProfileOtpVerifyActivity::class.java)
                                intent.putExtra("code", ""+countrycode)
                                intent.putExtra("phone", ""+ etWelcomeMobile.text.toString())
                                startActivityForResult(intent, 10)
                            }else{
                                showToast(""+t.response.message)
                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }

            }
        })
    }

    private fun callDriverLoginApi() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val forgot: MutableLiveData<DriverLoginResponse>

        val loginRequest = LoginRequest("+$countrycode", etWelcomeMobile.text.toString(), etPassword.text.toString(), deviceToken, "android")

        forgot = repository.driverLogin(sharedPref.getString("mylang", "en").toString(),loginRequest)
        forgot.observe(this, object : Observer<DriverLoginResponse> {
            override fun onChanged(t: DriverLoginResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {

                            if (checkBox.isChecked) {
                                editor.putString("country_code_driver_remem", countrycode)
                                editor.putString("mobile_no_driver_remem", etWelcomeMobile.text.toString())
                                editor.putString("password_driver_remem", etPassword.text.toString())

                            }
                            else
                            {
                                editor.putString("country_code_driver_remem", "")
                                editor.putString("mobile_no_driver_remem", "")
                                editor.putString("password_driver_remem", "")
                            }
                            editor.putString("country_code_driver", countrycode)
                            editor.putString("mobile_no_driver", etWelcomeMobile.text.toString())
                            editor.putString("password_driver", etPassword.text.toString())
                            editor.putString("logintype", "simple")
                            editor.putString("auth", "SED " + t.data.authToken)
                            editor.putString("name_driver", "" + t.data.fullName)
                            editor.putString("buildingname", "" + t.data.buildingNumber)
                            editor.putString("flatno", "" + t.data.flatNumber)
                            editor.putString("profilePic_driver", "" + t.data.profilePic)
                            editor.putString("id_driver", "" + t.data.id)
                            editor.putString("cartype_driver", "" + t.data.vehicleTypeId.name)

                            editor.apply()
                            editor.commit()

                            dismissprogressBar()
                            navigateFinishAffinity(RoutesActivity::class.java)
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()

                            if (t.response.message.equals("Driver is not verified yet", true))
                            {
                                showToast(""+t.response.message)
                                val intent = Intent(this@WelcomeLoginActivity, EditProfileOtpVerifyActivity::class.java)
                                intent.putExtra("code", ""+countrycode)
                                intent.putExtra("phone", ""+ etWelcomeMobile.text.toString())
                                startActivityForResult(intent, 10)
                            }
                            else
                                showToast(""+t.response.message)

                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }

            }
        })
    }

    private fun callSocialLoginApi(
        phone: String,
        fullname: String,
        emailId: String,
        countrycode: String,
        address: String,
        address1: String,
        facebookId: String,
        googleid: String
    ) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        var socialLogin: MutableLiveData<SocialLoginResponse>

        var socialloginRequest = SocialLoginRequest(
            phone, fullname, emailId, countrycode, address, address1, "12345678",
            deviceToken, "android", facebookId, googleid
        )

        Log.d("SocialRequest", "" + socialloginRequest.toString())

        socialLogin = repository.socialLogin(sharedPref.getString("mylang", "en").toString(),socialloginRequest)
        socialLogin.observe(this, object : Observer<SocialLoginResponse> {
            override fun onChanged(t: SocialLoginResponse?) {

                if (t != null)
                    Log.d("SocialRESPONSE", "" + t.toString())

                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()
                            editor.putString("auth", "SEC " + t.data.authToken).apply()

                            if (t.data.phone.isNullOrEmpty() || t.data.email.isNullOrEmpty()) {
                                showAlertDialog(t.data.email, t.data.fullName)
                            }
                            else {
                                editor.putString("country_code", countrycode)
                                editor.putString("mobile_no", t.data.phone)
                                editor.putString("password", "12345678")

                                editor.putString("name", "" + t.data.fullName)
                                editor.putString("address", "" + t.data.address)
                                editor.putString("address1", "" + t.data.address1)
                                editor.putString("email", "" + t.data.email)
                                editor.putString("profilePic", "" + t.data.profilePic)
                                editor.putString("id", "" + t.data.id)

                                editor.apply()
                                editor.commit()

                                if (t.data.isVerified) {
                                    navigateFinishAffinity(HomeActivity::class.java)
                                } else {
                                    val intent = Intent(this@WelcomeLoginActivity,
                                        OtpVerificationActivity::class.java
                                    )
                                    intent.putExtra("intentfrom", "signupcust")
                                    intent.putExtra("code", countrycode)
                                    intent.putExtra("phone", t.data.phone)
                                    startActivity(intent)
                                }
                            }
                        }
                        t.response.message.isNullOrEmpty() -> {
                            try {
                                if (sharedPref.getString("logintype", "").equals("google", true)) {
                                    mGoogleSignInClient.signOut()
                                        .addOnCompleteListener {
                                        }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            dismissprogressBar()
                        }
                        else -> {
                            Toast.makeText(context, t.response.message, Toast.LENGTH_SHORT).show()
                            try {
                                if (sharedPref.getString("logintype", "").equals("google", true)) {
                                    mGoogleSignInClient.signOut()
                                        .addOnCompleteListener {
                                        }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            dismissprogressBar()
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    if (sharedPref.getString("logintype", "").equals("google", true)) {
                        mGoogleSignInClient.signOut()
                            .addOnCompleteListener {
                            }
                    }
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }

            }
        })
    }

    private fun facebookSignIn() {

        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY)
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            Arrays.asList("email", "public_profile", "user_friends")
        )
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {

//                    Log.e("Facebook............", " id : " + result?.accessToken?.token)
                    facebookToken = result?.accessToken?.token
                    //added new
                    val graph: GraphRequest = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        object : GraphRequest.GraphJSONObjectCallback {
                            override fun onCompleted(
                                `object`: JSONObject?,
                                response: GraphResponse?
                            ) {
//                                Log.e("data of fb ", " all data facebook $`object` response  $response")
                                getFacebookData(`object`!!)
                            }
                        })

                    val bundle: Bundle = Bundle()
                    bundle.putString("fields", "id,first_name,last_name,email,gender")
                    graph.parameters = bundle
                    graph.executeAsync()

                }

                override fun onCancel() {
                    dismissprogressBar()
                }

                override fun onError(error: FacebookException?) {
                    dismissprogressBar()
                }
            })
    }

    private fun getFacebookData(jsonObject: JSONObject) {


        val id: String = jsonObject.getString("id")
        var pic: String = "https://graph.facebook.com/" + id + "/picture?type=large"

        val fullname: String = jsonObject.getString("first_name") + " " + jsonObject.getString("last_name")
        var email: String = ""
        if (jsonObject.has("email"))
            email = jsonObject.getString("email")

        dismissprogressBar()

        callSocialLoginApi(
            "",
            fullname,
            email,
            countrycode,
            "",
            "",
            id,
            ""
        )

    }

    //SignInMethod for Google
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignin(task)
        } else if (requestCode == 10 && resultCode == Activity.RESULT_OK)
        {
            // block for user not verified.
        }
        else {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleGoogleSignin(task: Task<GoogleSignInAccount>?) {
        try {
            dismissprogressBar()
            val account = task?.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            dismissprogressBar()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            when {
                task.isSuccessful -> {
                    callSocialLoginApi(
                        "",
                        account.displayName.toString(),
                        account.email.toString(),
                        countrycode,
                        "",
                        "",
                        "",
                        account.id.toString()
                    )
                }
                task.isCanceled -> { }
                else -> { }
            }
        }
    }

    private fun navigateToSignUp() {
        if (sharedPref.getString("type", "customer").equals("driver", true)) {
            navigate(SignUpAsDriverActivity::class.java)
        } else {
            navigate(SignUpAsCustActivity::class.java)
        }
    }

    private fun isValid(): Boolean {

        return when {
            etWelcomeMobile.text.toString().isEmpty() -> {
                showToast(resources.getString(R.string.pleaseentermobileNo))
                false
            }
            etWelcomeMobile.text.toString().trim().length < 5 -> {
                showToast(resources.getString(R.string.pleaseentervalidphone))
                false
            }
            etPassword.text.toString().length < 8 -> {
                showToast(resources.getString(R.string.entervalidpassword))
                false
            }
            else -> true
        }

    }

    private fun showAlertDialog(emailId: String, username: String)
    {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.sociallogin_alertlayout, null)

        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvLogin = mDialogView.findViewById<View>(R.id.tvSocialLogin) as TextView
        val edtsocialcode = mDialogView.findViewById<View>(R.id.edtSocialCountryCode) as CountryCodePicker
        val edtsocialMobile = mDialogView.findViewById<View>(R.id.etSocialMobile) as EditText
        val edtsocialEmail = mDialogView.findViewById<View>(R.id.etSocialEmail) as EditText

        edtsocialEmail.setText(emailId)

        edtsocialcode.setOnCountryChangeListener {
            countrycode = "" + edtsocialcode.selectedCountryCode
        }

        tvLogin.setOnClickListener {
            callUpdateProfile(
                edtsocialMobile.text.toString(),
                edtsocialEmail.text.toString(),
                username
            )
        }
    }

    private fun callUpdateProfile(phoneNo: String, emailID: String, fullname: String) {

        mAlertDialog.dismiss()
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val updateprofile: MutableLiveData<UpdateProfileResponse>

        val code = requestbodyString("+$countrycode")
        val phone = requestbodyString(phoneNo)
        val email = requestbodyString(emailID)
        val fullName = requestbodyString(fullname)
        val deviceToken = requestbodyString(deviceToken)
        val deviceType = requestbodyString("android")
        val address = requestbodyString("")
        val password = requestbodyString("")
        val address1 = requestbodyString("")
        val latitude = requestbodyString("20.23")
        val longitude = requestbodyString("23.25")

        val body: MultipartBody.Part

        body = if (imagefile != null) {
            prepareFilePart("profile", Uri.parse(imageString))
        } else {
            val reqFile: RequestBody =
                RequestBody.create(MediaType.parse("image/*"), "")
            MultipartBody.Part.createFormData("profile", "", reqFile)
        }

        updateprofile = repository.updateProfile(
            sharedPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(),
            phone,
            fullName,
            email,
            code,
            address,
            password,
            deviceToken,
            deviceType,
            address1,
            latitude,
            longitude,
            body
        )
        updateprofile.observe(this, object : Observer<UpdateProfileResponse> {
            override fun onChanged(t: UpdateProfileResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            editor.putString("country_code", t.data.phone.substringBefore("+"))
                            editor.putString("mobile_no", t.data.phone)
                            editor.putString("password", "12345678")

                            editor.putString("auth", "SEC " + t.data.authToken)
                            editor.putString("name", "" + t.data.fullName)
                            editor.putString("address", "" + t.data.address)
                            editor.putString("address1", "" + t.data.address1)
                            editor.putString("email", "" + t.data.email)
                            editor.putString("profilePic", "" + t.data.profilePic)
                            editor.putString("id", "" + t.data.id)

                            editor.apply()
                            editor.commit()

                            if (t.data.isVerified) {
                                navigateFinishAffinity(HomeActivity::class.java)
                            } else {
                                val intent = Intent(
                                    this@WelcomeLoginActivity,
                                    OtpVerificationActivity::class.java
                                )
                                intent.putExtra("intentfrom", "signupcust")
                                intent.putExtra("code", countrycode)
                                intent.putExtra("phone", t.data.phone)
                                startActivity(intent)
                            }

                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                            try {
                                if (sharedPref.getString("logintype", "").equals("google", true)) {
                                    mGoogleSignInClient.signOut()
                                        .addOnCompleteListener {
                                        }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        else -> {
                            dismissprogressBar()
                            try {
                                if (sharedPref.getString("logintype", "").equals("google", true)) {
                                    mGoogleSignInClient.signOut()
                                        .addOnCompleteListener {
                                        }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (t.response.message.equals("Authorization not correct", true)) {
                                showToast(resources.getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else if (t.response.logout == 1){
                                showToast(resources.getString(R.string.sessionexpired))
                               navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    if (sharedPref.getString("logintype", "").equals("google", true)) {
                        mGoogleSignInClient.signOut()
                            .addOnCompleteListener {
                            }
                    }
                    e.printStackTrace()
                }

            }
        })
    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), imagefile)
        return MultipartBody.Part.createFormData(partName, imagefile!!.name.trim(), requestFile)
    }

    override fun onBackPressed() {
        navigateWithFinish(ChooseUserTypeActivity::class.java)
    }
}
