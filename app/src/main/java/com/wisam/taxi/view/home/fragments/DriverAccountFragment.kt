package com.wisam.taxi.view.home.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getvehicletype.Data
import com.wisam.taxi.model.driverResponse.getvehicletype.GetVehicleTypeResponse
import com.wisam.taxi.model.driverResponse.profile.DriverProfileResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.verification.activity.EditProfileOtpVerifyActivity
import com.ybs.countrypicker.CountryPicker
import kotlinx.android.synthetic.main.fragment_driveraccount.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern

@Suppress("DEPRECATION")
class DriverAccountFragment : Fragment()
{
    var countrycode: String = "964"
    var carType: String = ""
    var isPossible :Boolean =false
    private var deviceToken: String = ""
    private var tempPhone: String = ""
    private var counter: Int = 0

    private var imageString: String? = ""
    private var imagefile: File? = null

    lateinit var carArrayList:ArrayList<Data>
    lateinit var carNameList:ArrayList<String>

    lateinit var imageUri: Uri
    internal lateinit var tvchoosemediaCamera: TextView
    internal lateinit var ivchoosemediaCamera: ImageView
    internal lateinit var tvchoosemediaGallery: TextView
    internal lateinit var ivchoosemediaGallery: ImageView
    internal lateinit var tvchoosemediaCancel: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driveraccount, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {

        edtDriAccountName.isEnabled = false
        edtDriAccountBuilding.isEnabled = false
        edtDriAccountFlat.isEnabled = false

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.w("DeviceToken", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                deviceToken = task.result?.token!!
                Log.e("DeviceToken", "getInstanceId :- $deviceToken")

            })

        loadUserData()

        callgetProfile()

        carArrayList = ArrayList()
        carNameList = ArrayList()

        getVehicleType()

        spinnerDriAccountCarType?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(
                p0: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                carType = carArrayList[position].id
                spinnerDriAccountCarType?.setSelection(position)
            }
        }

        ivDriAccountEdit.setOnClickListener {
            onEditClick()
        }

        ivDriAccountsave.setOnClickListener {
           if (isvalid())
           {
               onSaveClick()
               tempPhone = ""+edtDriAccountMobile.text.toString()
               callUpdateProfile()
           }
        }

//        edtDriAccountCountryCode.setOnCountryChangeListener {
//            Log.w(
//                "code",
//                "" + edtDriAccountCountryCode.selectedCountryCodeAsInt + " " + edtDriAccountCountryCode.selectedCountryCode
//            )
//
//            countrycode = ""+edtDriAccountCountryCode.selectedCountryCode
//        }

        clCodePicker.setOnClickListener {
            (activity as HomeActivity).picker = CountryPicker.newInstance("Select Country");
            (activity as HomeActivity).picker.setListener { name, code, p2, p3 ->
                try {
                    edtDriAccountCountryCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                    countrycode = ""+edtDriAccountCountryCode.selectedCountryCode
                }catch (e:Exception){
                    e.printStackTrace()
                }
                (activity as HomeActivity).picker.dismiss()
            }
            (activity as HomeActivity).picker.show(activity!!.supportFragmentManager, "COUNTRY_PICKER");
        }

        circleIvProfile.setOnClickListener {
            if (isPossible)
            {
                showPictureDialog()
            }
        }
    }

    private fun loadUserData() {

        tvDriverProfileName.text = "" + WisamTaxiApplication.shrdPref.getString("name_driver", "")
        tvDriAccountCarTypeShow.text = "" + WisamTaxiApplication.shrdPref.getString("cartype_driver", "")
        tvDriverprofileDetail.text = "" + WisamTaxiApplication.shrdPref.getString("buildingname", "") + " "+
                WisamTaxiApplication.shrdPref.getString("flatno", "")

        edtDriAccountName.setText("" + WisamTaxiApplication.shrdPref.getString("name_driver", ""))

        tvDriAccountMobileShow.text = "+" + WisamTaxiApplication.shrdPref.getString("country_code_driver", "") +" "+
                WisamTaxiApplication.shrdPref.getString("mobile_no_driver", "")

        edtDriAccountMobile.setText(""+ WisamTaxiApplication.shrdPref.getString("mobile_no_driver", ""))

        edtDriAccountBuilding.setText("" + WisamTaxiApplication.shrdPref.getString("buildingname", ""))
        edtDriAccountFlat.setText("" + WisamTaxiApplication.shrdPref.getString("flatno", ""))

        edtDriAccountCountryCode.setCountryForPhoneCode(WisamTaxiApplication.shrdPref.getString("country_code_driver", "")!!.toInt())

        countrycode = ""+WisamTaxiApplication.shrdPref.getString("country_code_driver", "")

        val profilePic =""+WisamTaxiApplication.shrdPref.getString("profilePic_driver", "")

        if (profilePic.isNullOrEmpty())
        {
            circleIvProfile.setImageResource(R.drawable.group_445)
        }else
        {
            Picasso.get()
                .load(WisamTaxiApplication.BASE_URL+""+profilePic)
                .placeholder(R.drawable.group_445)
                .into(circleIvProfile)
        }

    }

    fun onEditClick()
    {

        isPossible = true
        ivDriAccountsave.visibility = View.VISIBLE
        ivDriAccountEdit.visibility = View.INVISIBLE

        edtDriAccountName.isEnabled = true
        edtDriAccountBuilding.isEnabled = true
        edtDriAccountFlat.isEnabled = true

        tvDriAccountMobileShow.visibility = View.INVISIBLE
        edtDriAccountMobile.visibility = View.VISIBLE
        edtDriAccountCountryCode.visibility = View.VISIBLE

        tvDriAccountCarTypeShow.visibility = View.INVISIBLE
        spinnerDriAccountCarType.visibility = View.VISIBLE
        ivSignupDriverArrow.visibility = View.VISIBLE

        edtDriAccountName.isCursorVisible = true
        edtDriAccountName.requestFocus()
        edtDriAccountName.setSelection(edtDriAccountName.text.length)
    }

    private fun onSaveClick()
    {
        isPossible = false
        hideKeyboard()
        ivDriAccountsave.visibility = View.INVISIBLE
        ivDriAccountEdit.visibility = View.VISIBLE

        edtDriAccountName.isEnabled = false
        edtDriAccountBuilding.isEnabled = false
        edtDriAccountFlat.isEnabled = false

        tvDriAccountMobileShow.visibility = View.VISIBLE
        edtDriAccountMobile.visibility = View.INVISIBLE
        edtDriAccountCountryCode.visibility = View.INVISIBLE

        tvDriAccountCarTypeShow.visibility = View.VISIBLE
        spinnerDriAccountCarType.visibility = View.INVISIBLE
        ivSignupDriverArrow.visibility = View.INVISIBLE

    }

    private fun getVehicleType()
    {

        val repository : RetroRepository = RetroRepository.instance
        val drivergetRoutes : MutableLiveData<GetVehicleTypeResponse>


        drivergetRoutes = repository.getvehicleType("",(activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        drivergetRoutes.observe(viewLifecycleOwner, object : Observer<GetVehicleTypeResponse> {
            override fun onChanged(t: GetVehicleTypeResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {

                            carArrayList.addAll(t.data)

                            for (i in 0 until carArrayList.size)
                            {
                                carNameList.add(carArrayList[i].name)
                            }

                            var adapter = ArrayAdapter(
                                context!!, R.layout.signup_spinner_design, carNameList
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                            spinnerDriAccountCarType.adapter = adapter

                        }
                        t.response.message.isNullOrEmpty() -> {
                        }
                        else -> {

                            if (t.response.message.equals("Authorization not correct", true)) {
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                (activity as HomeActivity).clearnotification()
                                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                            else if (t.response.logout == 1){
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                (activity as HomeActivity).clearnotification()
                                (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
                                (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }

                        }
                    }
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                    Log.d("APIRESPONSE",""+e.toString())
                }

            }
        })
    }

    private fun callgetProfile()
    {
        counter = counter.plus(1)
        val repository : RetroRepository = RetroRepository.instance
        val resetPassword : MutableLiveData<DriverProfileResponse>

        resetPassword = repository.getDriverprofile(WisamTaxiApplication.shrdPref.getString("auth","").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString())
        resetPassword.observe(viewLifecycleOwner, object : Observer<DriverProfileResponse> {
            override fun onChanged(t: DriverProfileResponse?)
            {
                Log.d("APIRESPONSE",""+t?.response?.message)
                try {
                    when {
                        t?.response!!.success -> {
                            countrycode = ""+t.data.countryCode.substringAfter("+")
                            WisamTaxiApplication.editor.putString("name_driver", ""+t.data.fullName)
                            WisamTaxiApplication.editor.putString("buildingname", ""+t.data.buildingNumber)
                            WisamTaxiApplication.editor.putString("flatno", ""+t.data.flatNumber)
                            WisamTaxiApplication.editor.putString("profilePic_driver", ""+t.data.profilePic)
                            WisamTaxiApplication.editor.putString("id_driver", ""+t.data.id)
                            WisamTaxiApplication.editor.putString("country_code_driver",""+t.data.countryCode.substringAfter("+"))
                            WisamTaxiApplication.editor.putString("mobile_no_driver",""+t.data.phone)
                            WisamTaxiApplication.editor.putString("cartype_driver",""+t.data.vehicleTypeId.name)

                            WisamTaxiApplication.editor.apply()
                            WisamTaxiApplication.editor.commit()

                            loadUserData()

                            if (!t.data.isVerified)
                            {
                                if (counter < 2)
                                {
                                    val intent = Intent(activity, EditProfileOtpVerifyActivity::class.java)
                                    intent.putExtra("code", ""+t.data.countryCode.substringAfter("+"))
                                    intent.putExtra("phone", ""+t.data.phone)
                                    startActivityForResult(intent, 10)
                                }
                            }

                        }
                        t.response.message.isEmpty() -> { }
                        else -> {
                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> (activity as HomeActivity).showToast(t.response.message)
                            }
                        }
                    }
                }
                catch (e:Exception)
                {
                    (activity as HomeActivity).dismissprogressBar()
                    e.printStackTrace()
                }

            }
        })
    }

    private fun callUpdateProfile()
    {
        (activity as HomeActivity).showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val updateprofile: MutableLiveData<DriverProfileResponse>

        val code = (activity as HomeActivity).requestbodyString("+$countrycode")
        val phone = (activity as HomeActivity).requestbodyString(""+tempPhone)
        val carType = (activity as HomeActivity).requestbodyString(carType)
        val fullName = (activity as HomeActivity).requestbodyString(edtDriAccountName.text.toString())
        val deviceToken = (activity as HomeActivity).requestbodyString(deviceToken)
        val deviceType = (activity as HomeActivity).requestbodyString("android")

        val buildingname = (activity as HomeActivity).requestbodyString(edtDriAccountBuilding.text.toString())
        val flatno = (activity as HomeActivity).requestbodyString(edtDriAccountFlat.text.toString())
        val password = (activity as HomeActivity).requestbodyString(WisamTaxiApplication.shrdPref.getString("password_driver", "").toString())

        val latitude = (activity as HomeActivity).requestbodyString(""+(activity as HomeActivity).sharedPref.getString("lat","20.265"))
        val longitude = (activity as HomeActivity).requestbodyString(""+(activity as HomeActivity).sharedPref.getString("long","15.233"))

        val body: MultipartBody.Part

        body = if (imagefile != null) { prepareFilePart("profile", Uri.parse(imageString))
        } else {
            val reqFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), "")
            MultipartBody.Part.createFormData("profile", "", reqFile)
        }

        updateprofile = repository.driverupdateProfile(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),
            code,
            phone,
            fullName,
            password,
            latitude,
            longitude,
            buildingname,
            flatno,
            deviceToken,
            deviceType,
            body,
            carType
        )
        updateprofile.observe(viewLifecycleOwner, object : Observer<DriverProfileResponse> {
            override fun onChanged(t: DriverProfileResponse?) {
                try {
                    when {
                        t?.response!!.success ->
                        {
                            (activity as HomeActivity).dismissprogressBar()
                            countrycode = ""+t.data.countryCode.substringAfter("+")
                            WisamTaxiApplication.editor.putString("name_driver", ""+t.data.fullName)
                            WisamTaxiApplication.editor.putString("buildingname", ""+t.data.buildingNumber)
                            WisamTaxiApplication.editor.putString("flatno", ""+t.data.flatNumber)
                            WisamTaxiApplication.editor.putString("profilePic_driver", ""+t.data.profilePic)
                            WisamTaxiApplication.editor.putString("id_driver", ""+t.data.id)
                            WisamTaxiApplication.editor.putString("country_code_driver",""+t.data.countryCode.substringAfter("+"))
                            WisamTaxiApplication.editor.putString("mobile_no_driver",""+t.data.phone)
                            WisamTaxiApplication.editor.putString("cartype_driver",""+t.data.vehicleTypeId.name)

                            WisamTaxiApplication.editor.apply()
                            WisamTaxiApplication.editor.commit()

                            loadUserData()

                            if (!t.data.isVerified)
                            {
                                val intent = Intent(activity, EditProfileOtpVerifyActivity::class.java)
                                intent.putExtra("code", ""+countrycode)
                                intent.putExtra("phone", tempPhone)
                                startActivityForResult(intent, 10)
                            }
                            else{
                                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.profileupdated))
                            }
                        }
                        t.response.message.isEmpty() -> {
                            (activity as HomeActivity).dismissprogressBar()
                        }
                        else ->
                        {
                            (activity as HomeActivity).dismissprogressBar()

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout == 1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> {
                                    (activity as HomeActivity).showToast(""+t.response.message)
                                    loadUserData()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    (activity as HomeActivity).dismissprogressBar()
                    loadUserData()
                    e.printStackTrace()
                }
                
            }
        })
    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), imagefile)
        return MultipartBody.Part.createFormData(partName, imagefile!!.name.trim(), requestFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionCodes.CAMERAPERMISSIONCODE) {
            try {
                val thumbnail: Bitmap
                thumbnail = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageUri)

                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                val input: InputStream? = context?.contentResolver?.openInputStream(imageUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    circleIvProfile.setImageBitmap(getRotatedImage(bitmap,input!!))
                    imageString = (activity as HomeActivity).saveImage(getRotatedImage(bitmap, input))
                    imagefile = File(imageString)
                }
                else
                {
                    circleIvProfile.setImageBitmap(thumbnail)
                    imageString = (activity as HomeActivity).saveImage(thumbnail)
                    imagefile = File(imageString)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else if (requestCode == 10 && resultCode == Activity.RESULT_OK)
        {
            if (!data!!.getStringExtra("success").isNullOrEmpty())
            {
               loadUserData()
                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.profileupdated))
            }else{
                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.mobilenonotverified))
            }
        }
        else
        {
            circleIvProfile.setImageURI(data?.data)
            imageString = data?.data.toString()
            imagefile = File((activity as HomeActivity).getRealPathFromURI(data?.data))
        }
    }

    private fun showPictureDialog() {

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.choosemedia_layout,null)

        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(true)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        tvchoosemediaCamera = mDialogView.findViewById<View>(R.id.tvchoosemediaCamera) as TextView
        ivchoosemediaCamera = mDialogView.findViewById<View>(R.id.ivchoosemediaCamera) as ImageView
        tvchoosemediaGallery = mDialogView.findViewById<View>(R.id.tvchoosemediaGallery) as TextView
        ivchoosemediaGallery = mDialogView.findViewById<View>(R.id.ivchoosemediaGallery) as ImageView
        tvchoosemediaCancel = mDialogView.findViewById<View>(R.id.tvchoosemediaCancel) as TextView

        tvchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()
        }
        ivchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()}

        tvchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()}
        ivchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()}

        tvchoosemediaCancel.setOnClickListener {
            mAlertDialog.dismiss() }
    }

    //Open camera with check permission
    private fun openCamera() {
        if (checkAndRequestPermissions2(PermissionCodes.CAMERAPERMISSIONCODE)) {
            camera()
        }
    }

    //open camera if permision granted
    private fun camera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = activity!!.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        startActivityForResult(takePicture, PermissionCodes.CAMERAPERMISSIONCODE)
    }

    private fun openGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (isGalleryPermissions()) {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(
                    pickPhoto,
                    PermissionCodes.GALLERYREQUESTCODE
                )
            }
        } else {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(
                pickPhoto,
                PermissionCodes.GALLERYREQUESTCODE
            )
        }
    }

    private fun isGalleryPermissions(): Boolean {

        var isGranted: Boolean = false
        val permission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        isGranted = if (permission != PackageManager.PERMISSION_GRANTED) {
            makeGalleryRequest()
            false

        } else {
            true
        }
        return isGranted
    }

    private fun makeGalleryRequest() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionCodes.READSTORAGEPERMISSIONCODE
        )
    }

    //Check multiple permissions
    private fun checkAndRequestPermissions2(REQUEST_ID_MULTIPLE_PERMISSIONS:Int): Boolean {
        val camera = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA)
        val storage = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val recordAudio = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.RECORD_AUDIO)
        val fineLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val courseLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val backgroundLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val listPermissionsNeeded = ArrayList<String>()
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA)
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.RECORD_AUDIO)
        }
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (courseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (backgroundLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity!!, listPermissionsNeeded.toArray(arrayOfNulls<String>(listPermissionsNeeded.size)), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun getRotatedImage(bitmap: Bitmap, input: InputStream): Bitmap
    {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(input!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("ProfileActivity", "Exception: $e")
        }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val rotatedBitmap: Bitmap?

        rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                rotateImage(bitmap, 270F)
            else ->
                bitmap
        }
        return rotatedBitmap
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun hideKeyboard() {
        if (activity!!.currentFocus != null)
        {
            val inputMethodManager: InputMethodManager =
                activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken, 0)
        }
    }

    private fun isvalid(): Boolean {

        when {
            countrycode.isEmpty() -> {
                (activity as HomeActivity).showToast(getString(R.string.pleaseentercountrycode))
                return false
            }
            edtDriAccountName.text.toString().trim().isEmpty() -> {
                (activity as HomeActivity).showToast(getString(R.string.pleaseenterfullname))
                return false
            }
            edtDriAccountMobile.text.toString().trim().isEmpty() -> {
                (activity as HomeActivity).showToast(getString(R.string.pleaseentermobileNo))
                return false
            }
            edtDriAccountMobile.text.toString().trim().length < 5 -> {
                (activity as HomeActivity).showToast(getString(R.string.pleaseentervalidphone))
                return false
            }
            edtDriAccountBuilding.text.toString().trim().isEmpty() -> {
                (activity as HomeActivity).showToast(getString(R.string.enterbuildingname))
                return false
            }
            edtDriAccountFlat.text.toString().trim().isEmpty() -> {
                (activity as HomeActivity).showToast(getString(R.string.enterflatno))
                return false
            }
            else -> return true
        }
    }

}
