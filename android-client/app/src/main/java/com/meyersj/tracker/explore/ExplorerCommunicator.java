package com.meyersj.tracker.explore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.tracker.Utils;
import com.meyersj.tracker.protocol.ThreadedCommunicator;
import com.meyersj.tracker.protocol.ProtocolMessage;
import com.meyersj.tracker.protocol.Protocol;


public class ExplorerCommunicator extends ThreadedCommunicator {

    private RateLimiter rateLimiter;
    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (rateLimiter.tryAcquire()) {
                byte[] device = Utils.getDeviceID(context).getBytes();
                byte[] payload = Protocol.clientUpdate(device, result.getScanRecord().getBytes(), result.getRssi());
                ProtocolMessage message = new ProtocolMessage(payload, Protocol.CLIENT_UPDATE);
                addMessage(message);
            }
        }
    };

    public ExplorerCommunicator(Context context, Handler handler) {
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
