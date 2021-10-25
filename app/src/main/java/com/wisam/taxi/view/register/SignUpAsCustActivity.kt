package com.wisam.taxi.view.register

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
//import com.klinker.android.link_builder.Link
//import com.klinker.android.link_builder.LinkBuilder
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.response.register.SignUpResponse
import com.wisam.taxi.view.verification.activity.OtpVerificationActivity
import com.ybs.countrypicker.CountryPicker
import kotlinx.android.synthetic.main.activity_signupascustomer.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream


class SignUpAsCustActivity : BaseActivity()
{
    var countrycode: String = "964"
    private var imageString: String? = ""
    private var imagefile: File? = null
    private var deviceToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signupascustomer)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.w("DeviceToken", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                deviceToken = task.result?.token!!
                Log.e("DeviceToken", "getInstanceId :- $deviceToken")

            })

        etSignupFullName.isCursorVisible = true
        etSignupFullName.requestFocus()

        generateString()

        ivSignUpBack.setOnClickListener {
            finish()
        }

        tvSignUpAlreadyLogin.setOnClickListener {
            finish()
        }

        ivSignUpProfilePic.setOnClickListener {
            showPictureDialog()
        }


        clCodePicker.setOnClickListener {
            picker = CountryPicker.newInstance("Select Country");
            picker.setListener { name, code, p2, p3 ->
                try {
                    edtSignupCountryCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                    countrycode = ""+edtSignupCountryCode.selectedCountryCodeAsInt.toString()
                }catch (e: Exception){
                    e.printStackTrace()
                }
                picker.dismiss()
            }
            picker.show(supportFragmentManager, "COUNTRY_PICKER");
        }

        tvSignUpbtn.setOnClickListener {
            if (isvalid())
            {
                if (ivcheckedBox.isChecked){
                    callSignupApi()
                }
                else{
                    showToast("Please accept our Terms of Services and Privacy Policy.")
                }

            }
        }
    }

    private fun callSignupApi() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val signup: MutableLiveData<SignUpResponse>

        val code = requestbodyString("+$countrycode")
        val phone = requestbodyString(etSignupMobile.text.toString())
        val email = requestbodyString(etSignupEmail.text.toString())
        val fullname = requestbodyString(etSignupFullName.text.toString())

        val body: MultipartBody.Part = prepareFilePart("profile", Uri.parse(imageString))

        val address = requestbodyString(etSignupAddress.text.toString())
        val password = requestbodyString(etSignupPassword.text.toString())
        val address1 = requestbodyString(etSignupAddress2.text.toString())
        val latitude = requestbodyString("" + sharedPref.getString("lat", "20.265"))
        val longitude = requestbodyString("" + sharedPref.getString("long", "15.233"))
        val deviceId = requestbodyString(deviceToken)
        val deviceType = requestbodyString("android")

        signup = repository.getSignUp(
            sharedPref.getString("mylang", "en").toString(),
            code,
            phone,
            email,
            fullname,
            body,
            address,
            password,
            address1,
            latitude,
            longitude, deviceId, deviceType
        )


        signup.observe(this, object : Observer<SignUpResponse> {
            override fun onChanged(t: SignUpResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            editor.putString("auth", "SEC " + t.data.authToken)
                            editor.putString("country_code", countrycode)
                            editor.putString("mobile_no", etSignupMobile.text.toString())
                            editor.putString("password", etSignupPassword.text.toString())
                            editor.apply()
                            editor.commit()

                            val intent = Intent(
                                this@SignUpAsCustActivity,
                                OtpVerificationActivity::class.java
                            )
                            intent.putExtra("intentfrom", "signupcust")
                            intent.putExtra("code", countrycode)
                            intent.putExtra("phone", etSignupMobile.text.toString())
                            startActivity(intent)
                            finishAffinity()
                        }
                        t.response.message.isNullOrEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            Toast.makeText(context, t.response.message, Toast.LENGTH_SHORT).show()
                            dismissprogressBar()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionCodes.CAMERAPERMISSIONCODE)
        {
            try
            {
                val thumbnail: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val input: InputStream? = contentResolver?.openInputStream(imageUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ivSignUpProfilePic.setImageBitmap(getRotatedImage(bitmap, input!!))

                    imageString = saveImage(getRotatedImage(bitmap, input))
                    imagefile = File(imageString)
                }
                else
                {
                    ivSignUpProfilePic.setImageBitmap(thumbnail)

                    imageString = saveImage(thumbnail)
                    imagefile = File(imageString)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error in upload image..please try again.")
            }
        } else {
            if (data?.data != null) {
                ivSignUpProfilePic.setImageURI(data.data)
                imageString = data.data.toString()
                imagefile = File(getRealPathFromURI(data?.data))
            }
        }
    }

    private fun isvalid(): Boolean {

        if (imagefile == null) {
            showToast(resources.getString(R.string.pleaseuploadprofilepic))
            return false
        } else if (countrycode.isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseentercountrycode))
            return false
        } else if (etSignupFullName.text.toString().trim().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseenterfullname))
            return false
        } else if (etSignupMobile.text.toString().trim().isNullOrEmpty()) {
            showToast(resources.getString(R.string.pleaseentermobileNo))
            return false
        }
        else if (etSignupMobile.text.toString().trim().length < 5) {
            showToast(getString(R.string.pleaseentervalidphone))
            return false
        }
        else if (etSignupEmail.text.toString().trim().isEmpty() || !etSignupEmail.text.toString().isEmailValid()) {
            showToast(resources.getString(R.string.pleaseentervalidemail))
            return false
        } else if (etSignupPassword.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenterpassword))
            return false
        }else if (etSignupPassword.text.toString().trim().length < 8) {
            showToast(resources.getString(R.string.passwordlength))
            return false
        } else if (etSignupConfPassword.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenterconfirmpassword))
            return false
        } else if (etSignupConfPassword.text.toString().trim().length < 8) {
            showToast(resources.getString(R.string.confirmpasswordlength))
            return false
        } else if (!etSignupPassword.text.toString().trim().equals(
                etSignupConfPassword.text.toString(),
                false
            ))
        {
            showToast(resources.getString(R.string.passdonotmatch))
            return false
        }
        else if (etSignupAddress.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenteraddress))
            return false
        } else if (etSignupAddress2.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenteraddress))
            return false
        }

        return true
    }

    fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        var requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), imagefile)
        return MultipartBody.Part.createFormData(partName, imagefile!!.name.trim(), requestFile)
    }

    private fun generateString() {
        val builder01 = SpannableStringBuilder()
        val spanned1 = SpannableString(resources.getString(R.string.iaccept) + " ")
        val spanned2 = SpannableString(resources.getString(R.string.termsservice))
        val spanned3 = SpannableString(" " + resources.getString(R.string.and) + " ")
        val spanned4 = SpannableString(resources.getString(R.string.privacypolicy))
        val spanned5 = SpannableString(" " + resources.getString(R.string.ofbasmaya))
        spanned2.setSpan(
            ForegroundColorSpan(Color.parseColor("#ff7272")),
            0,
            spanned2.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanned4.setSpan(
            ForegroundColorSpan(Color.parseColor("#ff7272")),
            0,
            spanned4.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvSignupIAccept.text =
            builder01.append(spanned1).append(spanned2).append(spanned3).append(spanned4)
                .append(spanned5)

//        LinkBuilder.on(tvSignupIAccept)
//            .addLinks(getExampleLinks())
//            .build();

    }


//    private fun getExampleLinks(): List<Link>? {
//        val links: MutableList<Link> = ArrayList()
//        val linkTerms: Link = Link("Terms of Services")
//            .setTextColor(Color.parseColor("#000000")) // optional, defaults to holo blue
//            .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C")) // optional, defaults to holo blue
//            //                .setHighlightAlpha(.4f)                                     // optional, defaults to .15f
//            .setUnderlined(false) // optional, defaults to true
//            .setBold(true) // optional, defaults to false
//            .setOnClickListener {
//                finish()
//            }
////            .setOnClickListener(object : View.OnClickListener() {
////                fun onClick(clickedText: String?) {
////                    val intent = Intent(this@Register, WebActivity::class.java)
////                    intent.putExtra("key", "Terms of Service")
////                    intent.putExtra("url", WSContants.TERMS)
////                    startActivity(intent)
////                    Log.d(TAG, "Terms of Service")
////                }
////            })
//        links.add(linkTerms)
//        val linkPolicy: Link = Link("Privacy Policy")
//            .setTextColor(Color.parseColor("#000000")) // optional, defaults to holo blue
//            .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C")) // optional, defaults to holo blue
//            //                .setHighlightAlpha(.4f)                                     // optional, defaults to .15f
//            .setUnderlined(false) // optional, defaults to true
//            .setBold(true) // optional, defaults to false
////            .setOnClickListener(object : View.OnClickListener() {
////                fun onClick(clickedText: String?) {
////                    val intent = Intent(this@Register, WebActivity::class.java)
////                    intent.putExtra("key", "Privacy Policy")
////                    intent.putExtra("url", WSContants.PRIVACY)
////                    startActivity(intent)
////                    Log.d(TAG, "Privacy Policy")
////                }
////            })
//        links.add(linkPolicy)
//        return links
//    }
//
//
}
