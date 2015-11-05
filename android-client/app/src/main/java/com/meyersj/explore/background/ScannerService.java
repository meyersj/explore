package com.meyersj.explore.background;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.explore.communicate.MessageBuilder;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.communicate.ProtocolMessage;
import com.meyersj.explore.communicate.ResponseHandler;
import com.meyersj.explore.communicate.ThreadedCommunicator;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ScannerService extends Service {

    public class Device {

        public Integer counter;
        public ScanResult result;
        public Boolean notified;

        public Device(ScanResult result) {
            this.counter = 0;
            this.result = result;
            this.notified = false;
        }
    }

    private final String TAG = getClass().getCanonicalName();
    private BluetoothLeScanner bleScanner;
    private RateLimiter rateLimiter;
    private boolean scanning = false;
    private Integer serial = 1;
    private HashMap<String, Device> devices;
    private List<ScanFilter> scanFilters = new ArrayList<>();
    private ScanSettings scanSettings;
    private Notifier notifier;
    private ThreadedCommunicator communicator;
    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result == null) return;
            String mac = result.getDevice().getAddress();
            Device device = devices.get(mac);
            // first ping from device
            if (device == null) {
                device = new Device(result);
                device.counter++;
                devices.put(mac, device);
                Log.d(TAG, "new device");
                return;
            }
            // user has already been notified about this beacon
            if (device.notified) {
                Log.d(TAG, "already notified");
                return;
            }
            // device meets threshold, send notification
            if (device.counter + 1 > Cons.NEW_DEVICE_THRESHOLD) {
                device.notified = true;
                handleAdvertisement(device.result);
                return;
            }
            // prevent multiple signals received at once
            if(rateLimiter.tryAcquire()) {
                Log.d(TAG, mac + " " + String.valueOf(device.counter + 1));
                device.counter++;
            }
        }
    };

    @Override
    public void onCreate () {
        Log.d(TAG, "on create service");
        devices = new HashMap<>();
        //counter = new HashMap<>();
        rateLimiter = RateLimiter.create(2);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = scanSettingsBuilder.build();
        communicator = new ThreadedCommunicator(getApplicationContext(), new ResponseHandler(this));
        notifier = new Notifier(this, getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (bleScanner != null) {
            devices.clear();
            communicator.start();
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
            scanning = true;
            notifier.scanningNotification();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "on bind service");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on destroy service");
        if(scanning) {
            communicator.stop();
            bleScanner.stopScan(scanCallback);
            scanning = false;
        }
    }

    private void handleAdvertisement(ScanResult result) {
        ProtocolMessage message = new ProtocolMessage();
        message.advertisement = result.getScanRecord().getBytes();
        message.rssi = result.getRssi();
        message.mac = result.getDevice().getAddress();
        byte[] device = Utils.getDeviceID(getApplicationContext()).getBytes();
        message.payload = MessageBuilder.clientUpdate(
                device, message.mac.getBytes(),
                message.advertisement, message.rssi
        );
        message.payloadFlag = Protocol.CLIENT_UPDATE;
        communicator.addMessage(message);
    }

    public void handleResponse(Bundle data) {
        Log.d(TAG, "handle response");
        boolean registered = false;
        String name = data.getString(Cons.MAC);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        byte[] response = data.getByteArray(Cons.RESPONSE);

        if (flags == null) return;
        switch (flags[0]) {
            case 0x00:
                registered = true;
                if (response != null) {
                    try {
                        name = new String(response, "UTF-8");
                        name = ProtocolMessage.parseBeaconName(name);
                    } catch (UnsupportedEncodingException e) {
                        Log.d(TAG, e.toString());
                    }
                }
                break;
            case 0x01:
                break;
        }
        data.putString(Cons.BEACON_NAME, name);
        data.putBoolean(Cons.REGISTERED, registered);
        notifier.beaconNotification(serial++, name, data);
    }

    public void update(Message message) {
        handleResponse(message.getData());
    }
}
