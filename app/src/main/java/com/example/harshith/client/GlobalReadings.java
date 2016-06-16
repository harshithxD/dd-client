package com.example.harshith.client;

import android.app.Application;

/**
 * Created by harshith on 16/6/16.
 */

public class GlobalReadings extends Application{
    private int[] flex = new int[5];
    private  int[] mpu = new int[6];

    public int[] getMpu() {
        return mpu;
    }

    public void setMpu(int[] mpu) {
        this.mpu = mpu;
    }

    public int[] getFlex() {
        return flex;
    }

    public void setFlex(int[] flex) {
        this.flex = flex;
    }
}
