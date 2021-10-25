package com.wisam.taxi.socket;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.wisam.taxi.interfaces.SocketListener;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.util.List;


public class SocketIOService extends Service {
    private SocketListener socketListener;
    private Boolean appConnectedToService;
    private Socket mSocket;
    private boolean serviceBinded = false;
    private final LocalBinder mBinder = new LocalBinder();

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
        public SocketIOService getService(){
            return SocketIOService.this;
        }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initializeSocket() {
        try{
            Log.e("SocketIOService--initi", "try");

            IO.Options options = new IO.Options();
            options.forceNew = true;
            mSocket = IO.socket(SocketUrls.SOCKET_URL,options);
        }
        catch (Exception e){
            Log.e("Error", "Exception in socket creation");
            Log.e("SocketIOService", "catch");

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
                    Intent intent = new Intent(SocketUrls.socketConnection);
                    intent.putExtra("connectionStatus", true);
                    broadcastEvent(intent);
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
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}


