package com.meyersj.tracker.communicator;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;


public abstract class ThreadedCommunicator {


    private final String TAG = getClass().getCanonicalName();
    protected Context context;
    private Handler handler;
    private LinkedBlockingQueue<ProtocolMessage> queue;
    protected Boolean active;


    public ThreadedCommunicator(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.queue = new LinkedBlockingQueue<>();
        this.active = false;
    }

    public void start() {
        if (active) return;
        active = true;
        new Thread(new ScannerThread()).start();
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
                Socket socket = Protocol.openCommunication(context);
                if (socket != null) {
                    while (active) {
                        ProtocolMessage message = queue.poll();
                        if (message != null) {
                            Message threadMessage = sendMessage(socket, message);
                            handler.sendMessage(threadMessage);
                        }
                    }
                    Log.d(TAG, "CLOSE SOCKET");
                    Protocol.closeCommunication(socket);
                }
            } catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    private Message sendMessage(Socket socket, ProtocolMessage message) throws IOException {
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        outStream.write(message.payload);
        return getResponse(message, inStream).getThreadMessage();
    }

    private ProtocolMessage getResponse(ProtocolMessage message, DataInputStream inStream) throws IOException {
        byte[] response = Protocol.readResponse(inStream);
        if (response != null) {
            message.responseFlag = response[0];
            if (response.length > 1) {
                message.response = new byte[response.length - 2];
                System.arraycopy(response, 1, message.response, 0, response.length - 2);
            }
        }
        return message;
    }
}
