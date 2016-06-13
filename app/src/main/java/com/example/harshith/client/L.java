package com.example.harshith.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by harshith on 13/6/16.
 */

public class L {
    public static void m(String message){
        Log.d("OTH",message);
    }
    public static void s(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
