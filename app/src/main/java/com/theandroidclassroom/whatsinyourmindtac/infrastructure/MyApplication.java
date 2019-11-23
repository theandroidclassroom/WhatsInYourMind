package com.theandroidclassroom.whatsinyourmindtac.infrastructure;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    public static MyApplication mInstances;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstances = this;
    }
    public static MyApplication getInstance(){
        return mInstances;
    }
    public static Context getContext(){
        return mInstances.getApplicationContext();
    }
}
