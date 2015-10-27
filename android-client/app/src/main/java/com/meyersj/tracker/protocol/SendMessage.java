package com.meyersj.tracker.protocol;

import android.content.Context;
import android.util.Log;

import com.meyersj.tracker.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;


public class SendMessage implements Runnable {

    private final String TAG = getClass().getCanonicalName();
    private String host;
    private int port;
    protected Socket socket;
    protected byte[] payload;

    public SendMessage() {}

    public SendMessage(Context context, byte[] payload) {
        Properties properties = Utils.getProperties(context, "config.properties");
        this.host = properties.getProperty("Host");
        this.port = Integer.valueOf(properties.getProperty("Port"));
        this.payload = payload;
    }

    public boolean openSocket() {
        try {
            socket = new Socket(host, port);
            Log.d(TAG, "OPENED SOCKET");
            if(socket != null) {
                return true;
            }
        } catch (UnknownHostException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        return false;
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    public void run() {
        Log.d(TAG, "send message");

        if (openSocket()) {
            send(payload);
            closeSocket();
        }
    }

    protected void send(byte[] payload) {
        if (socket != null) {
            try {
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "write " + Utils.getHexString(payload));
                outStream.write(payload);
                outStream.write(Protocol.closeConnection());
            } catch (IOException e) {
                Log.d(TAG, "IOException sendDataOverSocket: " + e.toString());
            }
        }
    }

    protected Byte receive() {
        try {
            DataInputStream inStream = new DataInputStream(socket.getInputStream());
            return inStream.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
