package com.meyersj.explorer.explore;

import android.content.Context;

import com.meyersj.explorer.NearbyAdapter;
import com.meyersj.explorer.NearbyBeacon;

import java.util.ArrayList;


public class ExploreBeaconAdapter extends NearbyAdapter {

    public ExploreBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
