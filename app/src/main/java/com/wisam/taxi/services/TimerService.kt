package com.wisam.taxi.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.wisam.taxi.base.BaseActivity
import com.wisam.taxi.view.driverModule.chooseActive.ChooseActiveActivity


class TimerService : Service()
{
    companion object
    {
        val COUNTDOWN_BR = "com.wisam.taxi.services.countdown_br"
        var bi = Intent(COUNTDOWN_BR)
        var cdt: CountDownTimer? = null
    }

    override fun onCreate() {
        super.onCreate()
        cdt = object : CountDownTimer(900000, 1000) {
            override fun onTick(millisUntilFinished: Long)
            {
                var handler = Handler()

                kotlin.run {
                    handler.post(object : Runnable{
                        override fun run()
                        {
                            var seconds = ""+millisUntilFinished % 60000 / 1000
                            var minutes = ""+millisUntilFinished / 60000

                            if (seconds.toInt() <= 9)
                                seconds = "0$seconds"

                            if (minutes.toInt() <= 9)
                                minutes = "0$minutes"

                            Log.i("TimerService", "Starting timer... :- $minutes : $seconds")
//
//                            if (minutes.toInt()<=0 && seconds.toInt() == 10)
//                            {
//                                Toast.makeText(this@TimerService,"You will offline after 10 seconds",Toast.LENGTH_SHORT).show()
//                            }

                            bi.putExtra("countdown", millisUntilFinished)
                            sendBroadcast(bi)
                        }
                    })
                }
            }
            override fun onFinish() {
                Log.i("TimerService", "Timer finished")
                Toast.makeText(this@TimerService,"You are offline now",Toast.LENGTH_SHORT).show()
            }
        }
        cdt!!.start()

    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        val restartServiceTask = Intent(applicationContext, this.javaClass)
        restartServiceTask.setPackage(packageName)
        val restartPendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceTask,
            PendingIntent.FLAG_ONE_SHOT
        )
        val myAlarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        myAlarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] = restartPendingIntent

        Log.i("TimerService", "onStartCommand")

        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        cdt!!.cancel()
        Log.i("TimerService", "Timer cancelled")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("TimerService", "onStartCommand")
        return START_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}