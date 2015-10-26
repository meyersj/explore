package com.meyersj.tracker.explore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.google.common.util.concurrent.RateLimiter;
import com.meyersj.tracker.R;
import com.meyersj.tracker.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;



public class ExploreScanner {


    private final String TAG = getClass().getCanonicalName();

    private Context context;
    private ConcurrentLinkedQueue<BeaconBroadcast> queue;
    private BluetoothLeScanner bleScanner;
    private Thread scanner;
    private Socket socket;
    private Integer counter = 0;
    private String host;
    private int port;
    private RateLimiter rateLimiter;
    private boolean active = false;
    private ExploreTask exploreTask;
    private static Handler handler;
    private TextView statusTest;

    public ExploreScanner(Context context, final TextView statusText) {
        this.context = context;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.statusTest = statusText;
        queue = new ConcurrentLinkedQueue<>();
        Properties properties = Utils.getProperties(context, "config.properties");
        host = properties.getProperty("Host");
        port = Integer.valueOf(properties.getProperty("Port"));
        rateLimiter = RateLimiter.create(2);
        exploreTask = new ExploreTask(this);
        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {
                ExploreTask task = (ExploreTask) inputMessage.obj;
                Log.d(TAG + "TASK", task.code);
                statusText.setText(task.code);
            }

        };
    }

    public void start() {
        stop();
        active = true;
        bleScanner.startScan(scanCallback);
        scanner = new Thread(new ScannerThread());
        scanner.start();
    }

    public void stop() {
        if (active) {
            bleScanner.stopScan(scanCallback);
            queue.add(new BeaconBroadcast(counter, true));
        }

    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (rateLimiter.tryAcquire()) {
                counter += 1;
                queue.add(new BeaconBroadcast(context, counter, result));
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
                        exploreTask.updateBroadcast(broadcast);
                        if(broadcast != null) {
                            sendDataOverSocket(socket, broadcast);
                            Log.d(TAG, "SENT: " + broadcast.toString());
                            disconnect = broadcast.isDisconnectSignal();
                        }
                    }
                    socket.close();
                    active = false;
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

    // Handle status messages from tasks
    public void handleBroadcast(ExploreTask task) {
        Message completeMessage = handler.obtainMessage(0, task);
                completeMessage.sendToTarget();
    }

}
