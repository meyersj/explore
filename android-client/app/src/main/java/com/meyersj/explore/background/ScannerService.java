package com.meyersj.explore.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.explore.R;
import com.meyersj.explore.utilities.Utils;

import java.util.ArrayList;
import java.util.List;


public class ScannerService extends Service {

    private final String TAG = getClass().getCanonicalName();
    private BluetoothLeScanner bleScanner;
    private RateLimiter rateLimiter;
    private boolean scanning = false;
    private List<ScanFilter> scanFilters = new ArrayList<>();
    private ScanSettings scanSettings;
    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null && rateLimiter.tryAcquire()) {
                String beacon = Utils.getHexString(result.getScanRecord().getBytes());
                Log.d(TAG, "ble callback " + beacon);
                sendNotification(beacon);
            }
        }
    };

    @Override
    public void onCreate () {
        Log.d(TAG, "on create service");
        rateLimiter = RateLimiter.create(0.1);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        scanSettings = scanSettingsBuilder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (bleScanner != null) {
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
            scanning = true;
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
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
            bleScanner.stopScan(scanCallback);
            scanning = false;
        }
    }

    public void sendNotification(String beacon) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_location_city_white_24dp)
                        .setContentTitle("My notification")
                        .setContentText(beacon);

        int mNotificationId = 1;
        NotificationManager mNotifyMgr = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
