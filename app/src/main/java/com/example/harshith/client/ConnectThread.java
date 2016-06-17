package com.example.harshith.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by harshith on 16/6/16.
 */

public class ConnectThread extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private String address = null;
    private UUID uuid = null;
    public int isConnected;
    Handler handler;
    ReceiveDataThread receiveDataThread = null;

    public ConnectThread(String address,UUID uuid,Handler handler){
        this.address = address;
        this.uuid = uuid;
        this.handler = handler;
    }

    @Override
    public void run() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try {
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            isConnected = Constants.CONNECTION_STATUS_OK;
//            receiveDataThread = new ReceiveDataThread(bluetoothSocket,handler);


        }
        catch (IOException e) {
            isConnected = Constants.CONNECTION_STATUS_NOT_CONNECTED;
        }

        Message message = handler.obtainMessage(Constants.CONNECTION_STATUS,isConnected);
        handler.handleMessage(message);
    }
}
