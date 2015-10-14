package com.meyersj.tracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;



public class BluetoothScanner {


    private final String TAG = getClass().getCanonicalName();

    private BluetoothAdapter bluetoothAdapter;
    private ConcurrentLinkedQueue<BeaconBroadcast> queue;


    private BluetoothLeScanner bleScanner;
    private Thread scanner;
    private Socket socket;
    private Integer counter = 0;

    public BluetoothScanner() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null)
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        queue = new ConcurrentLinkedQueue<>();

    }

    public void start() {
        bleScanner.startScan(scanCallback);
        scanner = new Thread(new ScannerThread());
        scanner.start();
    }

    public void stop() {
        bleScanner.stopScan(scanCallback);
        queue.add(new BeaconBroadcast(counter, null));
        scanner.interrupt();
        scanner = null;

    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.d(TAG, "add result");
            counter += 1;
            queue.add(new BeaconBroadcast(counter, result));
        }
    };


    class ScannerThread implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "start scanning");

            try {
                socket = new Socket("meyersj.com", 8082);
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

    public class BeaconBroadcast {

        private int id;
        private ScanResult scanResult;

        public BeaconBroadcast(int id, ScanResult scanResult) {
            this.id = id;
            if(scanResult != null) {
                Log.d(TAG, "PAYLOAD: " + getHexString(scanResult.getScanRecord().getBytes()));
                this.scanResult = scanResult;
            }
        }

        public int getId() {
            return id;
        }

        public byte[] getPayload() {
            byte[] payload;
            if(scanResult != null) {
                byte[] rawBytes = scanResult.getScanRecord().getBytes();
                int rssi = scanResult.getRssi();
                byte delimiter = (byte) 255;
                payload = buildPacket(rawBytes, rssi, delimiter);
            }
            else {
                // payload to message to kill connection
                payload = new byte[2];
                payload[0] = (byte) 0;
                payload[1] = (byte) 255;
            }
            /*
            // for testing
            List<byte[]> payload = new ArrayList<>();
            byte[] bytes1 = {(byte) 1, (byte)1, (byte)255};
            byte[] bytes2 = {(byte) 2, (byte)1, (byte)255};
            byte[] bytes3 = {(byte) 3, (byte)1, (byte)255};

            payload.add(bytes1);
            payload.add(bytes2);
            payload.add(bytes3);
            */

            return payload;
        }

        @Override
        public String toString() {
            if(scanResult != null) {
                return id + " " + scanResult.getRssi();
            }
            return id + " CLOSE CONNECTION SIGNAL";
        }
    }



    private byte[] buildPacket (byte[] payload, int rssi, byte delimiter) {
        //List<byte[]> packets = new ArrayList<>();
        //List<Byte> current = new ArrayList<>();
        //int size = 0;

        // placeholder for length byte as first byte in payload
        // payload can be split into separate packets
        // so length byte will enable backend to group properly
        //current.add((byte) 255);

        // length + signal strength + payload + delimiter
        int length = payload.length + 3;
        byte[] packet = new byte[length];
        packet[0] = (byte) length;


        packet[1] = (byte) rssi;
        Log.d(TAG, "RSSI: " + rssi);
        Log.d(TAG, "int bit count: " + Integer.bitCount(rssi));
        for(int i = 2; i < payload.length; i++) {
            packet[i] = payload[i-2];
        }
        packet[length - 1] = delimiter;

        return packet;

        // check if payload contains delimiter character
        // if so split into separate packets
        /*
        for(int i = 0; i < payload.length; i++) {
            // delimiter was found in payload, so create new packet for remaining bytes
            if (payload[i] == delimiter) {
                current.add(delimiter);
                size += current.size();
                packets.add(copyBytes(current));
                size += 1;
                current = new ArrayList<>();
            }
            // regular byte sequence so copy as normal
            else {
                current.add(payload[i]);
            }
        }
        */

        // add delimiter to end of last packet
        //current.add(delimiter);
        //size += current.size();
        //packets.add(copyBytes(current));

        // set length byte
        //packets.get(0)[0] = (byte) size;
        //return packets;
    }

    // util to copy List of Byte objects into primitive byte array
    //private byte[] copyBytes(List<Byte> bytes) {
    //    byte[] newBytes = new byte[bytes.size()];
    //    for(int i = 0; i < bytes.size(); i++) {
    //        newBytes[i] = bytes.get(i);
    //    }
    //    return newBytes;
    //}

    // return hex string for array of bytes
    private String getHexString(byte[] rawBytes) {
        String hexString = "";
        for(int i=0; i < rawBytes.length; i++) {
            hexString += String.format("%2s", Integer.toHexString(rawBytes[i] & 0xFF)).replace(' ', '0') + " ";
        }
        return hexString;
    }

}
