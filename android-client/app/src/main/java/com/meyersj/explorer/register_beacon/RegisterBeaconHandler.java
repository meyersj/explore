package com.meyersj.explorer.register_beacon;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;


public class RegisterBeaconHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private final WeakReference<RegisterBeaconFragment> fragment;

    public RegisterBeaconHandler(RegisterBeaconFragment fragment) {
        this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message message){
        RegisterBeaconFragment fragment = this.fragment.get();
        if (fragment != null){
            fragment.update(message);
        }
    }

}
