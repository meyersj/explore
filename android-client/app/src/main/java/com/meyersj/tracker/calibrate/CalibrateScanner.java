package com.meyersj.tracker.calibrate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.tracker.Utils;
import com.meyersj.tracker.explore.BeaconBroadcast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;


public class CalibrateScanner {


    private final String TAG = getClass().getCanonicalName();

    //private ConcurrentLinkedQueue<BeaconBroadcast> queue;
    private BluetoothLeScanner bleScanner;
    //private Thread scanner;
    //private Socket socket;
    private Integer counter = 0;
    private String host;
    private int port;
    private Accumulator accumulator;
    //private RateLimiter rateLimiter;
    //private boolean active = false;

    public CalibrateScanner(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        //Properties properties = Utils.getProperties(context, "config.properties");
        //host = properties.getProperty("Host");
        //port = Integer.valueOf(properties.getProperty("Port"));
    }

    public void start() {
        bleScanner.startScan(scanCallback);
        accumulator = new Accumulator();
    }

    public void stop() {
        bleScanner.stopScan(scanCallback);
        accumulator.logData();
        counter = 0;
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            counter += 1;
            accumulator.add(result);
        }
    };

}
