package com.example.harshith.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.UUID;

public class ReceiveActivity extends AppCompatActivity {


    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    TextView sensor1,sensor2,sensor3;


    public String[] sarr = new String[3];


    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private StringBuilder stringBuilder = new StringBuilder();
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    Handler bluetoothIn;
    final int handlerState = 0;
    private ConnectedThread connectedThread;
    private StringBuilder recDataString = new StringBuilder();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        sarr[0] = "s1v";
        sarr[1] = "s2v";
        sarr[2] = "s3v";

        arrayAdapter = new ArrayAdapter<String>(getBaseContext(),R.layout.device_name);
        listView = (ListView) findViewById(R.id.valuesList);
        listView.setAdapter(arrayAdapter);

        sensor1 = (TextView) findViewById(R.id.sensor1);
        sensor2 = (TextView) findViewById(R.id.sensor2);
        sensor3 = (TextView) findViewById(R.id.sensor3);

        bluetoothIn = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(1, endOfLineIndex - 1);    // extract string
                        L.m(dataInPrint);
                        String[] readingStrings = dataInPrint.split("\\+");
                        float[] readings = new float[readingStrings.length];
                        for (int i = 0; i < readingStrings.length; i++){
                            readings[i] = Float.valueOf(readingStrings[i]);
                        }
                        arrayAdapter.add(readMessage.replace('+',' '));
                        recDataString.delete(0, endOfLineIndex);
                    }
                }
            }
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();


    }




    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            bluetoothSocket = createBluetoothSocket(device);
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(),"Could not create socket",Toast.LENGTH_SHORT).show();
        }

        try {
            bluetoothSocket.connect();
        }
        catch (IOException e) {
            try{
                bluetoothSocket.close();
            }
            catch (IOException e1){
                Toast.makeText(getBaseContext(),"Could not close socket",Toast.LENGTH_SHORT).show();
            }
        }
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }



    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(arrayAdapter.getCount() - 1);
            }
        });
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createInsecureRfcommSocketToServiceRecord(uuid);
    }



    @Override
    protected void onPause() {
        super.onPause();
        try {
            bluetoothSocket.close();
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(),"Could not close socket while pausing",Toast.LENGTH_SHORT).show();
        }
    }




    private void checkBTState() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(this,"This device doesn't support bluetooth.",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent,1);
            }
        }
    }





    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e){
                Toast.makeText(getBaseContext(),"ERROR - Could not acquire input/output stream from socket",Toast.LENGTH_SHORT).show();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes = -1;
            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer,0,bytes);
                    bluetoothIn.obtainMessage(handlerState,bytes,-1,readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] buffer = input.getBytes();
            try {
                outputStream.write(buffer);
            }
            catch (IOException e){
                Toast.makeText(getBaseContext(),"Connection Failure",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
