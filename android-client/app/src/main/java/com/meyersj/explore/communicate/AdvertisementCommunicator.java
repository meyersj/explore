package com.meyersj.explore.communicate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.explore.utilities.Utils;


public class AdvertisementCommunicator extends ThreadedCommunicator {

    private RateLimiter rateLimiter;
    private BluetoothLeScanner bleScanner;
    private boolean scanning = false;

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (rateLimiter.tryAcquire()) {
                ProtocolMessage message = new ProtocolMessage();
                message.handler = ProtocolMessage.SEARCH_HANDLER;
                message.mac = result.getDevice().getAddress();
                message.advertisement = result.getScanRecord().getBytes();
                message.rssi = result.getRssi();
                byte[] device = Utils.getDeviceID(context).getBytes();
                byte[] payload = MessageBuilder.beaconLookup(
                        device, message.mac.getBytes(),
                        message.advertisement, message.rssi
                );
                message.payload = payload;
                message.payloadFlag = Protocol.BEACON_LOOKUP;
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

    public void startScan() {
        if (!scanning) {
            bleScanner.startScan(scanCallback);
            scanning = true;
        }
    }
    public void stopScan() {
        if (scanning) {
            bleScanner.stopScan(scanCallback);
            scanning = false;
        }
    }
}
