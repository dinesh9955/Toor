package com.wisam.taxi.base

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.wisam.taxi.R
import com.wisam.taxi.socket.AppSocketListener
import com.ybs.countrypicker.CountryPicker
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.math.*

open class BaseActivity : AppCompatActivity() {

    lateinit var editor: SharedPreferences.Editor
    lateinit var sharedPref: SharedPreferences
    lateinit var imageUri: Uri
    private lateinit var progressDialog: CustomProgressBar
    lateinit var circularProgressDrawable: CircularProgressDrawable
    private val IMAGE_DIRECTORY = "/WisamTaxi"
    lateinit var context: Context
    lateinit var mLocAlertDialog: AlertDialog
    private lateinit var tvchoosemediaCamera: TextView
    private lateinit var ivchoosemediaCamera: ImageView
    private lateinit var tvchoosemediaGallery: TextView
    private lateinit var ivchoosemediaGallery: ImageView
    private lateinit var tvchoosemediaCancel: TextView
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private var snackBar: Snackbar? = null
    lateinit var picker : CountryPicker
    internal lateinit var notificationManager: NotificationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
//        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        context = this
        sharedPref = this.getSharedPreferences("com.wisam.taxi", Context.MODE_PRIVATE)
        editor = sharedPref!!.edit()

        setLanguage(this)

        WisamTaxiApplication.shrdPref = getSharedPreferences("com.wisam.taxi", Context.MODE_PRIVATE)
        WisamTaxiApplication.editor = WisamTaxiApplication.shrdPref.edit()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        progressDialog = CustomProgressBar(this@BaseActivity)

        circularProgressDrawable = CircularProgressDrawable(this@BaseActivity)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        initGoogleLogin()
    }

    fun clearnotification()
    {
        try { notificationManager.cancel(89) }catch (e: Exception){e.printStackTrace()}
    }

    fun initGoogleLogin() {
        //Google Sign-In Options

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    fun navigate(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
    }

    fun navigateWithFinish(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
        finish()
    }

    fun navigateFinishAffinity(destination: Class<*>) {
        startActivity(Intent(this@BaseActivity, destination))
        finishAffinity()
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun showPictureDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.choosemedia_layout, null)

        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
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

    fun openGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (isGalleryPermissions()) {
                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, PermissionCodes.GALLERYREQUESTCODE)
            }
        } else {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, PermissionCodes.GALLERYREQUESTCODE)
        }
    }

    //Open camera with check permission
    fun openCamera() {
        if (checkAndRequestPermissions2(PermissionCodes.CAMERAPERMISSIONCODE)) {
            camera()
        }
    }

    //open camera if permision granted
    private fun camera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(takePicture, PermissionCodes.CAMERAPERMISSIONCODE)
    }

    private fun isGalleryPermissions(): Boolean {
        var isGranted: Boolean = false
        val permission = ContextCompat.checkSelfPermission(
            this@BaseActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        isGranted = if (permission != PackageManager.PERMISSION_GRANTED) {
            makeGalleryRequest()
            false
        }
        else
            true

        return isGranted
    }

    private fun makeGalleryRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PermissionCodes.READSTORAGEPERMISSIONCODE
        )
    }

    // save captured image from camera
    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists())
            wallpaperDirectory.mkdirs()

        try {
            val f = File(wallpaperDirectory, ((Calendar.getInstance().timeInMillis).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.path), arrayOf("image/jpeg"), null)
            fo.close()

            return f.absolutePath

        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    // save captured image from camera
    fun saveImageWithFile(myBitmap: Bitmap?): File? {


        var file: File? = null
        val wallpaperDirectory = File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists())
            wallpaperDirectory.mkdirs()

//        try {
//            val f = File()
//            f.createNewFile()
//            val fo = FileOutputStream(f)
//            fo.write(bytes.toByteArray())
//            MediaScannerConnection.scanFile(this, arrayOf(f.path), arrayOf("image/jpeg"), null)
//            fo.close()
//
//            return f.absolutePath
//
//        } catch (e1: IOException) {
//            e1.printStackTrace()
//        }

        if (myBitmap != null) {
            file = File(wallpaperDirectory, ((Calendar.getInstance().timeInMillis).toString() + ".jpg"))
            try {
                var outputStream: FileOutputStream? = null
                try {
                    outputStream = FileOutputStream(file)
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        outputStream?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return file
    }

    //Check multiple permissions
    private fun checkAndRequestPermissions2(REQUEST_ID_MULTIPLE_PERMISSIONS: Int): Boolean {
        val camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val storage = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val read = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val recordAudio = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.RECORD_AUDIO)
        val fineLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val courseLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val backgroundLocation = ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (camera != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA)

        if (storage != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (read != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

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
            ActivityCompat.requestPermissions(
                this,
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
            exif = ExifInterface(input)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotatedBitmap: Bitmap?

        rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)
            else -> bitmap
        }
        return rotatedBitmap
    }

    fun showprogressbar() {
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun dismissprogressBar() {
        progressDialog.dismiss()
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.preRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {
        var stringPath: String = ""
        stringPath = try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = managedQuery(contentUri, proj, null, null, null)
            val columnindex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnindex)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return stringPath
    }


    fun requestbodyString(string: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), string.trim())
    }

    fun String.isEmailValid() =
        Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        ).matcher(this).matches()

    fun hideKeyboard() {
        if (this.currentFocus != null) {
            val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
    }

    fun sendObjectToSocket(jsonObject: JSONObject, type: String) {
        try {
            AppSocketListener.getInstance().emit(type, jsonObject)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLocationDialog(title: String) {
        val mDialogView = LayoutInflater.from(this).inflate(
            R.layout.locationpermission_alertdialog,
            null
        )

        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        mLocAlertDialog = mBuilder.show()
        mLocAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val confirmAlertdescTv = mDialogView.findViewById<View>(R.id.confirmAlertdescTv) as TextView
        val tvSettings = mDialogView.findViewById<View>(R.id.locSettingslTv) as TextView
        val tvCancel = mDialogView.findViewById<View>(R.id.locCancelTv) as TextView

        confirmAlertdescTv.text = title

        tvCancel.setOnClickListener {
            mLocAlertDialog.dismiss()
        }

        tvSettings.setOnClickListener {
            try {
                startInstalledAppDetailsActivity(this@BaseActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startInstalledAppDetailsActivity(context: Activity)
    {
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + context.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
        mLocAlertDialog.dismiss()
    }

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val startPoint = Location("locationA")
        startPoint.latitude = lat1
        startPoint.longitude = lon1
        val endpoint = Location("locationB")
        endpoint.latitude = lat2
        endpoint.longitude = lon2

        val distancebtw = startPoint.distanceTo(endpoint)

        return (distancebtw / 800).toInt()
    }

    fun getKilometers(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val PI_RAD = Math.PI / 180.0
        val phi1 = lat1 * PI_RAD
        val phi2 = lat2 * PI_RAD
        val lam1 = long1 * PI_RAD
        val lam2 = long2 * PI_RAD

        return 6371.01 * acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1))
    }

    fun haversine(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val Rad = 6372.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(long2 - long1)
        val lati1 = Math.toRadians(lat1)
        val lati2 = Math.toRadians(lat2)
        val a = sin(dLat / 2) * sin(dLat / 2) + sin(dLon / 2) * sin(dLon / 2) * cos(lati1) * cos(
            lati2
        )
        val c = 2 * asin(sqrt(a))
        return Rad * c
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            (mConnReceiver),
            IntentFilter("EVENT_SNACKBAR")
        )
    }

    private val mConnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                val data = intent!!.extras!!.getString("status")

                if (data.equals("lost", true)) {
                    showNetworkMessage()
                } else {
                    snackBar!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNetworkMessage()
    {
        val view = findViewById<View>(android.R.id.content)
        snackBar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        val layout = snackBar!!.view as Snackbar.SnackbarLayout
        snackBar!!.view.setBackgroundColor(Color.WHITE)
        layout.findViewById<View>(R.id.snackbar_text).visibility = View.INVISIBLE
        val snackView = layoutInflater.inflate(R.layout.network_connection_layout, null)
        layout.addView(snackView, 0)
        snackBar!!.show()

    }

    override fun onResume() {
        setLanguage(this)
        super.onResume()
    }

    fun changeLanguage(language: String, context: Context) {
        val lang = if(language.isBlank()){"en"}else{language}
        val locale = Locale(lang, "US")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        editor.putString("mylang", lang).apply()
    }

    fun setLanguage(context: Context) {
        val language = sharedPref.getString("mylang", "en").toString()
        val lang = if(language.isBlank()){"en"}else{language}
        val locale = Locale(lang, "US")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
