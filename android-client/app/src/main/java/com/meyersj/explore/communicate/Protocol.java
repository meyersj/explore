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

    // Error Flags
    public static final byte SUCCESS = (byte) 0x00;

    // Message Type Flags
    public static final byte CLOSE_CONN = (byte) 0x00;
    public static final byte REGISTER_CLIENT = (byte) 0x01;
    public static final byte REGISTER_BEACON = (byte) 0x02;
    public static final byte CLIENT_UPDATE = (byte) 0x03;
    public static final byte PUT_MESSAGE = (byte) 0x04;
    public static final byte GET_MESSAGE = (byte) 0x05;


    // prefix length field and append delimiter to payload
    private static byte[] newPayload(byte flag, byte[] inPayload) {
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

    // Close connection to server
    public static byte[] closeConnection() {
        byte[] payload = {};
        return newPayload(Protocol.CLOSE_CONN, payload);
    }

    private static Integer addField(byte[] payload, byte[]data, Integer offset) {
        payload[offset++] = (byte) data.length;
        for(int i = 0; i < data.length; i++) {
            payload[offset++] = data[i];
        }
        return offset;
    }


    // update received beacon advertisement from client
    public static byte[] clientUpdate(byte[] device, byte[] adv, int rssi) {
        byte[] payload = new byte[4 + device.length + adv.length];
        byte[] strength = {(byte) rssi};
        Integer index = 0;
        index = addField(payload, strength, index);
        index = addField(payload, device, index);
        addField(payload, adv, index);
        return newPayload(Protocol.CLIENT_UPDATE, payload);
    }

    // register human readable name of beacon
    public static byte[] registerBeacon(byte[] name, byte[] adv, byte[] lat, byte[] lon) {
        byte[] payload = new byte[3+16+name.length+adv.length];
        int index = 0;
        index = addField(payload, name, index);
        payload[index++] = (byte) 16;
        for(int i = 0; i < 8; i++) {
            payload[index++] = lat[i];
        }
        for(int i = 0; i < 8; i++) {
            payload[index++] = lon[i];
        }
        addField(payload, adv, index);
        return newPayload(Protocol.REGISTER_BEACON, payload);
    }

    // register human readable name of client
    public static byte[] registerClient(byte[] device, byte[] client) {
        byte[] payload = new byte[2 + device.length + client.length];
        int index = 0;
        index = addField(payload, device, index);
        addField(payload, client, index);
        return newPayload(Protocol.REGISTER_CLIENT, payload);
    }

    public static byte[] sendMessage(byte[] device, byte[] user, byte[] message, byte[] beacon) {
        byte[] payload = new byte[4+device.length+user.length+message.length+beacon.length];
        int index = 0;
        index = addField(payload, device, index);
        index = addField(payload, user, index);
        index = addField(payload, message, index);
        addField(payload, beacon, index);
        return newPayload(Protocol.PUT_MESSAGE, payload);
    }

    public static Socket openCommunication(Context context) throws IOException {
        String host = Utils.getHost(context);
        Integer port = Utils.getPort(context);
        return new Socket(host, port);
    }

    public static void closeCommunication(Socket socket) throws IOException {
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        outStream.write(Protocol.closeConnection());
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

    public static byte[] getMessages(byte[] beacon) {
        byte[] payload = new byte[1+beacon.length];
        addField(payload, beacon, 0);
        return newPayload(Protocol.GET_MESSAGE, payload);
    }
}
