package com.meyersj.tracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

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

    public BluetoothScanner(Context context) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        queue = new ConcurrentLinkedQueue<>();
        Properties properties = Utils.getProperties(context, "config.properties");
        host = properties.getProperty("Host");
        port = Integer.valueOf(properties.getProperty("Port"));
    }

    public void start() {
        bleScanner.startScan(scanCallback);
        scanner = new Thread(new ScannerThread());
        scanner.start();
    }

    public void stop() {
        if (scanner != null) {
            bleScanner.stopScan(scanCallback);
            queue.add(new BeaconBroadcast(counter, null));
            scanner.interrupt();
            scanner = null;
        }

    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            counter += 1;
            queue.add(new BeaconBroadcast(counter, result));
        }
    };


    class ScannerThread implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "start scanning");

            try {
                socket = new Socket(host, port);
                Log.d(TAG, "OPENED SOCKET");
            } catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }

            if(socket != null) {
                while (true) {
                    BeaconBroadcast broadcast = queue.poll();
                    if(broadcast != null) {
                        Log.d(TAG, "FETCH: " + broadcast.toString());
                        sendDataOverSocket(socket, broadcast);
                    }
                }
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
