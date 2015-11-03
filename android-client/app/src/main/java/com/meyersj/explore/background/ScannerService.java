package com.meyersj.explore.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.explore.R;
import com.meyersj.explore.activity.MainActivity;
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

    private final String TAG = getClass().getCanonicalName();
    private BluetoothLeScanner bleScanner;
    private RateLimiter rateLimiter;
    private boolean scanning = false;
    private Integer serial = 1;
    private HashMap<String, Integer> counter;
    private HashMap<String, ScanResult> received;
    private List<ScanFilter> scanFilters = new ArrayList<>();
    private ScanSettings scanSettings;
    private ThreadedCommunicator communicator;
    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null && rateLimiter.tryAcquire()) {
                String beacon = Utils.getHexString(result.getScanRecord().getBytes());
                if (received.containsKey(beacon)) {
                    int count = counter.get(beacon) + 1;
                    Log.d(TAG, "beacon: " + String.valueOf(count));
                    counter.put(beacon, count);
                    if (count == 5) {
                        Log.d(TAG, "handle background ble advertisement" + beacon);
                        received.put(beacon, result);
                        handleAdvertisement(result);
                    }
                }
                else {
                    received.put(beacon, result);
                    counter.put(beacon, 0);
                }
            }
        }
    };

    @Override
    public void onCreate () {
        Log.d(TAG, "on create service");
        received = new HashMap<>();
        counter = new HashMap<>();
        rateLimiter = RateLimiter.create(1);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = scanSettingsBuilder.build();
        communicator = new ThreadedCommunicator(getApplicationContext(), new ResponseHandler(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (bleScanner != null) {
            communicator.start();
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
            scanning = true;
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

    public void sendNotification(String beaconName, Bundle extras) {

        int icon = R.drawable.ic_location_city_white_24dp;
        String title = "New Location";
        String content = "Touch to register it.";
        if (extras.getBoolean(Cons.REGISTERED)) {
            icon = R.drawable.ic_chat_white_24dp;
            title = beaconName;
            content = "Touch to add a message.";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setContentText(content);

        // setup activity that notification will open
        Intent resultIntent = new Intent(this, MainActivity.class);
        extras.putBoolean("notification", true);
        resultIntent.putExtras(extras);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        // vibrate and led
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(2500);
        mBuilder.setVibrate(new long[] { 0, 1000, 500, 1000});
        mBuilder.setLights(Color.YELLOW, 3000, 3000);

        // issue notification
        NotificationManager mNotifyMgr = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(serial++, mBuilder.build());
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
        sendNotification(name, data);
    }

    public void update(Message message) {
        handleResponse(message.getData());
    }
}
