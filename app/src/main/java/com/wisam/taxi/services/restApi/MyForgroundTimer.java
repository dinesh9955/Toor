package com.wisam.taxi.services.restApi;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.wisam.taxi.R;
import com.wisam.taxi.base.WisamTaxiApplication;
import com.wisam.taxi.common.RetroRepository;
import com.wisam.taxi.interfaces.ApiInterface;
import com.wisam.taxi.model.driverRequest.DriverSelectRoutesRequest;
import com.wisam.taxi.model.driverResponse.selectActiveRoute.SelectActiveRouteResponse;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyForgroundTimer extends Service {
    String actions = "restartservice";
    SharedPreferences mySharedPreferences;
    Boolean isSuccess = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Your are active in a route")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mySharedPreferences = getApplicationContext().getSharedPreferences("com.wisam.taxi", Activity.MODE_PRIVATE);
        startTimer(intent.getLongExtra("timemili",900000));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSuccess)
        {
            actions = "nostart";
        }else{
            actions = "restartservice";
        }
        Log.i("TimerForgroundService", "Timer... :- Finished " + actions);

        if (actions.equals("restartservice"))
        {
            if (mySharedPreferences.getString("stopservice","").equals("self"))
            {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(actions);
                broadcastIntent.setClass(this, Restarter.class);
                this.sendBroadcast(broadcastIntent);
            }
        }
        stoptimertask();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private CountDownTimer timer;

    public void startTimer(long militime) {
        timer = new CountDownTimer(militime, 1000) {
            public void onTick(long millisUntilFinished) {

                String seconds = "" + millisUntilFinished % 60000 / 1000;
                String mins = "" + millisUntilFinished / 60000;


                if (Integer.valueOf(seconds) <= 9)
                    seconds = "0" + seconds;

                if (Integer.valueOf(mins) <= 9)
                    mins = "0" + mins;

                mySharedPreferences.edit().putLong("driver_time",millisUntilFinished).apply();

                Log.i("TimerForgroundService", "Starting timer... :- " + mins + " : " + seconds);
            }

            @Override
            public void onFinish() {
                selectActiveroute();
                mySharedPreferences.edit().putLong("driver_time",0).apply();
                Log.i("TimerForgroundService", "Timer... :- Finished");

            }
        }.start();
    }

    void selectActiveroute()
    {
        Log.d("TimerForgroundService", "Under Api block  :- "+mySharedPreferences.getString("auth","")+" actions :- "+actions);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://appgrowthcompany.com:3008")
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        DriverSelectRoutesRequest request = new DriverSelectRoutesRequest("");

        Call<SelectActiveRouteResponse> call = service.selectActiveRouteDriver(mySharedPreferences.getString("auth",""),
                mySharedPreferences.getString("mylang","en"),request);

        call.enqueue(new Callback<SelectActiveRouteResponse>() {
            @Override
            public void onResponse(Call<SelectActiveRouteResponse> call, Response<SelectActiveRouteResponse> response)
            {
                if (response.body().getResponse().getSuccess())
                {
                    Log.e("TimerForgroundService","Success");

                    mySharedPreferences.edit().putString("driver_isactive","false").apply();
                    mySharedPreferences.edit().putString("stopservice","self").apply();
                    isSuccess =true;

                    stopForeground(true);
                    stopSelf();

                    mySharedPreferences.edit().putString("stopservice","self").apply();
                    actions = "nostart";

                }else
                {
                    Log.e("TimerForgroundService",""+response.body().getResponse().getMessage());
                    selectActiveroute();
                }
            }
            @Override
            public void onFailure(Call<SelectActiveRouteResponse> call, Throwable t) {
                Log.e("TimerForgroundService","failed");
                selectActiveroute();
            }
        });

    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
