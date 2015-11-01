package com.meyersj.explore.communicate;

import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.meyersj.explore.background.ScannerService;
import com.meyersj.explore.explore.ExploreFragment;
import com.meyersj.explore.map.LocationMapFragment;

import java.lang.ref.WeakReference;


public class ResponseHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private final WeakReference<Fragment> fragment;
    private final WeakReference<Service> service;

    public ResponseHandler(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
        this.service = null;
    }

    public ResponseHandler(Service service) {
        this.service = new WeakReference<>(service);
        this.fragment = null;
    }

    @Override
    public void handleMessage(Message message){
        if (service != null) {
            Service service = this.service.get();
            if(service != null) {
                ((ScannerService) service).update(message);
            }
            return;
        }
        Fragment fragment = this.fragment.get();
        if (fragment != null) {
            if (fragment instanceof ExploreFragment) {
                ((ExploreFragment) fragment).update(message);
            }
            else if (fragment instanceof LocationMapFragment) {
                ((LocationMapFragment)fragment).update(message);
            }
        }
    }
}
