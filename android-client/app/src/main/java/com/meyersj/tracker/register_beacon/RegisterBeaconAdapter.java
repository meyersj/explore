package com.meyersj.tracker.register_beacon;

import android.content.Context;

import com.meyersj.tracker.NearbyAdapter;
import com.meyersj.tracker.NearbyBeacon;

import java.util.ArrayList;


public class RegisterBeaconAdapter extends NearbyAdapter {
    public RegisterBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
