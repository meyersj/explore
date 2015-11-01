package com.meyersj.explore.communicate;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.meyersj.explore.explore.ExploreFragment;
import com.meyersj.explore.map.LocationMapFragment;
import com.meyersj.explore.register_beacon.RegisterBeaconFragment;

import java.lang.ref.WeakReference;


public class ResponseHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private final WeakReference<Fragment> fragment;

    public ResponseHandler(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message message){
        Fragment fragment = this.fragment.get();
        if (fragment != null){
            if (fragment instanceof ExploreFragment) {
                ((ExploreFragment) fragment).update(message);
            }
            else if (fragment instanceof RegisterBeaconFragment) {
                ((RegisterBeaconFragment) fragment).update(message);
            }
            else if (fragment instanceof LocationMapFragment) {
                ((LocationMapFragment) fragment).update(message);
            }
        }
    }

}
