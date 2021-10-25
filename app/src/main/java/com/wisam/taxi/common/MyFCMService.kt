package com.wisam.taxi.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.wisam.taxi.R
import com.wisam.taxi.view.booking.activity.BookingConfirmActivity
import com.wisam.taxi.view.driverModule.chooseActive.ChooseActiveActivity
import com.wisam.taxi.view.home.activity.HomeActivity
import com.wisam.taxi.view.rideComplete.RideCompleteActivity
import com.wisam.taxi.view.splash.SplashActivity
import org.json.JSONObject


class MyFCMService : FirebaseMessagingService() {
    private var notifyPendingIntent: PendingIntent? = null
    private lateinit var broadcaster: LocalBroadcastManager
    lateinit var notidata: JSONObject
    lateinit var driverData: JSONObject
    lateinit var editor: SharedPreferences.Editor
    lateinit var sharedPref: SharedPreferences

    override fun onCreate() {

        broadcaster = LocalBroadcastManager.getInstance(this)

        sharedPref = this.getSharedPreferences("com.wisam.taxi", Context.MODE_PRIVATE)
        editor = sharedPref!!.edit()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//
//        val resultIntent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        notifyPendingIntent = PendingIntent.getActivity(this,1001, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

//         Check if message contains a notification payload.
//        remoteMessage.notification?.let {
//            Log.d("MyFCMService", "Message Notification Body: ${it.body}")
//        }

        // Check if message contains a data payload.
//        remoteMessage.data.isNotEmpty().let {
//            Log.d("MyFCMService", "Message data payload: " + remoteMessage.data.values)
//        }

        if (remoteMessage.notification != null) {

            Log.d("MyFCMService", "Message notification payload: " + remoteMessage.notification!!.title!!+" body : "+remoteMessage.notification!!.body)
//            showNotification(
//                remoteMessage?.notification!!.title!!,
//                remoteMessage?.notification!!.body!!
//            )
        }
        else
        {

            try {

                val dataMap = remoteMessage.data
                var resultIntent : Intent? = null
                Log.d("MyFCMService", "Message data payload: $dataMap")

                if (dataMap.get("type").toString().trim().equals("2",true))
                {
                    notidata = JSONObject(dataMap.get("notiData"))

                    val driverdata = notidata.get("driverId")

                    driverData= JSONObject(driverdata.toString())

                    Log.d("MyFCMService", "Message data payload: data array : $driverData")

                    var intent = Intent("mNotiDataReceiver")

                    if (dataMap.get("title").toString().trim().equals("Ride Confirmed",true))
                    {
                        Handler().postDelayed({
                            intent.putExtra("notimessage", "" + dataMap.get("title").toString().trim())
                            intent.putExtra("id", "" + notidata.get("id"))
                            broadcaster.sendBroadcast(intent)
                        },2000)

                        resultIntent = Intent(this, BookingConfirmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        resultIntent.putExtra("bookingId",""+notidata.get("id"))
                        resultIntent.putExtra("intentfrom","booking")
                    }
                    else if (dataMap.get("title").toString().trim().equals("Ride Started",true)){

                        intent.putExtra("notimessage", "" + dataMap.get("title").toString().trim())
                        intent.putExtra("id", "" + notidata.get("id"))
                        broadcaster.sendBroadcast(intent)

                        resultIntent = Intent(this, BookingConfirmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        resultIntent.putExtra("bookingId",""+notidata.get("id"))
                        resultIntent.putExtra("intentfrom","booking")
                    }
                    else if (dataMap.get("title").toString().trim().equals("Ride Reached",true))
                    {
                        intent.putExtra("notimessage", "" + dataMap.get("title").toString().trim())
                        intent.putExtra("id", "" + notidata.get("id"))
                        broadcaster.sendBroadcast(intent)

                        resultIntent = Intent(this, RideCompleteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        resultIntent.putExtra("bookingId",""+notidata.get("id"))
                        resultIntent.putExtra("driverId", ""+driverData.get("id"))
                        resultIntent.putExtra("name", ""+driverData.get("fullName"))
                        resultIntent.putExtra("profilePic", ""+driverData.get("profilePic"))
                        resultIntent.putExtra("rating",""+notidata.get("driverRating"))
                    }
                    else
                    {
                        resultIntent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    notifyPendingIntent = PendingIntent.getActivity(this,1001, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    showNotification(dataMap.get("title").toString().trim(), dataMap.get("body").toString().trim())

                }
                else if (dataMap.get("type").toString().trim().equals("52",true))
                {
                    notidata = JSONObject(dataMap.get("notiData"))

                    if (!ChooseActiveActivity.isAppOpen)
                    {
                        editor.putString("bookingdata",dataMap.get("notiData").toString()).apply()
                        editor.commit()

                        resultIntent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        notifyPendingIntent = PendingIntent.getActivity(this,1001, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        showNotification(dataMap.get("title").toString().trim(), dataMap.get("body").toString().trim())
                    }
                }
                else
                {
                    resultIntent = Intent(this, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    notifyPendingIntent = PendingIntent.getActivity(this,1001, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    showNotification(dataMap.get("title").toString().trim(), dataMap.get("body").toString().trim())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun showNotification(title: String, body: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var mBuilder = NotificationCompat.Builder(this@MyFCMService, "008")
                .setContentText(body)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLights(Color.WHITE, 3000, 1000)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setAutoCancel(true)
//                .setNumber(1)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(notifyPendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel(
                "008",
                this@MyFCMService.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            // Configure the notification channel.
            mChannel.enableVibration(true)
            mChannel.setShowBadge(true)
            mChannel.vibrationPattern = longArrayOf(4000)
            mChannel.enableLights(true)
            mChannel.lightColor = Notification.COLOR_DEFAULT
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            mChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(mChannel)
            notificationManager.notify(89, mBuilder.build())
        } else {
            var mBuilder = NotificationCompat.Builder(this@MyFCMService, "008")
                .setContentText(body)
                .setContentTitle(title)
                .setVibrate(longArrayOf(4000))
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLights(Color.WHITE, 3000, 1000)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
//                .setNumber(1)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(notifyPendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(89, mBuilder.build())
        }
    }
}