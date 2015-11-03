package com.meyersj.explore.nearby;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.explore.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class NearbyAdapter extends ArrayAdapter<NearbyBeacon> {

    private final String TAG = getClass().getCanonicalName();
    private final int MIN_COUNT_DISPLAY = 1;
    private HashMap<String, NearbyBeacon> beacons = new HashMap<>();
    private ArrayList<NearbyBeacon> data;
    private ArrayList<NearbyBeacon> restoreList;
    private NearbyBeacon active = null;

    public NearbyAdapter(Context context, ArrayList<NearbyBeacon> list) {
        super(context, 0, list);
        data = list;
    }

    @Override
    public void clear() {
        beacons.clear();
        super.clear();
    }

    @Override
    public void add(NearbyBeacon result) {
        // check if beacon signature already exists
        if (beacons.containsKey(result.mac)) {
            // increment count and update signal strength
            NearbyBeacon beacon = beacons.get(result.mac);
            beacon.count++;
            beacon.rssi = result.rssi;
            // if beacon is not being displayed and minimum count is
            // exceeded then add that beacon to display list
            if (!beacon.displayed && beacon.count >= MIN_COUNT_DISPLAY) {
                beacon.displayed = true;
                if (active == null) {
                    super.add(beacon);
                }
            }
        }
        else {
            // if beacon signature does not exists then save it
            result.count++;
            beacons.put(result.mac, result);
        }
        // forces list to sort itself based on signal strength
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        NearbyBeacon beacon = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.beacon_list_view, parent, false);
        }
        TextView rssi = (TextView) view.findViewById(R.id.rssi);
        TextView count = (TextView) view.findViewById(R.id.count);
        TextView description = (TextView) view.findViewById(R.id.description);
        count.setText("#" + String.valueOf(beacon.count));
        rssi.setText(String.valueOf(beacon.rssi));
        description.setText(beacon.name);
        if (active == beacon) {
            view.setBackground(getContext().getDrawable(R.drawable.rounded_active));
        }
        else {
            view.setBackground(getContext().getDrawable(R.color.transparent));
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        this.setNotifyOnChange(false);
        this.sort(new Comparator<NearbyBeacon>() {
            @Override
            public int compare(NearbyBeacon lhs, NearbyBeacon rhs) {
                return rhs.rssi.compareTo(lhs.rssi);
            }
        });
        this.setNotifyOnChange(true);
        super.notifyDataSetChanged();
    }

    public void setActive(NearbyBeacon active) {
        this.active = active;
    }


    public int toggleActiveBeacon(NearbyBeacon beacon) {
        int ret;
        // new selection
        if (this.active == null) {
            this.active = beacon;
            activateBeacon();
            ret = 1;
        }
        // swap selection
        else if (this.active != beacon) {
            this.active = beacon;
            activateBeacon();
            ret = 0;
        }
        // undo selection
        else {
            this.active = null;
            deactivateBeacon();
            ret = -1;
        }
        notifyDataSetChanged();
        return ret;
    }

    private void activateBeacon() {
        //data.remove(active);
        restoreList = new ArrayList<>(data);
        //restoreList.add(0, active);
        super.clear();
        super.add(active);
    }

    private void deactivateBeacon() {
        super.clear();
        for(NearbyBeacon beacon: restoreList) {
            super.add(beacon);
        }
    }
}