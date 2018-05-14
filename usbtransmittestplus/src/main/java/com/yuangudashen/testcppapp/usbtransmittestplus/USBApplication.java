package com.yuangudashen.testcppapp.usbtransmittestplus;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/10/25/025.
 */

public class USBApplication extends Application{

    private static USBApplication instance;

    public static Context applicationContext;
    public static ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();

        init();

    }

    private void init() {
        applicationContext = this;
        executorService = Executors.newCachedThreadPool();
    }

    public static USBApplication getInstance() {
        return instance;
    }
}
