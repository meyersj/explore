package com.meyersj.explore.explore;

import android.content.Context;

import com.meyersj.explore.nearby.NearbyAdapter;
import com.meyersj.explore.nearby.NearbyBeacon;

import java.util.ArrayList;


public class ExploreBeaconAdapter extends NearbyAdapter {

    public ExploreBeaconAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, result);
    }
}
