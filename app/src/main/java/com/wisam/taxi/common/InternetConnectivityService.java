package com.wisam.taxi.common;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class InternetConnectivityService extends Service {

    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();

        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                    //check internet connection
                    if (!ConnectionHelper.isConnectedOrConnecting(context)) {
                        if (context != null) {
                            boolean show = false;
                            if (ConnectionHelper.lastNoConnectionTs == -1) {
                                show = true;
                                ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();

                            } else {
                                if (System.currentTimeMillis() - ConnectionHelper.lastNoConnectionTs > 1000) {
                                    show = true;
                                    ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                                }
                            }

                            if (show && ConnectionHelper.isOnline) {

                                ConnectionHelper.isOnline = false;
                                doSendBroadcast("lost");
                            }
                        }
                    } else {
                        doSendBroadcast("true");
                        ConnectionHelper.isOnline = true;
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void doSendBroadcast(String message) {
        Intent it = new Intent("EVENT_SNACKBAR");
        if (!TextUtils.isEmpty(message))
            it.putExtra("status", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(it);
    }
}

