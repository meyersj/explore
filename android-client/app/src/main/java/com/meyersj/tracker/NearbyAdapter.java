package com.meyersj.tracker;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class NearbyAdapter extends ArrayAdapter<NearbyBeacon> {

    private final String TAG = getClass().getCanonicalName();
    private final int MIN_COUNT_DISPLAY = 1;
    private HashMap<String, NearbyBeacon> beacons = new HashMap<>();
    private NearbyBeacon active = null;


    public NearbyAdapter(Context context, ArrayList<NearbyBeacon> result) {
        super(context, 0, result);
    }

    @Override
    public void clear() {
        beacons.clear();
        super.clear();
    }

    @Override
    public void add(NearbyBeacon result) {
        // check if beacon signature already exists
        if (beacons.containsKey(result.hash)) {
            // increment count and update signal strength
            NearbyBeacon beacon = beacons.get(result.hash);
            beacon.count++;
            beacon.rssi = result.rssi;
            // if beacon is not being displayed and minimum count is
            // exceeded then add that beacon to display list
            if (!beacon.displayed && beacon.count > MIN_COUNT_DISPLAY) {
                beacon.displayed = true;
                super.add(beacon);
            }
        }
        else {
            // if beacon signature does not exists then save it
            result.count++;
            beacons.put(result.hash, result);
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
        TextView hash = (TextView) view.findViewById(R.id.hash);
        count.setText(String.valueOf(beacon.count));
        rssi.setText(String.valueOf(beacon.rssi));
        String name = beacon.hash;
        if (name.length() > 60) {
            name = name.substring(0, 60) + "...";
        }
        hash.setText(name);
        if (active == beacon) {
            view.setBackground(getContext().getDrawable(R.color.selected));
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

    public void setActiveBeacon(NearbyBeacon beacon) {
        this.active = beacon;
        notifyDataSetChanged();
    }



}