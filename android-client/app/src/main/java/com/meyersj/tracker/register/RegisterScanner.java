package com.meyersj.tracker.register;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.tracker.Utils;
import com.meyersj.tracker.calibrate.Accumulator;


public class RegisterScanner {


    private final String TAG = getClass().getCanonicalName();

    private BluetoothLeScanner bleScanner;
    private NearbyAdapter nearbyAdapter;
    private RateLimiter rateLimiter;

    public RegisterScanner(NearbyAdapter adapter) {
        this.nearbyAdapter = adapter;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        rateLimiter = RateLimiter.create(10);
    }

    public void start() {
        bleScanner.startScan(scanCallback);
        nearbyAdapter.clear();
    }

    public void stop() {
        bleScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "result");
            if (rateLimiter.tryAcquire()) {
                nearbyAdapter.add(new NearbyBeacon(result));
            }
        }
    };

}
