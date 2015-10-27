package com.meyersj.tracker.register;

import android.bluetooth.le.ScanResult;

import com.meyersj.tracker.Utils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NearbyBeacon {
    public ScanResult result;
    public String hash;
    public Integer count;
    public Integer rssi;
    public boolean displayed = false;
    public Double lat;
    public Double lon;

    public NearbyBeacon(ScanResult result) {
        this.result = result;
        this.hash = Utils.getHexString(result.getScanRecord().getBytes());
        this.rssi = result.getRssi();
        this.count = 0;
    }

    private byte[] doubleByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public byte[] getLatitudeBytes() {
        if (lat != null) {
            return doubleByteArray(lat);
        }
        return doubleByteArray(0);
    }

    public byte[] getLongitudeBytes() {
        if (lon != null) {
            return doubleByteArray(lon);
        }
        return doubleByteArray(0);
    }
}