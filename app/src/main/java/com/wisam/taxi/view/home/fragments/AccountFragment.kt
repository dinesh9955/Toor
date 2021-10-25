package com.wisam.taxi.view.home.fragments


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso

import com.wisam.taxi.R
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.response.getprofile.GetProfileResponse
import com.wisam.taxi.model.response.updateprofile.UpdateProfileResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.verification.activity.EditProfileOtpVerifyActivity
import com.ybs.countrypicker.CountryPicker
import kotlinx.android.synthetic.main.activity_forgotpassword.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.clCodePicker
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.async
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern

@Suppress("DEPRECATION")
class AccountFragment : Fragment() {


    companion object{
        var TAG = "AccountFragment"
    }

    var countrycode: String = "964"
    private var imageString: String? = ""
    private var imagefile: File? = null
    private var deviceToken: String = ""
    private var tempPhone: String = ""
    private var counter: Int = 0
    private var isValid: Boolean = false
    private lateinit var imageUri: Uri
    private lateinit var tvchoosemediaCamera: TextView
    private lateinit var ivchoosemediaCamera: ImageView
    private lateinit var tvchoosemediaGallery: TextView
    private lateinit var ivchoosemediaGallery: ImageView
    private lateinit var tvchoosemediaCancel: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        edtAccountName.isEnabled = false
        edtAccountEmail.isEnabled = false
        edtAccountAddress.isEnabled = false

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

        circleImageView.setOnClickListener {
            if (isValid) {
                showPictureDialog()
            }
        }

        ivAccountEdit.setOnClickListener {
            onEditClick()
        }

        ivAccountsave.setOnClickListener {

            if (isvalid()) {
                onSaveClick()
                tempPhone = ""+edtAccountMobile.text.toString()
                callUpdateProfile()
            }
        }

        clCodePicker.setOnClickListener {
            (activity as HomeActivity).picker = CountryPicker.newInstance("Select Country");
            (activity as HomeActivity).picker.setListener { name, code, p2, p3 ->
                try {
                    edtAccountCountryCode.setCountryForPhoneCode(p2.substringAfter("+").toInt())
                    countrycode = ""+edtAccountCountryCode.selectedCountryCodeAsInt.toString()
                }catch (e:Exception){
                    e.printStackTrace()
                }
                (activity as HomeActivity).picker.dismiss()
            }
            (activity as HomeActivity).picker.show(activity!!.supportFragmentManager, "COUNTRY_PICKER");
        }


    }

    private fun onEditClick() {

        isValid = true
        ivAccountsave.visibility = View.VISIBLE
        ivAccountEdit.visibility = View.INVISIBLE

        edtAccountName.isEnabled = true
        edtAccountEmail.isEnabled = true
        edtAccountAddress.isEnabled = true

        tvAccountMobileShow.visibility = View.INVISIBLE
        edtAccountMobile.visibility = View.VISIBLE
        edtAccountCountryCode.visibility = View.VISIBLE

        edtAccountName.isCursorVisible = true
        edtAccountName.requestFocus()
        edtAccountName.setSelection(edtAccountName.text.length)
    }

    private fun onSaveClick() {
        isValid = false
        hideKeyboard()
        ivAccountsave.visibility = View.INVISIBLE
        ivAccountEdit.visibility = View.VISIBLE

        edtAccountName.isEnabled = false
        edtAccountEmail.isEnabled = false
        edtAccountAddress.isEnabled = false

        tvAccountMobileShow.visibility = View.VISIBLE
        edtAccountMobile.visibility = View.INVISIBLE
        edtAccountCountryCode.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PermissionCodes.CAMERAPERMISSIONCODE) {
            try {
                val thumbnail: Bitmap = MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), imageUri)

                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                val input: InputStream? = context?.contentResolver?.openInputStream(imageUri!!)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    circleImageView.setImageBitmap(getRotatedImage(bitmap, input!!))
                    imageString = (activity as HomeActivity).saveImage(getRotatedImage(bitmap, input))
                    imagefile = File(imageString)
                    Log.d("IMAGEPATH", "" + imageString)
                }
                else
                {

                    circleImageView.setImageBitmap(thumbnail)
                    imageString = (activity as HomeActivity).saveImage(thumbnail)
                    imagefile = File(imageString)

                    Log.d("IMAGEPATH", "" + imageString)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else if (requestCode == 10 && resultCode == Activity.RESULT_OK)
        {
            if (!data!!.getStringExtra("success").isNullOrEmpty())
            {
                loadUserData()
                (activity as HomeActivity).showToast(getString(R.string.profileupdated))
            }else{
                (activity as HomeActivity).showToast(getString(R.string.mobilenonotverified))
            }
        }
        else
        {
            circleImageView.setImageURI(data?.data)

            imageString = data?.data.toString()
            imagefile = File((activity as HomeActivity).getRealPathFromURI(data?.data))

            Log.d("IMAGEPATH", "" + imageString)
        }
    }

    private fun showPictureDialog() {

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.choosemedia_layout, null)

        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(true)
        mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        tvchoosemediaCamera = mDialogView.findViewById<View>(R.id.tvchoosemediaCamera) as TextView
        ivchoosemediaCamera = mDialogView.findViewById<View>(R.id.ivchoosemediaCamera) as ImageView
        tvchoosemediaGallery = mDialogView.findViewById<View>(R.id.tvchoosemediaGallery) as TextView
        ivchoosemediaGallery =
            mDialogView.findViewById<View>(R.id.ivchoosemediaGallery) as ImageView
        tvchoosemediaCancel = mDialogView.findViewById<View>(R.id.tvchoosemediaCancel) as TextView

        tvchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()
        }
        ivchoosemediaCamera.setOnClickListener {
            openCamera()
            mAlertDialog.dismiss()
        }

        tvchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()
        }
        ivchoosemediaGallery.setOnClickListener {
            openGallery()
            mAlertDialog.dismiss()
        }

        tvchoosemediaCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }
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
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
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
            Log.i("AccountFrag", "Permission to get image from gallery denied")
            makeGalleryRequest()
            false
        }
        else
            true

        return isGranted
    }

    public fun makeGalleryRequest() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionCodes.READSTORAGEPERMISSIONCODE
        )
    }

    //Check multiple permissions
    fun checkAndRequestPermissions2(REQUEST_ID_MULTIPLE_PERMISSIONS: Int): Boolean {
        val camera =  ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA)
        val storage = ContextCompat.checkSelfPermission( context!!,  android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(  context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity!!,
                listPermissionsNeeded.toArray(arrayOfNulls<String>(listPermissionsNeeded.size)),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getRotatedImage(bitmap: Bitmap, input: InputStream): Bitmap {
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
        var rotatedBitmap: Bitmap?
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                rotatedBitmap = rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                rotatedBitmap = rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                rotatedBitmap = rotateImage(bitmap, 270F)
            else ->
                rotatedBitmap = bitmap
        }
        return rotatedBitmap!!
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun hideKeyboard() {
        if (activity!!.getCurrentFocus() != null) {
            val inputMethodManager: InputMethodManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity!!.getCurrentFocus()!!.getWindowToken(),
                0
            )
        }
    }

    private fun callgetProfile()
    {
        counter = counter.plus(1)
        Log.d("counter", "" +counter)
        val repository: RetroRepository = RetroRepository.instance
        val getprofile: MutableLiveData<GetProfileResponse>

        getprofile = repository.getCustomerprofile(
            WisamTaxiApplication.shrdPref.getString(
                "auth",
                ""
            ).toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString()
        )
        getprofile.observe(viewLifecycleOwner, object : Observer<GetProfileResponse> {
            override fun onChanged(t: GetProfileResponse?) {

                try {

                    when {
                        t?.response!!.success -> {

                            WisamTaxiApplication.editor.putString(
                                "country_code",
                                "" + t.data.countryCode.substringAfter("+")
                            )
                            WisamTaxiApplication.editor.putString("mobile_no", "" + t.data.phone)
                            WisamTaxiApplication.editor.putString("name", "" + t.data.fullName)
                            WisamTaxiApplication.editor.putString("address", "" + t.data.address)
                            WisamTaxiApplication.editor.putString("address1", "" + t.data.address1)
                            WisamTaxiApplication.editor.putString("email", "" + t.data.email)
                            WisamTaxiApplication.editor.putString("profilePic", "" + t.data.profilePic)
                            WisamTaxiApplication.editor.putString("id", "" + t.data.id)

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
                                    (activity as HomeActivity).showToast(resources.getString(R.string.sessionexpired))
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    fun loadUserData() {
        tvProfileName.text = "" + WisamTaxiApplication.shrdPref.getString("name", "")
        tvprofileDetail.text = "" + WisamTaxiApplication.shrdPref.getString(
            "address",
            ""
        ) + " " + WisamTaxiApplication.shrdPref.getString("address1", "")
        edtAccountName.setText("" + WisamTaxiApplication.shrdPref.getString("name", ""))
        edtAccountEmail.setText("" + WisamTaxiApplication.shrdPref.getString("email", ""))
        val address = "" + WisamTaxiApplication.shrdPref.getString(
            "address",
            ""
        ) + "" + WisamTaxiApplication.shrdPref.getString("address1", "")

        if (!address.isNullOrEmpty()) {
            edtAccountAddress.setText(
                "" + WisamTaxiApplication.shrdPref.getString("address", "") + " " +
                        WisamTaxiApplication.shrdPref.getString("address1", "")
            )
        } else {
            edtAccountAddress.setHint("Address")
        }
        tvAccountMobileShow.text =
            "+" + WisamTaxiApplication.shrdPref.getString(
                "country_code", ""
            ) + " " + WisamTaxiApplication.shrdPref.getString("mobile_no", "")

        edtAccountMobile.setText("" + WisamTaxiApplication.shrdPref.getString("mobile_no", ""))



        if(WisamTaxiApplication.shrdPref.getString(  "country_code",  "") != ""){
            edtAccountCountryCode.setCountryForPhoneCode(
                WisamTaxiApplication.shrdPref.getString(
                    "country_code",
                    ""
                )!!.toInt()
            )
        }

//        Log.e(TAG, "QQQQQQQ "+ WisamTaxiApplication.shrdPref.getString(
//            "country_code",
//            ""
//        ))

        val profilePic = "" + WisamTaxiApplication.shrdPref.getString("profilePic", "")

        if (profilePic.isEmpty()) {
            circleImageView.setImageResource(R.drawable.group_445)
        } else {
            Picasso.get()
                .load(WisamTaxiApplication.BASE_URL + "" + profilePic)
                .placeholder(R.drawable.group_445)
                .into(circleImageView)
        }


        countrycode = "" + WisamTaxiApplication.shrdPref.getString("country_code", "")
    }

    private fun callUpdateProfile() {
        (activity as HomeActivity).showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val updateprofile: MutableLiveData<UpdateProfileResponse>

        val code = (activity as HomeActivity).requestbodyString("+$countrycode")
        val phone = (activity as HomeActivity).requestbodyString(tempPhone)
        val email = (activity as HomeActivity).requestbodyString(edtAccountEmail.text.toString())
        val fullName = (activity as HomeActivity).requestbodyString(edtAccountName.text.toString())
        val deviceToken = (activity as HomeActivity).requestbodyString(deviceToken)
        val deviceType = (activity as HomeActivity).requestbodyString("android")

        val address =
            (activity as HomeActivity).requestbodyString(edtAccountAddress.text.toString())
        val password = (activity as HomeActivity).requestbodyString(
            WisamTaxiApplication.shrdPref.getString("password", "12345678").toString()
        )
        val address1 = (activity as HomeActivity).requestbodyString("")
        val latitude = (activity as HomeActivity).requestbodyString(
            "" + (activity as HomeActivity).sharedPref.getString(
                "lat",
                "20.265"
            )
        )
        val longitude = (activity as HomeActivity).requestbodyString(
            "" + (activity as HomeActivity).sharedPref.getString(
                "long",
                "15.233"
            )
        )


        val body: MultipartBody.Part

        body = if (imagefile != null) {
            prepareFilePart("profile", Uri.parse(imageString))
        } else {
            val reqFile: RequestBody =
                RequestBody.create(MediaType.parse("image/*"), "")
            MultipartBody.Part.createFormData("profile", "", reqFile)
        }

        updateprofile = repository.updateProfile(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            (activity as HomeActivity).sharedPref.getString("mylang", "en").toString(),
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
        updateprofile.observe(viewLifecycleOwner, object : Observer<UpdateProfileResponse> {
            override fun onChanged(t: UpdateProfileResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {
                    when {
                        t?.response!!.success -> {

                            (activity as HomeActivity).dismissprogressBar()

                            WisamTaxiApplication.editor.putString("country_code", "" + t.data.countryCode.substringAfter("+"))
                            WisamTaxiApplication.editor.putString("mobile_no", "" + t.data.phone)
                            WisamTaxiApplication.editor.putString("name", "" + t.data.fullName)
                            WisamTaxiApplication.editor.putString("address", "" + t.data.address)
                            WisamTaxiApplication.editor.putString("address1", "" + t.data.address1)
                            WisamTaxiApplication.editor.putString("email", "" + t.data.email)
                            WisamTaxiApplication.editor.putString("profilePic", "" + t.data.profilePic)
                            WisamTaxiApplication.editor.putString("id", "" + t.data.id)

                            WisamTaxiApplication.editor.apply()
                            WisamTaxiApplication.editor.commit()

                            loadUserData()

                            if (!t.data.isVerified)
                            {
                                val intent = Intent(activity, EditProfileOtpVerifyActivity::class.java)
                                intent.putExtra("code", ""+countrycode)
                                intent.putExtra("phone", tempPhone)
                                startActivityForResult(intent, 10)

                            }else
                                (activity as HomeActivity).showToast(context!!.resources.getString(R.string.profileupdated))

                        }
                        t.response.message.isEmpty() -> {
                            (activity as HomeActivity).dismissprogressBar()
                        }
                        else -> {
                            (activity as HomeActivity).dismissprogressBar()

                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    (activity as HomeActivity).clearnotification()
                                    (activity as HomeActivity).showToast(context!!.resources.getString(R.string.sessionexpired))
                                    (activity as HomeActivity).navigateFinishAffinity(
                                        ChooseUserTypeActivity::class.java
                                    )
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
                    e.printStackTrace()
                }

            }
        })
    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {

        val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), imagefile)
        return MultipartBody.Part.createFormData(partName, imagefile!!.name.trim(), requestFile)
    }

    private fun isvalid(): Boolean {

        if (countrycode.isEmpty()) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseentercountrycode))
            return false
        } else if (edtAccountName.text.toString().trim().isEmpty()) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseenterfullname))
            return false
        } else if (edtAccountEmail.text.toString().trim().isEmpty() || !edtAccountEmail.text.toString().isEmailValid()) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseentervalidemail))
            return false
        } else if (edtAccountMobile.text.toString().trim().isEmpty()) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseentermobileNo))
            return false
        }else if (edtAccountMobile.text.toString().trim().length < 5) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseentervalidphone))
            return false
        } else if (edtAccountAddress.text.toString().trim().isEmpty()) {
            (activity as HomeActivity).showToast(getString(R.string.pleaseenteraddress))
            return false
        }
        return true
    }

    private fun String.isEmailValid() =
        Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        ).matcher(this).matches()

}
