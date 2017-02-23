package com.umer.towtruckdriver.utils;

import android.app.Application;

import com.umer.towtruckdriver.receiver.ConnectivityReceiver;

/**
 * Created by Mansoor Ali on 10/6/2016.
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}