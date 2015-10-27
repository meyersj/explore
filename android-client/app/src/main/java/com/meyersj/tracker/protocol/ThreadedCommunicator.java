package com.meyersj.tracker.protocol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.meyersj.tracker.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;


public abstract class ThreadedCommunicator {


    private final String TAG = getClass().getCanonicalName();
    protected Context context;
    private Handler handler;
    private Thread scanner;
    private Socket socket;
    private LinkedBlockingQueue<ProtocolMessage> queue;
    protected Boolean active;


    public ThreadedCommunicator(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.queue = new LinkedBlockingQueue<>();
        //this.rateLimiter = RateLimiter.create(10);
        //BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //if(bluetoothAdapter != null) {
        //    bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        //}
        this.active = false;
    }

    public void start() {
        if (active) return;
        active = true;
        scanner = new Thread(new ScannerThread());
        scanner.start();
    }

    public void stop() {
        if (active) {
            active = false;
        }
    }

    protected void addMessage(ProtocolMessage message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Log.d(TAG, e.toString());
        }
    }

    class ScannerThread implements Runnable {

        @Override
        public void run() {

            try {
                Log.d(TAG, "OPEN SOCKET");
                socket = Utils.openSocket(context);
                if (socket != null) {
                    while (active) {
                        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                        DataInputStream inStream = new DataInputStream(socket.getInputStream());
                        ProtocolMessage message = queue.poll();
                        if (message != null) {
                            outStream.write(message.payload);
                            message = getResponse(message, inStream);
                            Message threadMessage = message.getThreadMessage();
                            handler.sendMessage(threadMessage);
                        }
                    }
                    Log.d(TAG, "CLOSE SOCKET");
                    socket.close();

                }
            } catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    private ProtocolMessage getResponse(ProtocolMessage message, DataInputStream inStream) throws IOException {
        byte[] responseLength = new byte[4];
        inStream.read(responseLength);
        ByteBuffer wrapped = ByteBuffer.wrap(responseLength);
        Integer length = wrapped.getInt();
        if (length > 0) {
            message.responseFlag = inStream.readByte();
            byte[] response = new byte[length];
            if (length > 1) {
                inStream.read(response);

                message.response = response;
            }
            else {
                response[0] = message.responseFlag;
                message.response = response;
            }
            return message;
        }
        return message;
    }




}
