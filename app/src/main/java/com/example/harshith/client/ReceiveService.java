package com.example.harshith.client;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by harshith on 16/6/16.
 */

public class ReceiveService extends Service {
    ConnectThread connectThread;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler handler;

    @Override
    public void onCreate() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(message.what == Constants.CONNECTION_STATUS) {
                    Toast.makeText(getApplicationContext(),"Message from Connection Status" + (String) ("" + message.obj.toString()),Toast.LENGTH_SHORT).show();
                }
                else if (message.what == Constants.READ_STATUS) {

                }
            }
        };
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        String address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        connectThread = new ConnectThread(address,uuid,handler) {

        };
        connectThread.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){

    }
}
