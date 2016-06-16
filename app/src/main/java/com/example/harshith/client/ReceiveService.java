package com.example.harshith.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by harshith on 16/6/16.
 */

public class ReceiveService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
