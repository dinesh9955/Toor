package com.wisam.taxi.socket;

import android.content.*;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.wisam.taxi.interfaces.SocketListener;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class AppSocketListener implements SocketListener {
    private static AppSocketListener sharedInstance;
    private SocketIOService socketServiceInterface;
    public SocketListener activeSocketListener;
    private Context mContext;

    public void setActiveSocketListener(SocketListener activeSocketListener) {
        try {
            this.activeSocketListener = activeSocketListener;
            if (socketServiceInterface != null && socketServiceInterface.isSocketConnected()) {
                onSocketConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static AppSocketListener getInstance() {
        if (sharedInstance == null)
        {
            sharedInstance = new AppSocketListener();
        }
        return sharedInstance;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                socketServiceInterface = ((SocketIOService.LocalBinder) service).getService();
                socketServiceInterface.setServiceBinded(true);
                socketServiceInterface.setSocketListener(sharedInstance);
                if (socketServiceInterface.isSocketConnected()) {
                    onSocketConnected();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                socketServiceInterface.setServiceBinded(false);
                socketServiceInterface = null;
                onSocketDisconnected();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    public void initialize(Context applicationContext) {
        try {
            if (applicationContext != null) {
                Log.e("initialize","try");

                mContext = applicationContext;
                Intent intent = new Intent(applicationContext, SocketIOService.class);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                {

                    Intent intent1 = new Intent(applicationContext, ForegroundService.class);
                    intent.setAction(ForegroundService.ACTION_START_FOREGROUND_SERVICE);
                    applicationContext.startService(intent1);

                    //  applicationContext.startForegroundService(intent);
                }
                else
                {

                    applicationContext.startService(intent);
                }
                applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                LocalBroadcastManager.getInstance(applicationContext).
                        registerReceiver(socketConnectionReceiver, new IntentFilter(SocketUrls.
                                socketConnection));
            }
        } catch (Exception e) {
            Log.e("initialize","catch");

            e.printStackTrace();
        }


    }

    private BroadcastReceiver socketConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean connected = intent.getBooleanExtra("connectionStatus", false);
                if (connected) {
                    Log.e("AppSocketListener", "Socket connected");
                    onSocketConnected();
                } else {
                    onSocketDisconnected();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private BroadcastReceiver connectionFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast toast = Toast.
//                    makeText(new App().getContext(), "Please check your network connection",
//                            Toast.LENGTH_SHORT);
//            toast.show();
        }
    };


    public void destroy() {
        try {
            socketServiceInterface.setServiceBinded(false);
            mContext.unbindService(serviceConnection);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(socketConnectionReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSocketConnected() {
        try {
            if (activeSocketListener != null) {
                activeSocketListener.onSocketConnected();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onSocketDisconnected() {
        try {
            if (activeSocketListener != null) {
                activeSocketListener.onSocketDisconnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSocketConnectionError() {
        if (activeSocketListener != null) {
            activeSocketListener.onSocketConnectionError();
        }
    }

    @Override
    public void onSocketConnectionTimeOut() {
        try {
            if (activeSocketListener != null) {
                activeSocketListener.onSocketConnectionTimeOut();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void addOnHandler(String event, Emitter.Listener listener) {
        try {
            socketServiceInterface.addOnHandler(event, listener);
            Log.d("listenMessage",event);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("listenMessage",ex.toString());

        }

    }

    public void emit(String event, Object[] args, Ack ack) {
        try {
            socketServiceInterface.emit(event, args, ack);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void emit(String event, Object... args) {
        try {
            if (!event.toString().equals("updateLatLong") && !event.toString().equals("tracking")) {
                Log.e("EMIT  : ", event + "   " + args[0]);
            }
            if (socketServiceInterface != null) {
                socketServiceInterface.emit(event, args[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Log.e("EVENT emit ",event+"   "+args[0].toString());
    }

    public void connect() {
        try {
            if (socketServiceInterface != null) {
                socketServiceInterface.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void disconnect() {
        try {
            socketServiceInterface.disconnect();
//            Log.d("listenMessage_socket","socket disconnetct");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void off(String event, Emitter.Listener ongetChatList) {
        try {
            if (socketServiceInterface != null) {
                socketServiceInterface.off(event, ongetChatList);
            }
//            Log.d("listenMessage_socket",event);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void off(String event) {
        try {
            if (socketServiceInterface != null) {
                socketServiceInterface.off(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isSocketConnected() {
        try {
            if (socketServiceInterface == null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return socketServiceInterface.isSocketConnected();
    }

    public void setAppConnectedToService(Boolean status) {
        try {
            if (socketServiceInterface != null) {
                socketServiceInterface.setAppConnectedToService(status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void restartSocket() {
        try {
            if (socketServiceInterface != null) {
                socketServiceInterface.restartSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void signOutUser() {
        try {
            AppSocketListener.getInstance().disconnect();
            AppSocketListener.getInstance().connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
