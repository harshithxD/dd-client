package com.example.harshith.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Created by harshith on 16/6/16.
 */

public class ConnectThread extends Thread {
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;

    public ConnectThread(){
        super();
    }

    @Override
    public void run() {

    }
}
