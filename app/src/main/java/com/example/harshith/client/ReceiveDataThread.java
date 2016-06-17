package com.example.harshith.client;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by harshith on 16/6/16.
 */

public class ReceiveDataThread extends Thread {
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    Handler handler;
    public int readStatus;
    public ReceiveDataThread(BluetoothSocket bluetoothSocket, Handler handler) {
        this.bluetoothSocket = bluetoothSocket;
        this.handler = handler;
    }

    @Override
    public void run(){
        try {
            InputStream tempIn = null;
            tempIn = bluetoothSocket.getInputStream();
            inputStream = tempIn;

            byte[] buffer = new byte[64];
            int bytes = -1;

            while(true) {
                bytes = inputStream.read(buffer);
                String readMessage = new String(buffer,0,bytes);
                L.m(readMessage);
            }
        }
        catch (IOException e){
            readStatus = Constants.CONNECTION_STATUS_NOT_CONNECTED;
        }
    }
}
