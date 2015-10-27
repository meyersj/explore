package com.meyersj.explorer.register_beacon;

import android.content.Context;

import com.meyersj.explorer.NearbyAdapter;
import com.meyersj.explorer.NearbyBeacon;

import java.util.ArrayList;


public class RegisterBeaconAdapter extends NearbyAdapter {
    public RegisterBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
