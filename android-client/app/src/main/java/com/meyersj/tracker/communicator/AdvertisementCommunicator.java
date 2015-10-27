package com.meyersj.tracker.communicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.tracker.Utils;


public class AdvertisementCommunicator extends ThreadedCommunicator {

    private RateLimiter rateLimiter;
    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (rateLimiter.tryAcquire()) {
                byte[] device = Utils.getDeviceID(context).getBytes();
                byte[] payload = Protocol.clientUpdate(device, result.getScanRecord().getBytes(), result.getRssi());
                ProtocolMessage message = new ProtocolMessage();
                message.advertisement = result.getScanRecord().getBytes();
                message.rssi = result.getRssi();
                message.hash = Utils.getHexString(result.getScanRecord().getBytes());
                message.payload = payload;
                message.payloadFlag = Protocol.CLIENT_UPDATE;
                addMessage(message);
            }
        }
    };

    public AdvertisementCommunicator(Context context, Handler handler) {
        super(context, handler);
        this.rateLimiter = RateLimiter.create(3);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    public void start() {
        if (!active) {
            bleScanner.startScan(scanCallback);
        }
        super.start();
    }

    public void stop() {
        if (active) {
            bleScanner.stopScan(scanCallback);
        }
        super.stop();
    }
}
