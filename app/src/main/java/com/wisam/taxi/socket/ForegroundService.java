package com.wisam.taxi.socket;



import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.wisam.taxi.R;
import com.wisam.taxi.interfaces.SocketListener;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.util.ArrayList;
import java.util.List;

public class ForegroundService extends Service {
    private Socket mSocket;
    private boolean serviceBinded = false;
    private SocketListener socketListener;
    private Boolean appConnectedToService;

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSE = "ACTION_PAUSE";

    public static final String ACTION_PLAY = "ACTION_PLAY";

    private final LocalBinder mBinder = new LocalBinder();
    public ForegroundService() {
    }

    public void setAppConnectedToService(Boolean appConnectedToService) {
        try
        {
            this.appConnectedToService = appConnectedToService;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void setSocketListener(SocketListener socketListener) {
        try
        {
            this.socketListener = socketListener;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public class LocalBinder extends Binder {
        public ForegroundService getService(){
            return ForegroundService.this;
        }
    }






    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();

            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_PLAY:
                    Toast.makeText(getApplicationContext(), "You click Play button.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_PAUSE:
                    Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /* Used to build and start foreground service. */
    private void startForegroundService()
    {
        Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.");

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create notification builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Make notification show big text.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("Music player implemented by foreground service.");
        bigTextStyle.bigText("Android foreground service is a android service which can run in foreground always, it can be controlled by user via notification.");
        // Set big text style.
        builder.setStyle(bigTextStyle);

        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        builder.setLargeIcon(largeIconBitmap);
        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX);
        // Make head-up notification.
        builder.setFullScreenIntent(pendingIntent, true);

        // Add Play button intent in notification.
        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);
        builder.addAction(playAction);

        // Add Pause button intent in notification.
        Intent pauseIntent = new Intent(this, ForegroundService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPrevIntent);
        builder.addAction(prevAction);

        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        startForeground(1, notification);
    }

    private void stopForegroundService()
    {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }



    public void setServiceBinded(boolean serviceBinded) {
        try
        {
            this.serviceBinded = serviceBinded;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try
        {Log.e("SocketIOServiceonCreate", "try");
            initializeSocket();
            addSocketHandlers();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try
        {
            closeSocketSession();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return serviceBinded;
    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }

    private void initializeSocket() {
        try{
            Log.e("SocketIOS", "try");

            IO.Options options = new IO.Options();
            options.forceNew = true;
            mSocket = IO.socket(SocketUrls.SOCKET_URL,options);
        }
        catch (Exception e){
            Log.e("Error", "Exception in socket creation");
            throw new RuntimeException(e);
        }
    }

    private void closeSocketSession(){
        mSocket.disconnect();
        mSocket.off();
    }
    private void addSocketHandlers(){
        try
        {
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("EVENT_CONNECT ","Connected");

                    ArrayList<String> strings = new ArrayList<String>();
                    strings.add("5e01b7709ae7ea0791f02448");



                    Intent intent = new Intent(SocketUrls.socketConnection);
                    intent.putExtra("connectionStatus", true);
                    broadcastEvent(intent);

//                    emit("sendMessage", sendMessageRequest);
//                    emit("addUser", "5daee1a7aab0d87f7e20eed5");
                }
            });

//            mSocket.on("sendMessage", new Emitter.Listener() {
            mSocket.on("sendBooking", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
//                    Log.e("sendMessage ",args[0].toString());
                    Log.e("sendBooking ",args.toString());
//                    Intent intent = new Intent(SocketUrls.socketConnection);
//                    intent.putExtra("connectionStatus", false);
//                    broadcastEvent(intent);
                }
            });

            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("EVENT_DISCONNECT ",args[0].toString());
                    Intent intent = new Intent(SocketUrls.socketConnection);
                    intent.putExtra("connectionStatus", false);
                    broadcastEvent(intent);
                }
            });


            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("EVENT_CONNECT_ERROR ",args[0].toString());
                    Intent intent = new Intent(SocketUrls.connectionFailure);
                    broadcastEvent(intent);
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("EVENT_CONNECT_TIMEOUT ",args[0].toString());
                    Intent intent = new Intent(SocketUrls.connectionFailure);
                    broadcastEvent(intent);
                }
            });
            mSocket.connect();
        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    public void emit(String event, Object[] args, Ack ack){
        try
        {
            mSocket.emit(event, args, ack);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    public void emit (String event,Object... args) {
        try {
            mSocket.emit(event, args,null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addOnHandler(String event, Emitter.Listener listener){
        try {
            mSocket.on(event, listener);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void connect(){
        try {
            mSocket.connect();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void disconnect(){
        try {
            mSocket.disconnect();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void restartSocket(){
        try {
            mSocket.off();
            mSocket.disconnect();
            addSocketHandlers();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void off(String event, Emitter.Listener ongetChatList){
        try {
            mSocket.off(event,ongetChatList);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void off(String event){
        try {
            mSocket.off(event);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void broadcastEvent(Intent intent){
        try {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public boolean isSocketConnected(){
        if (mSocket == null){
            return false;
        }
        return mSocket.connected();
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        @SuppressLint({"NewApi", "LocalSuppress"}) ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
