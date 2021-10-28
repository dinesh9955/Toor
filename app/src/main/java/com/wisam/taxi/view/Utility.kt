package com.patchoguefd.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager

import android.os.Build

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.util.*

public class Utility {

    companion object {
        val MY_PERMISSIONS_REQUEST = 123
        private val TAG = "Utility"

        fun checkAndRequestPermissions(context: Activity, request: Int): Boolean {
            val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            val writeExternalStorage =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            val readExternalStorage =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            val coarseLocartion = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val fineLocartion =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//            val callPhone =
//                ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
//            val readContacts =
//                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)

            val listPermissionsNeeded = ArrayList<String>()

            if (camera != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA)
            }
            if (writeExternalStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (readExternalStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (coarseLocartion != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            if (fineLocartion != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
//            if (callPhone != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.CALL_PHONE)
//            }
//            if (readContacts != PackageManager.PERMISSION_GRANTED) {
//                listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS)
//            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(
                    context, listPermissionsNeeded.toTypedArray
                        (), request
                )
                return false
            }
            return true
        }


        fun checkPermissionsCheck(splashScreen: Activity): Boolean {
            return if (ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    splashScreen,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else false
        }


        fun checkAdditionPermissionsCheck(
            splashScreen: Activity,
            permissions: Array<String>,
            grantResults: IntArray,
            REQUEST_ID_MULTIPLE_PERMISSIONS: Int
        ): Boolean {

            val perms = HashMap<String, Int>()

            perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
            perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
            perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
            perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
//            perms[Manifest.permission.CALL_PHONE] = PackageManager.PERMISSION_GRANTED
//            perms[Manifest.permission.READ_CONTACTS] = PackageManager.PERMISSION_GRANTED

            if (grantResults.size > 0) {
                for (i in permissions.indices)
                    perms[permissions[i]] = grantResults[i]
                // Check for both permissions
                if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED &&
                    perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
//                    perms[Manifest.permission.CALL_PHONE] == PackageManager.PERMISSION_GRANTED &&
//                    perms[Manifest.permission.READ_CONTACTS] == PackageManager.PERMISSION_GRANTED
                ) {
                    //   Log.d(TAG, "Please allow access to all asked permissions. Due to the nature of the friendlywagon app, access to these areas on your mobile devices are necessary  for the app to function properly.");
                    // process the normal flow
                    //else any one or both the permissions are not granted
                    //                Toast.makeText(splashScreen, "CCCCCCCCCc", Toast.LENGTH_LONG)
                    //                        .show();
                    return true
                } else {
                    //  Log.d(TAG, "Please allow access to all asked permissions. Due to the nature of the friendlywagon app, access to these areas on your mobile devices are necessary  for the app to function properly.");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            splashScreen,
                            Manifest.permission.CAMERA
                        ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            splashScreen,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            splashScreen,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            splashScreen,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            splashScreen,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
//                        ActivityCompat.shouldShowRequestPermissionRationale(
//                            splashScreen,
//                            Manifest.permission.CALL_PHONE
//                        ) ||
//                        ActivityCompat.shouldShowRequestPermissionRationale(
//                            splashScreen,
//                            Manifest.permission.READ_CONTACTS
//                        )
                    ) {
                        //                    showDialogOK(splashScreen, "Please allow access to all asked permissions, access to your mobile devices are necessary for the app to function properly.",
                        showDialogOK(splashScreen, "Please allow access to all asked permissions.",
                            DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions(
                                        splashScreen,
                                        REQUEST_ID_MULTIPLE_PERMISSIONS
                                    )
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                    }
                                }
                            })
                    } else {
                        return true
                        //                    Toast.makeText(splashScreen, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                        //                            .show();
                    }
                }
            } else {

            }


            return false
        }


        private fun showDialogOK(
            splashScreen: Activity,
            message: String,
            okListener: DialogInterface.OnClickListener
        ) {
            AlertDialog.Builder(splashScreen)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
        }


        fun isEmptyNull(address: String?): String {
            return if (address != null && !address.isEmpty() && !address.equals(
                    "null",
                    ignoreCase = true
                )
            ) {
                address
            } else {
                ""
            }
        }

    }

}
