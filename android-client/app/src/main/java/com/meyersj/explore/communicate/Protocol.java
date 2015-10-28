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
    public static final byte FAIL = (byte) 0x01;

    // Message Type Flags
    public static final byte CLOSE_CONN = (byte) 0x00;
    public static final byte REGISTER_CLIENT = (byte) 0x01;
    public static final byte REGISTER_BEACON = (byte) 0x02;
    public static final byte CLIENT_UPDATE = (byte) 0x03;
    public static final byte PUT_MESSAGE = (byte) 0x04;
    public static final byte DELIMITER = (byte) 0xFF;


    // prefix length field and append delimiter to payload
    public static byte[] newPayload(byte[] inPayload) {
        byte[] payload = new byte[inPayload.length + 4 + 1];
        byte[] length = ByteBuffer.allocate(4).putInt(inPayload.length + 1).array();

        Log.d("PROTOCOL LENGTH", String.valueOf(inPayload.length + 1) + " " + Utils.getHexString(length));
        // copy length bytes
        for(int i = 0; i < 4; i++) {
            payload[i] = length[i];
        }
        // copy payload bytes
        for(int i = 0; i < inPayload.length; i++) {
            payload[4+i] = inPayload[i];
        }
        // add delimiter
        payload[payload.length - 1] = Protocol.DELIMITER;
        return payload;
    }

    // Close connection to server
    public static byte[] closeConnection() {
        byte[] payload = {Protocol.CLOSE_CONN};
        return newPayload(payload);
    }

    // update received beacon advertisement from client
    public static byte[] clientUpdate(byte[] device, byte[] adv, int rssi) {
        byte[] payload = new byte[5 + device.length + adv.length];
        payload[0] = Protocol.CLIENT_UPDATE;
        // signal strength
        payload[1] = (byte) 1;
        payload[2] = (byte) rssi;
        // device data
        payload[3] = (byte) device.length;
        for(int i = 0; i < device.length; i++) {
            payload[4+i] = device[i];
        }
        // advertisement data
        payload[4+device.length] = (byte) adv.length;
        for(int i = 0; i < adv.length; i++) {
            payload[5+device.length+i] = adv[i];
        }
        return newPayload(payload);
    }

    // register human readable name of beacon
    public static byte[] registerBeacon(byte[] name, byte[] adv, byte[] lat, byte[] lon) {
        byte[] payload = new byte[3+name.length+17+adv.length];
        int index = 0;
        payload[index++] = Protocol.REGISTER_BEACON;
        payload[index++] = (byte) name.length;
        for(int i = 0; i < name.length; i++) {
            payload[index++] = name[i];
        }
        payload[index++] = (byte) 16;
        for(int i = 0; i < 8; i++) {
            payload[index++] = lat[i];
        }
        for(int i = 0; i < 8; i++) {
            payload[index++] = lon[i];
        }
        payload[index++] = (byte) adv.length;
        for(int i = 0; i < adv.length; i++) {
            payload[index++] = adv[i];
        }
        return newPayload(payload);
    }

    // register human readable name of client
    public static byte[] registerClient(byte[] device, byte[] client) {
        byte[] payload = new byte[3 + device.length + client.length];
        int index = 0;
        payload[index++] = Protocol.REGISTER_CLIENT;
        // device id
        payload[index++] = (byte) device.length;
        for(int i = 0; i < device.length; i++) {
            payload[index++] = device[i];
        }
        // client name
        payload[index++] = (byte) client.length;
        for(int i = 0; i < client.length; i++) {
            payload[index++] = client[i];
        }
        return newPayload(payload);
    }

    public static byte[] sendMessage(byte[] device, byte[] message, byte[] beacon) {
        // length
        // flag
        // length (device)
        // device
        // length (message)
        // message
        // length (beacon)
        // beacon
        byte[] payload = new byte[4+device.length+message.length+beacon.length];
        int index = 0;
        payload[index++] = Protocol.PUT_MESSAGE;
        payload[index++] = (byte) device.length;
        for(int i = 0; i < device.length; i++) {
            payload[index++] = device[i];
        }
        payload[index++] = (byte) message.length;
        for(int i = 0; i < message.length; i++) {
            payload[index++] = message[i];
        }
        payload[index++] = (byte) beacon.length;
        for(int i = 0; i < beacon.length; i++) {
            payload[index++] = beacon[i];
        }
        return newPayload(payload);
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




}
