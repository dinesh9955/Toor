package com.wisam.taxi.view.documents

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.base.PermissionCodes
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.common.RetroRepository
import com.wisam.taxi.model.driverResponse.getdocuments.GetDocumentsResponse
import com.wisam.taxi.model.driverResponse.updateDocuments.UpdateDocumentsResponse
import com.wisam.taxi.view.chooseUserType.ChooseUserTypeActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import kotlinx.android.synthetic.main.activity_documents.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

@Suppress("DEPRECATION")
class DocumentsActivity : BaseActivity()
{
    lateinit var documentResponse: GetDocumentsResponse
    private var isLicense: Boolean = false
    var isUploadContract: Boolean = false
    private var imagefile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        ivDocumentBack.setOnClickListener {
            finish()
        }

        tvDocumentLicenseChange.setOnClickListener {
            makeLicenseTrue()
            showPictureDialog()
        }

        tvDocumentContractChange.setOnClickListener {
            makeContractTrue()
            showPictureDialog()
        }

        callgetDocuments()
    }


    private fun callgetDocuments() {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val resetPassword: MutableLiveData<GetDocumentsResponse>

        resetPassword = repository.getDriverDocuments(WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString())
        resetPassword.observe(this, object : Observer<GetDocumentsResponse> {
            override fun onChanged(t: GetDocumentsResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            dismissprogressBar()

                            documentResponse = t

                            Picasso.get()
                                .load(WisamTaxiApplication.BASE_URL + "" + t.data.docsList[1].image)
                                .placeholder(circularProgressDrawable).into(ivDocumentLicense)

                            Picasso.get()
                                .load(WisamTaxiApplication.BASE_URL + "" + t.data.docsList[0].image)
                                .placeholder(circularProgressDrawable).into(ivDocumentContract)

                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()
                            ivDocumentLicense.setImageResource(R.drawable.roundcorner_disabled_bg)
                            ivDocumentContract.setImageResource(R.drawable.roundcorner_disabled_bg)

                            if (t.response.message.equals("Authorization not correct", true)) {
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                clearnotification()
                                showToast(resources.getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            } else if (t.response.logout == 1) {
                                WisamTaxiApplication.editor.putString("auth", "").apply()
                                clearnotification()
                                showToast(resources.getString(R.string.sessionexpired))
                                navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    ivDocumentLicense.setImageResource(R.drawable.roundcorner_disabled_bg)
                    ivDocumentContract.setImageResource(R.drawable.roundcorner_disabled_bg)
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())
                }
            }
        })
    }

    private fun callUpdateDocuments(id: String) {
        showprogressbar()
        val repository: RetroRepository = RetroRepository.instance
        val resetPassword: MutableLiveData<UpdateDocumentsResponse>

        val id = requestbodyString(id)

        val file: MultipartBody.Part = prepareFilePart("file")

        resetPassword = repository.updateDocuments(
            WisamTaxiApplication.shrdPref.getString("auth", "").toString(),
            sharedPref.getString("mylang", "en").toString(), file, id
        )
        resetPassword.observe(this, object : Observer<UpdateDocumentsResponse> {
            override fun onChanged(t: UpdateDocumentsResponse?) {
                Log.d("APIRESPONSE", "" + t?.response?.message)

                try {

                    when {
                        t?.response!!.success -> {
                            showToast(t.response.message)
                            dismissprogressBar()
                        }
                        t.response.message.isEmpty() -> {
                            dismissprogressBar()
                        }
                        else -> {
                            dismissprogressBar()

                            if (isUploadContract) {

                                if (documentResponse.data.docsList[0].image.isEmpty())
                                    ivDocumentContract.setImageResource(R.drawable.roundcorner_disabled_bg)
                                else
                                    Picasso.get().load(WisamTaxiApplication.BASE_URL + "" + documentResponse.data.docsList[0].image).placeholder(circularProgressDrawable).into(ivDocumentContract)

                            }
                            else {

                                if (documentResponse.data.docsList[1].image.isEmpty())
                                    ivDocumentLicense.setImageResource(R.drawable.roundcorner_disabled_bg)
                                else
                                    Picasso.get().load(WisamTaxiApplication.BASE_URL + "" + documentResponse.data.docsList[1].image).placeholder(circularProgressDrawable).into(ivDocumentLicense)

                            }
                            when {
                                t.response.message.equals("Authorization not correct", true) -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(resources.getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                t.response.logout ==1 -> {
                                    WisamTaxiApplication.editor.putString("auth", "").apply()
                                    clearnotification()
                                    showToast(resources.getString(R.string.sessionexpired))
                                    navigateFinishAffinity(ChooseUserTypeActivity::class.java)
                                }
                                else -> showToast("" + t.response.message)
                            }
                        }
                    }
                } catch (e: Exception) {
                    dismissprogressBar()
                    e.printStackTrace()
                    Log.d("APIRESPONSE", "" + e.toString())

                    if (isUploadContract) {

                        if (documentResponse.data.docsList[0].image.isEmpty())
                            ivDocumentContract.setImageResource(R.drawable.roundcorner_disabled_bg)
                        else
                            Picasso.get().load(WisamTaxiApplication.BASE_URL + "" + documentResponse.data.docsList[0].image).placeholder(circularProgressDrawable).into(ivDocumentContract)

                    }
                    else {

                        if (documentResponse.data.docsList[1].image.isEmpty())
                            ivDocumentLicense.setImageResource(R.drawable.roundcorner_disabled_bg)
                        else
                            Picasso.get().load(WisamTaxiApplication.BASE_URL + "" + documentResponse.data.docsList[1].image).placeholder(circularProgressDrawable).into(ivDocumentLicense)
                    }
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionCodes.CAMERAPERMISSIONCODE) {
            try {
                val thumbnail: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val input: InputStream? = contentResolver?.openInputStream(imageUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    Log.e("BLock","Naugat block")
                    if (isUploadContract) {
                        ivDocumentContract.setImageBitmap(getRotatedImage(bitmap, input!!))
                        imagefile = saveImageWithFile(getRotatedImage(bitmap, input))
                        callUpdateDocuments(documentResponse.data.docsList[0].id)
                    }
                    else {
                        ivDocumentLicense.setImageBitmap(getRotatedImage(bitmap, input!!))
                        imagefile = saveImageWithFile(getRotatedImage(bitmap, input))
                        callUpdateDocuments(documentResponse.data.docsList[1].id)
                    }
                } else {
                    Log.e("BLock"," Outside Naugat block")
                    if (isUploadContract) {
                        ivDocumentContract.setImageBitmap(thumbnail)
                        imagefile = File(saveImage(thumbnail))
                        callUpdateDocuments(documentResponse.data.docsList[0].id)

                    } else {
                        ivDocumentLicense.setImageBitmap(thumbnail)
                        imagefile = File(saveImage(thumbnail))
                        callUpdateDocuments(documentResponse.data.docsList[1].id)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                if (isUploadContract) {
                    ivDocumentContract.setImageURI(data?.data)
                    imagefile = File(getRealPathFromURI(data?.data)?:"")
                    callUpdateDocuments(documentResponse.data.docsList[0].id)
                }
                else {
                    ivDocumentLicense.setImageURI(data?.data)
                    imagefile = File(getRealPathFromURI(data?.data)?:"")
                    callUpdateDocuments(documentResponse.data.docsList[1].id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun prepareFilePart(partName: String): MultipartBody.Part {
        val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/*"), imagefile)
        return MultipartBody.Part.createFormData(partName, imagefile!!.name.trim(), requestFile)
    }

    private fun makeLicenseTrue() {
        isUploadContract = false
        isLicense = true
    }

    private fun makeContractTrue() {
        isUploadContract = true
        isLicense = false
    }
}
