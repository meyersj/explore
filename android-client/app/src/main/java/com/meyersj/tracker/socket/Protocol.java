package com.meyersj.tracker.socket;



public class Protocol {

    // Error Flags
    public static final byte SUCCESS = (byte) 0x00;
    public static final byte FAIL = (byte) 0x01;

    // Message Type Flags
    public static final byte CLOSE_CONN = (byte) 0x00;
    public static final byte REGISTER_CLIENT = (byte) 0x01;
    public static final byte REGISTER_BEACON = (byte) 0x02;
    public static final byte CLIENT_UPDATE = (byte) 0x03;
    public static final byte GET_STATUS = (byte) 0x04;
    public static final byte DELIMITER = (byte) 0xFF;

    // Close connection to server
    public static byte[] closeConnection() {
        byte[] payload = {(byte) 3, (byte) Protocol.CLOSE_CONN, Protocol.DELIMITER};
        return payload;
    }

    // update received beacon advertisement from client
    public static byte[] clientUpdate(byte[] device, byte[] adv, int rssi) {
        int length = device.length + adv.length + 7;
        byte[] payload = new byte[length];
        payload[0] = (byte) length;
        payload[1] = Protocol.CLIENT_UPDATE;
        // length and signal strength
        payload[2] = (byte) 1;
        payload[3] = (byte) rssi;
        // length and device data
        payload[4] = (byte) device.length;
        for(int i = 0; i < device.length; i++) {
            payload[5+i] = device[i];
        }
        // length and advertisement data
        payload[5+device.length] = (byte) adv.length;
        for(int i = 0; i < adv.length; i++) {
            payload[6+device.length+i] = adv[i];
        }
        // ending delimiter
        payload[payload.length-1] = Protocol.DELIMITER;
        return payload;
    }

    // register human readable name of beacon
    public static byte[] registerBeacon(byte[] name, byte[] adv) {
        byte[] payload = new byte[5+name.length+adv.length];
        payload[0] = (byte) payload.length;
        payload[1] = Protocol.REGISTER_BEACON;
        // length and device name
        payload[2] = (byte) name.length;
        for(int i = 0; i < name.length; i++) {
            payload[3+i] = name[i];
        }
        // length and advertisement
        payload[3+name.length] = (byte) adv.length;
        for(int i = 0; i < adv.length; i++) {
            payload[4+name.length+i] = adv[i];
        }
        // ending delimiter
        payload[payload.length-1] = Protocol.DELIMITER;
        return payload;
    }

    // register human readable name of client
    public static byte[] registerClient(byte[] device, byte[] client) {
        byte[] payload = new byte[5 + device.length + client.length];
        payload[0] = (byte) payload.length;
        payload[1] = Protocol.REGISTER_CLIENT;
        // length and device id
        payload[2] = (byte) device.length;
        for(int i = 0; i < device.length; i++) {
            payload[3+i] = device[i];
        }
        // length and client name
        payload[3+device.length] = (byte) client.length;
        for(int i = 0; i < client.length; i++) {
            payload[3+1+device.length+i] = client[i];
        }
        // ending delimiter
        payload[payload.length - 1] = Protocol.DELIMITER;
        return payload;
    }

}
