package com.meyersj.tracker;

import android.bluetooth.le.ScanResult;
import android.util.Log;


public class BeaconBroadcast {

    private final String TAG = getClass().getCanonicalName();
    private int id;
    private ScanResult scanResult;
    private boolean disconnect = false;

    public BeaconBroadcast(int id, ScanResult scanResult) {
        this.id = id;
        if(scanResult != null) {
            Log.d(TAG, "PAYLOAD: " + Utils.getHexString(scanResult.getScanRecord().getBytes()));
            this.scanResult = scanResult;
        }
    }

    public BeaconBroadcast(int id, boolean disconnect) {
        this.id = id;
        this.disconnect = disconnect;
    }

    public int getId() {
        return id;
    }


    public byte[] getPayload() {
        byte[] payload;
        if(disconnect || scanResult == null) {
            // payload to message to kill connection
            payload = new byte[2];
            payload[0] = (byte) 0;
            payload[1] = (byte) 255;
        }
        else {
            byte[] rawBytes = scanResult.getScanRecord().getBytes();
            int rssi = scanResult.getRssi();
            byte delimiter = (byte) 255;
            payload = buildPacket(rawBytes, rssi, delimiter);
        }
        return payload;
    }

    @Override
    public String toString() {
        if(scanResult != null) {
            return id + " " + scanResult.getRssi();
        }
        return id + " CLOSE CONNECTION SIGNAL";
    }

    public boolean isDisconnectSignal() {
        return disconnect;
    }



    private byte[] buildPacket (byte[] payload, int rssi, byte delimiter) {

        // placeholder for length byte as first byte in payload
        // payload can be split into separate packets
        // so length byte will enable backend to group properly
        //current.add((byte) 255);

        // length + signal strength + payload + delimiter
        int length = payload.length + 3;
        byte[] packet = new byte[length];
        packet[0] = (byte) length;
        packet[1] = (byte) rssi;
        Log.d(TAG, "RSSI: " + rssi);
        for(int i = 2; i < payload.length; i++) {
            packet[i] = payload[i-2];
        }
        packet[length - 1] = delimiter;

        return packet;
    }
}
