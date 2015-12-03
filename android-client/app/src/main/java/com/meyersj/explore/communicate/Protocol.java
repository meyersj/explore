package com.meyersj.explore.communicate;


import android.content.Context;
import android.util.Log;

import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

public class Protocol {

    // Message Type Flags
    public static final byte CLOSE_CONN = (byte) 0x00;
    public static final byte BEACON_LOOKUP = (byte) 0x01;
    public static final byte BEACON_REGISTER = (byte) 0x02;
    public static final byte JOIN_CHANNEL = (byte) 0x03;
    public static final byte LEAVE_CHANNEL = (byte) 0x04;
    public static final byte SEND_BROADCAST = (byte) 0x05;
    public static final byte RECEIVE_BROADCAST = (byte) 0x06;



    public static byte[] newPayload(byte flag, byte[] inPayload) {
        byte[] payload = new byte[8 + inPayload.length];
        byte[] length = ByteBuffer.allocate(4).putInt(inPayload.length).array();
        byte[] flags = {flag, 0x00, 0x00, 0x00};
        // copy length bytes
        int index = 0;
        for(int i = 0; i < 4; i++) {
            payload[index++] = length[i];
        }
        // copy flag bytes
        for(int i = 0; i < 4; i++) {
            payload[index++] = flags[i];
        }
        // copy payload bytes
        for(int i = 0; i < inPayload.length; i++) {
            payload[index++] = inPayload[i];
        }
        return payload;
    }

    public static Socket openCommunication(Context context) throws IOException {
        String host = Utils.getHost(context);
        Integer port = Utils.getPort(context);
        return new Socket(host, port);
    }

    public static void closeCommunication(Socket socket) throws IOException {
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        outStream.write(MessageBuilder.closeConnection());
        socket.close();
    }

    public static String hashAdvertisement(byte[] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            return Utils.getHexString(md.digest(data));
        }
        catch(NoSuchAlgorithmException e) {}
        return null;
    }
}
