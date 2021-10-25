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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getvehicletype.Data
import com.wisam.taxi.model.driverResponse.getvehicletype.GetVehicleTypeResponse
import com.wisam.taxi.model.driverResponse.register.DriverSignupResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.verification.activity.OtpVerificationActivity
import com.ybs.countrypicker.CountryPicker
import kotlinx.android.synthetic.main.activity_signupascustomer.*
import kotlinx.android.synthetic.main.activity_signupasdriver.*
import kotlinx.android.synthetic.main.activity_signupasdriver.clCodePicker
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

@Suppress("DEPRECATION")
class SignUpAsDriverActivity : BaseActivity() {

    private var isProfile: Boolean = false
    private var isUploadContract: Boolean = false
    private var isUploadLicense: Boolean = false
    lateinit var carArrayList: ArrayList<Data>
    lateinit var carNamesList: ArrayList<String>

    var countrycode: String = "964"
    var carType: String = ""

    private var profilepicString: String? = ""
    private var profilepicfile: File? = null

    private var contractpicString: String? = ""
    private var contractpicfile: File? = null

    private var licensepicString: String? = ""
    private var licensepicfile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signupasdriver)

        etSignupDriverFullName.isCursorVisible = true
        etSignupDriverFullName.requestFocus()

        generateString()

        carArrayList = ArrayList()
        carNamesList = ArrayList()

//        getVehicleType()

//        spinnerDriverKindCar?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(p0: AdapterView<*>?) {}
//            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                carType = carArrayList[position].id
//                spinnerDriverKindCar?.setSelection(position)
//            }
//        }

        ivSignUpDriverBack.setOnClickListener {
            finish()
        }

        tvDriverSignUpAlreadyLogin.setOnClickListener {
            finish()
        }

        ivSignUpDriverProfile.setOnClickListener {
            makeProfileTrue()
            showPictureDialog()
        }

        ivDriverUploadContractLarge.setOnClickListener {
            makeContractTrue()
            showPictureDialog()
        }

        ivDriverUploadContractSmall.setOnClickListener {
            makeContractTrue()
            showPictureDialog()
        }

        ivDriverUploadLicenseLarge.setOnClickListener {
            makeLicenseTrue()
            showPictureDialog()
        }

        ivDriverUploadLicenseSmall.setOnClickListener {
            makeLicenseTrue()
            showPictureDialog()
        }

        edtSignupDriverCountryCode.setOnCountryChangeListener {
            countrycode = "" + edtSignupDriverCountryCode.selectedCountryCode
        }

        clCodePicker.setOnClickListener {
            picker = CountryPicker.newInstance("Select Country")
            picker.setListener { name, code, p2, p3 ->
                try {
                    edtSignupDriverCountryCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                    countrycode = ""+edtSignupDriverCountryCode.selectedCountryCodeAsInt.toString()
                }catch (e:Exception){
                    e.printStackTrace()
                }
                picker.dismiss()
            }
            picker.show(supportFragmentManager, "COUNTRY_PICKER");
        }


        tvDriverSignUpDriverIAccept.setOnClickListener {
            ivcheckedBoxDriver.isChecked = !ivcheckedBoxDriver.isChecked
        }

        tvDriverSignUp.setOnClickListener {

            if (isvalid())
            {
                if (ivcheckedBoxDriver.isChecked)
                    callSignupApi()
                else
                    showToast(getString(R.string.pleaseaccepttermandcondition))
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionCodes.CAMERAPERMISSIONCODE) {
            try {
                val thumbnail: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val input: InputStream? = contentResolver?.openInputStream(imageUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when {
                        isProfile -> {
                            ivSignUpDriverProfile.setImageBitmap(getRotatedImage(bitmap, input!!))

                            profilepicString = saveImage(getRotatedImage(bitmap, input))
                            profilepicfile = File(profilepicString)
                        }
                        isUploadContract -> {
                            ivDriverUploadContractSmall.visibility = View.INVISIBLE
                            Glide.with(applicationContext).load(getRotatedImage(bitmap, input!!)).thumbnail(0.1f).into(ivDriverUploadContractLarge)

                            contractpicString = saveImage(getRotatedImage(bitmap, input))
                            contractpicfile = File(contractpicString)
                        }
                        else -> {
                            ivDriverUploadLicenseSmall.visibility = View.INVISIBLE
                            Glide.with(applicationContext).load(getRotatedImage(bitmap, input!!)).thumbnail(0.1f).into(ivDriverUploadLicenseLarge)

                            licensepicString = saveImage(getRotatedImage(bitmap, input))
                            licensepicfile = File(licensepicString)
                        }
                    }
                } else {
                    when {
                        isProfile -> {
                            ivSignUpDriverProfile.setImageBitmap(thumbnail)

                            profilepicString = saveImage(thumbnail)
                            profilepicfile = File(profilepicString)
                        }
                        isUploadContract -> {
                            ivDriverUploadContractSmall.visibility = View.INVISIBLE
                            Glide.with(applicationContext).load(thumbnail).thumbnail(0.1f).into(ivDriverUploadContractLarge)

                            contractpicString = saveImage(thumbnail)
                            contractpicfile = File(contractpicString)
                        }
                        else -> {
                            ivDriverUploadLicenseSmall.visibility = View.INVISIBLE
                            Glide.with(applicationContext).load(thumbnail).thumbnail(0.1f).into(ivDriverUploadLicenseLarge)

                            licensepicString = saveImage(thumbnail)
                            licensepicfile = File(licensepicString)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else {

            when {
                isProfile -> {
                    ivSignUpDriverProfile.setImageURI(data?.data)

                    profilepicString = data?.data.toString()
                    profilepicfile = File(getRealPathFromURI(data?.data))
                }
                isUploadContract -> {
                    ivDriverUploadContractSmall.visibility = View.INVISIBLE
                    Glide.with(applicationContext).load(data?.data).thumbnail(0.1f).into(ivDriverUploadContractLarge)

                    contractpicString = data?.data.toString()
                    contractpicfile = File(getRealPathFromURI(data?.data))
                }
                else -> {
                    ivDriverUploadLicenseSmall.visibility = View.INVISIBLE
                    Glide.with(applicationContext).load(data?.data).thumbnail(0.1f).into(ivDriverUploadLicenseLarge)

                    licensepicString = data?.data.toString()
                    licensepicfile = File(getRealPathFromURI(data?.data))
                }
            }
        }
    }

    private fun callSignupApi() {
        showprogressbar()
        var deviceToken:String= ""

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                deviceToken = task.result?.token!!
            })

        val repository: RetroRepository = RetroRepository.instance
        val signup: MutableLiveData<DriverSignupResponse>

        val code = requestbodyString("+$countrycode")
        val phone = requestbodyString(etSignupDriverMobile.text.toString())
        val vehicleName = requestbodyString(clspinnerDriverKindCar.text.toString())
        val vehicleModel = requestbodyString(edtVehicleModel.text.toString())
        val fullname = requestbodyString(etSignupDriverFullName.text.toString())

        val profile  = prepareFilePart("profile", Uri.parse(profilepicString), profilepicfile)
        val contractCopy= prepareFilePart("contractCopy", Uri.parse(contractpicString), contractpicfile)
        val license= prepareFilePart("license", Uri.parse(licensepicString), licensepicfile)

        val buildingNumber = requestbodyString(etSignupDriverBuilding.text.toString())
        val flatNumber = requestbodyString(etSignupFlatNo.text.toString())
        val password = requestbodyString(etSignupDriverPassword.text.toString())
        val latitude = requestbodyString(""+sharedPref.getString("lat","20.265"))
        val longitude = requestbodyString(""+sharedPref.getString("long","15.233"))
        val deviceId = requestbodyString(deviceToken)
        val deviceType = requestbodyString("android")

        signup = repository.driverSignUp(
            sharedPref.getString("mylang", "en").toString(),
            code,
            phone,
            fullname,
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

        signup.observe(this, object : Observer<DriverSignupResponse> {
            override fun onChanged(t: DriverSignupResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {
                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            editor.putString("country_code_driver", countrycode)
                            editor.putString("mobile_no_driver", etSignupDriverMobile.text.toString())
                            editor.putString("password_driver", etSignupDriverPassword.text.toString())
                            editor.apply()
                            editor.commit()

                            val intent = Intent(this@SignUpAsDriverActivity, OtpVerificationActivity::class.java)
                            intent.putExtra("intentfrom", "signupdriver")
                            intent.putExtra("code", countrycode)
                            intent.putExtra("phone", etSignupDriverMobile.text.toString())
                            startActivity(intent)
                            finishAffinity()
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            showToast(t.response.message)
                            dismissprogressBar()
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                }
            }
        })
    }

    private fun getVehicleType()
    {
        val repository : RetroRepository = RetroRepository.instance
        val drivergetRoutes : MutableLiveData<GetVehicleTypeResponse>

        drivergetRoutes = repository.getvehicleType("",sharedPref.getString("mylang", "en").toString())
        drivergetRoutes.observe(this, object : Observer<GetVehicleTypeResponse> {
            override fun onChanged(t: GetVehicleTypeResponse?)
            {

                try {

                    when {
                        t?.response!!.success -> {

                            carArrayList.addAll(t.data)

                            for (i in 0 until carArrayList.size)
                            {
                                carNamesList.add(carArrayList[i].name)
                            }

                            val adapter = ArrayAdapter(this@SignUpAsDriverActivity, R.layout.signup_spinner_design, carNamesList)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                            spinnerDriverKindCar.adapter = adapter

                        }
                        t.response.message.isEmpty() -> { }

                        else -> {

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    showToast(resources.getString(R.string.sessionexpired))
                                }
                                t.response.logout == 1 -> {
                                    showToast(resources.getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> showToast(t.response.message)
                            }

                        }
                    }
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun makeLicenseTrue() {
        isProfile = false
        isUploadContract = false
        isUploadLicense = true
    }

    private fun makeContractTrue() {
        isProfile = false
        isUploadContract = true
        isUploadLicense = false
    }

    private fun makeProfileTrue() {
        isProfile = true
        isUploadContract = false
        isUploadLicense = false
    }

    private fun generateString() {
        val builder01 = SpannableStringBuilder()
        val spanned1 = SpannableString(resources.getString(R.string.iaccept)+" ")
        val spanned2 = SpannableString(resources.getString(R.string.termsservice))
        val spanned3 = SpannableString(" "+resources.getString(R.string.and)+" ")
        val spanned4 = SpannableString(resources.getString(R.string.privacypolicy))
        val spanned5 = SpannableString(" "+resources.getString(R.string.ofbasmaya))
        spanned2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7272")), 0, spanned2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned4.setSpan(ForegroundColorSpan(Color.parseColor("#ff7272")), 0, spanned4.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvDriverSignUpDriverIAccept.text = builder01.append(spanned1).append(spanned2).append(spanned3).append(spanned4).append(spanned5)

    }

    private fun prepareFilePart(
        partName: String,
        fileUri: Uri,
        filename: File?
    ): MultipartBody.Part {

        val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), filename)
        return MultipartBody.Part.createFormData(partName, filename!!.name.trim(), requestFile)
    }

    private fun isvalid(): Boolean {

        if (profilepicfile == null) {
            showToast(resources.getString(R.string.pleaseuploadprofilepic))
            return false
        } else if (etSignupDriverFullName.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenterfullname))
            return false
        } else if (countrycode.isEmpty()) {
            showToast(getString(R.string.pleaseselectcountrycode))
            return false
        } else if (etSignupDriverMobile.text.toString().trim().isEmpty())
        {
            showToast(resources.getString(R.string.pleaseentermobileNo))
            return false
        } else if (etSignupDriverMobile.text.toString().trim().length < 5)
        {
            showToast(resources.getString(R.string.pleaseentervalidphone))
            return false
        }
        else if (clspinnerDriverKindCar.text.toString().trim().isEmpty())
        {
            showToast(resources.getString(R.string.pleaseselctcartype))
            return false
        }
        else if (edtVehicleModel.text.toString().trim().isEmpty())
        {
            showToast(resources.getString(R.string.pleaseentervehiclemodel))
            return false
        } else if (etSignupDriverBuilding.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.enterbuildingname))
            return false
        } else if (etSignupFlatNo.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.enterflatno))
            return false
        } else if (etSignupDriverPassword.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenterpassword))
            return false
        } else if (etSignupDriverPassword.text.toString().trim().length < 8) {
            showToast(resources.getString(R.string.passwordlength))
            return false
        } else if (etSignupDriverConfPassword.text.toString().trim().isEmpty()) {
            showToast(resources.getString(R.string.pleaseenterconfirmpassword))
            return false
        } else if (etSignupDriverConfPassword.text.toString().trim().length < 8) {
            showToast(resources.getString(R.string.confirmpasswordlength))
            return false
        } else if (!etSignupDriverPassword.text.toString().trim().equals(etSignupDriverConfPassword.text.toString().trim(), false))
        {
            showToast(resources.getString(R.string.passdonotmatch))
            return false
        } else if (contractpicfile == null) {
            showToast(resources.getString(R.string.uploadcontractcopy))
            return false
        } else if (licensepicfile == null) {
            showToast(resources.getString(R.string.uploadlicensedoc))
            return false
        }
        return true
    }
}
