package com.meyersj.tracker.explore;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.meyersj.tracker.socket.Protocol;
import com.meyersj.tracker.Utils;


public class BeaconBroadcast {

    private final String TAG = getClass().getCanonicalName();
    private int id;
    private ScanResult scanResult;
    private boolean disconnect = false;
    private byte[] deviceID;

    public BeaconBroadcast(Context context, int id, ScanResult scanResult) {
        this.id = id;
        this.deviceID = Utils.getDeviceID(context).getBytes();
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
            payload = Protocol.closeConnection();
        }
        else {
            byte[] rawBytes = scanResult.getScanRecord().getBytes();
            int rssi = scanResult.getRssi();
            payload = Protocol.clientUpdate(deviceID, rawBytes, rssi);
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

}
