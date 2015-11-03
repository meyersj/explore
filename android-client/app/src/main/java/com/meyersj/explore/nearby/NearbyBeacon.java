package com.meyersj.explore.nearby;

import java.nio.ByteBuffer;

public class NearbyBeacon {

    public String mac;
    public String name;
    public Integer count;
    public Integer rssi;
    public boolean displayed = false;
    public Double lat;
    public Double lon;
    public boolean registered;

    public NearbyBeacon(boolean registered, String mac, String name, Integer rssi) {
        this.mac = mac;
        this.name = name;
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