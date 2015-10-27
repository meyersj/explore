package com.meyersj.explorer;

import java.nio.ByteBuffer;

public class NearbyBeacon {
    public String hash;
    public Integer count;
    public Integer rssi;
    public byte[] advertisement;
    public boolean displayed = false;
    public Double lat;
    public Double lon;
    public boolean registered;


    public NearbyBeacon(byte[] advertisement, String hash, Integer rssi) {
        this.advertisement = advertisement;
        this.hash = hash;
        this.rssi = rssi;
        this.count = 0;
        this.registered = false;
    }

    public NearbyBeacon(boolean registered, byte[] advertisement, String hash, Integer rssi) {
        this.advertisement = advertisement;
        this.hash = hash;
        this.rssi = rssi;
        this.count = 0;
        this.registered = registered;
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