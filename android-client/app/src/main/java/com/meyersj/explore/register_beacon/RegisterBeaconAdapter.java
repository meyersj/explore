package com.meyersj.explore.register_beacon;

import android.content.Context;

import com.meyersj.explore.nearby.NearbyAdapter;
import com.meyersj.explore.nearby.NearbyBeacon;

import java.util.ArrayList;


public class RegisterBeaconAdapter extends NearbyAdapter {
    public RegisterBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
