package com.meyersj.tracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;



public class BluetoothScanner {


    private final String TAG = getClass().getCanonicalName();

    private ConcurrentLinkedQueue<BeaconBroadcast> queue;
    private BluetoothLeScanner bleScanner;
    private Thread scanner;
    private Socket socket;
    private Integer counter = 0;
    private String host;
    private int port;
    private RateLimiter rateLimiter;

    public BluetoothScanner(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        queue = new ConcurrentLinkedQueue<>();
        Properties properties = Utils.getProperties(context, "config.properties");
        host = properties.getProperty("Host");
        port = Integer.valueOf(properties.getProperty("Port"));
        rateLimiter = RateLimiter.create(2);
    }

    public void start() {
        stop();
        bleScanner.startScan(scanCallback);
        scanner = new Thread(new ScannerThread());
        scanner.start();
    }

    public void stop() {
        if (scanner != null) {
            bleScanner.stopScan(scanCallback);
            queue.add(new BeaconBroadcast(counter, true));
            scanner = null;
        }

    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (rateLimiter.tryAcquire()) {
                counter += 1;
                queue.add(new BeaconBroadcast(counter, result));
            }
        }
    };


    class ScannerThread implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "start scanning");

            try {
                socket = new Socket(host, port);
                Log.d(TAG, "OPENED SOCKET");
                if(socket != null) {
                    // `active` gets set to false when BluetoothScanner.stop() method gets called
                    boolean disconnect = false;
                    while (!disconnect) {
                        BeaconBroadcast broadcast = queue.poll();
                        if(broadcast != null) {
                            sendDataOverSocket(socket, broadcast);
                            Log.d(TAG, "SENT: " + broadcast.toString());
                            disconnect = broadcast.isDisconnectSignal();
                        }
                    }
                    socket.close();
                    Log.d(TAG, "CLOSED SOCKET");
                }
            } catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }


        }

    }

    private void sendDataOverSocket(Socket socket, BeaconBroadcast broadcast) {
        if (socket != null) {
            try {
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                outStream.write(broadcast.getPayload());
            } catch (IOException e) {
                Log.d(TAG, "IOException sendDataOverSocket: " + e.toString());
            }
        }
    }

}
