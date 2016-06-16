package com.example.harshith.client;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



/**
 * Created by harshith on 15/6/16.
 */

public class BluetoothBackground extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                InputStream tempIn = null;
                OutputStream tempOut = null;
                try {
                    tempIn = bluetoothSocket.getInputStream();
                    tempOut = bluetoothSocket.getOutputStream();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                inputStream = tempIn;
                outputStream = tempOut;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                byte[] buffer = new byte[64];
                int bytes = -1;
                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        String readMessage = new String(buffer,0,bytes);

                    }
                    catch (IOException e){
                        Toast.makeText(getApplicationContext(),"Could not read data",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            stopSelf(msg.arg1);
        }
    }


    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private StringBuilder stringBuilder = new StringBuilder();
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    private Handler handler;




    @Override
    public void onCreate() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final GlobalReadings globalReadings = (GlobalReadings) getApplicationContext();
        if(bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Your device doesn't support bluetooth",Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                stringBuilder.append(message);
                int endOfLineIndex = stringBuilder.indexOf("~");
                if (endOfLineIndex > 0){
                    int startOfLineIndex = stringBuilder.indexOf("#");
                    if(startOfLineIndex > endOfLineIndex || startOfLineIndex == -1){
                        startOfLineIndex = 0;
                    }
                    String dataIn = stringBuilder.substring(startOfLineIndex,endOfLineIndex);
                    String[] readingStrings = dataIn.split("\\+");
                    int[] readings = new int[readingStrings.length];
                    for(int i = 0; i != readingStrings.length;i++) {
                        try {
                            readings[i] = Integer.getInteger(readingStrings[i]);
                        }
                        catch (NumberFormatException e ) {

                        }
                    }
                    String testConvert = "";
                    for (int reading : readings){
                        testConvert+=" " + reading;
                    }
                    L.m(testConvert);
                    stringBuilder.delete(0,endOfLineIndex + 2);

                    int[] flex = new int[5];
                    for(int i = 0;i != 5;i++){
                        flex[i] = readings[i];
                    }

                    int[] mpu = new int[6];
                    for(int i = 0;i != 6;i++) {
                        mpu[i] = readings[5 + i];
                    }

                    globalReadings.setFlex(flex);
                    globalReadings.setFlex(mpu);
                }
            }
        };

        HandlerThread thread = new HandlerThread("BluetoothReceiver", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        Toast.makeText(getApplicationContext(),"Starting the bluetooth service",Toast.LENGTH_SHORT).show();

        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try {
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            try {
                bluetoothSocket.connect();
            }
            catch (IOException e) {
                try {
                    bluetoothSocket.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }


        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.handleMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),"Service Completed",Toast.LENGTH_SHORT).show();
    }
}
