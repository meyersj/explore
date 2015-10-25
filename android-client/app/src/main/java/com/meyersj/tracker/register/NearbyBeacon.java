package com.meyersj.tracker.register;

import android.bluetooth.le.ScanResult;

import com.meyersj.tracker.Utils;

public class NearbyBeacon {
    public ScanResult result;
    public String hash;
    public Integer count;
    public Integer rssi;
    public boolean displayed = false;

    public NearbyBeacon(ScanResult result) {
        this.result = result;
        this.hash = Utils.getHexString(result.getScanRecord().getBytes());
        this.rssi = result.getRssi();
        this.count = 0;
    }
}