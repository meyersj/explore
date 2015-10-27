package com.meyersj.tracker.explore;

import android.content.Context;

import com.meyersj.tracker.NearbyAdapter;
import com.meyersj.tracker.NearbyBeacon;

import java.util.ArrayList;


public class ExploreBeaconAdapter extends NearbyAdapter {

    public ExploreBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
