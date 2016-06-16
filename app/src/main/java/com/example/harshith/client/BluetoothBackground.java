package com.example.harshith.client;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.jar.Manifest;

import static android.content.ContentValues.TAG;


/**
 * Created by harshith on 15/6/16.
 */

public class BluetoothBackground extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;




    private final class ServiceHandler extends Handler {
        BluetoothDevice bluetoothDevice;
        BluetoothAdapter bluetoothAdapter;
        BluetoothSocket bluetoothSocket;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        StringBuilder stringBuilder = new StringBuilder();
        final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            String address;

            address = (String) msg.obj;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
//                        Toast.makeText(getApplicationContext(),"Couldn't make a connection",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (IOException e){
//                Toast.makeText(getApplicationContext(),"Couldn't make a connection",Toast.LENGTH_SHORT).show();
            }


            try {
                InputStream tempIn = null;
                OutputStream tempOut = null;
                try {
                    tempIn = bluetoothSocket.getInputStream();
                    tempOut = bluetoothSocket.getOutputStream();
                }
                catch (IOException e) {
                    Toast.makeText(getApplicationContext(),"Couldn't get streaming",Toast.LENGTH_SHORT).show();
                }

                inputStream = tempIn;
                outputStream = tempOut;
            }
            catch (Exception e) {
//                Toast.makeText(getApplicationContext(),"Unknown Exception occured",Toast.LENGTH_SHORT).show();
            }

            final GlobalReadings globalReadings  = (GlobalReadings) getApplicationContext();
            byte[] buffer = new byte[64];
            int bytes = -1;

//            Intent intent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            try {
//                PackageManager pm = getBaseContext().getPackageManager();
//
//                final ResolveInfo mInfo = pm.resolveActivity(intent2, 0);
//
//                Intent intent = new Intent();
//                intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
//                intent.setAction(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//
//            } catch (Exception e){ Log.i(TAG, "Unable to launch camera: " + e); }
//            try {
//                Thread.sleep(5000);
//            }catch (InterruptedException e) {
//
//            }
//            for(int i = 0; i!= 5; i++) {
//                Intent intent1 = new Intent("android.intent.action.CAMERA_BUTTON");
//                intent1.putExtra("android.intent.extra.KEY_EVENT",new KeyEvent(0, KeyEvent.KEYCODE_CAMERA));
//                sendOrderedBroadcast(intent1, android.Manifest.permission.CAMERA);
//            }

            while (true) {
                try {

                    bytes = inputStream.read(buffer);
                    String message = new String(buffer,0,bytes);
                    stringBuilder.append(message);
                    int endOfLineIndex = stringBuilder.indexOf("~");
                    if (endOfLineIndex > 0) {
                        int startOfLineIndex = stringBuilder.indexOf("#");
                        if (startOfLineIndex > endOfLineIndex || startOfLineIndex == -1) {
                            startOfLineIndex = 0;
                        }
                        else if (startOfLineIndex == 0) {
                            startOfLineIndex = 1;
                        }
                        String dataIn = stringBuilder.substring(startOfLineIndex, endOfLineIndex);

                        String[] readingStrings = dataIn.split("\\+");
                        int[] readings = new int[readingStrings.length];
                        for (int i = 0; i != readingStrings.length; i++) {
                            try {
                                readings[i] = Integer.valueOf(readingStrings[i]);
                            } catch (NumberFormatException e) {

                            }
                            catch (NullPointerException e) {

                            }
                        }
                        String testConvert = "";
                        for (int reading : readings) {
                            testConvert += " " + reading;
                        }
                        L.m(testConvert);
                        stringBuilder.delete(0, endOfLineIndex + 2);

                        int[] flex = new int[5];
                        for (int i = 0; i != 5; i++) {
                            try {
                                flex[i] = readings[i];
                            }
                            catch (ArrayIndexOutOfBoundsException e) {

                            }
                        }

                        int[] mpu = new int[6];
                        for (int i = 0; i != 6; i++) {
                            try {
                                mpu[i] = readings[5 + i];
                            }
                            catch (ArrayIndexOutOfBoundsException e) {

                            }
                        }

                        globalReadings.setFlex(flex);
                        globalReadings.setFlex(mpu);
                    }
                }
                catch (IOException e){
//                    Toast.makeText(getApplicationContext(),"Could not read data",Toast.LENGTH_SHORT).show();
                }
            }
//            stopSelf(msg.arg1);
        }
    }


    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private StringBuilder stringBuilder = new StringBuilder();
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    final int handlerState = 0;





    @Override
    public void onCreate() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Your device doesn't support bluetooth",Toast.LENGTH_SHORT).show();
            stopSelf();
        }


        HandlerThread thread = new HandlerThread("BluetoothReceiver",Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }




    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        Toast.makeText(getApplicationContext(),"Starting the bluetooth service",Toast.LENGTH_SHORT).show();

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        mServiceHandler.handleMessage(msg);

        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    @Override
    public void onDestroy() {
        if(bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Couldn't close the connection",Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(getApplicationContext(),"Service Completed",Toast.LENGTH_SHORT).show();
    }
}
