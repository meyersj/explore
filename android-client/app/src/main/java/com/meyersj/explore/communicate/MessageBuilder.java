package com.meyersj.explore.communicate;


public class MessageBuilder {

    private static Integer addField(byte[] payload, byte[]data, Integer offset) {
        payload[offset++] = (byte) data.length;
        for(int i = 0; i < data.length; i++) {
            payload[offset++] = data[i];
        }
        return offset;
    }

    public static byte[] closeConnection() {
        byte[] payload = {};
        return Protocol.newPayload(Protocol.CLOSE_CONN, payload);
    }

    public static byte[] clientUpdate(byte[] device, byte[] mac, byte[] adv, int rssi) {
        byte[] payload = new byte[5 + mac.length + device.length + adv.length];
        byte[] strength = {(byte) rssi};
        Integer index = 0;
        index = addField(payload, strength, index);
        index = addField(payload, device, index);
        index = addField(payload, mac, index);
        addField(payload, adv, index);
        return Protocol.newPayload(Protocol.CLIENT_UPDATE, payload);
    }

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
        return Protocol.newPayload(Protocol.REGISTER_BEACON, payload);
    }

    public static byte[] sendMessage(byte[] device, byte[] user, byte[] message, byte[] beacon) {
        byte[] payload = new byte[4+device.length+user.length+message.length+beacon.length];
        int index = 0;
        index = addField(payload, device, index);
        index = addField(payload, user, index);
        index = addField(payload, message, index);
        addField(payload, beacon, index);
        return Protocol.newPayload(Protocol.PUT_MESSAGE, payload);
    }

    public static byte[] getMessages(byte[] beacon) {
        byte[] payload = new byte[1+beacon.length];
        addField(payload, beacon, 0);
        return Protocol.newPayload(Protocol.GET_MESSAGE, payload);
    }
}