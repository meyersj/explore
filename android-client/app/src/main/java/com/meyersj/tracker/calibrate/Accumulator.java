package com.meyersj.tracker.calibrate;

import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.meyersj.tracker.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jeff on 10/23/15.
 */
public class Accumulator {


    private final String TAG = getClass().getCanonicalName();
    private HashMap<String, Beacon> data;

    public Accumulator() {
        data = new HashMap<>();
    }

    public void add(ScanResult result) {
        Integer rssi = result.getRssi();
        String hexString = Utils.getHexString(result.getScanRecord().getBytes());
        Log.d(TAG, String.valueOf(hexString.length()) + " " + String.valueOf(rssi) + " " + hexString);
        Beacon beacon = data.get(hexString);
        if(beacon == null) {
            beacon = new Beacon(hexString, result);
            data.put(hexString, beacon);
        }
        beacon.add(rssi);
    }

    public void logData() {
        for (Beacon beacon : data.values()) {
            //ScanResult result = value.getResult();
            Log.d(TAG, beacon.hash + " " + String.valueOf(beacon.getAverage()));
        }
    }


    public class Beacon {

        private List<Integer> broadcasts;
        public String hash;
        private ScanResult result;

        public Beacon(String hash, ScanResult result) {
            broadcasts = new ArrayList<Integer>();
            this.result = result;
            this.hash = hash;
        }

        public void add(int rssi) {
            broadcasts.add(rssi);
        }

        public String getAverage() {
            Integer sum = 0;
            for(Integer i: broadcasts) {
                sum += i;
            }
            return String.valueOf(broadcasts.size()) + " " + String.valueOf(sum / broadcasts.size());
        }

        public ScanResult getResult() {
            return result;
        }
    }

}
