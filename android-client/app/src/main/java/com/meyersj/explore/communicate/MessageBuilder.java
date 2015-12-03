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

    public static byte[] beaconLookup(byte[] device, byte[] mac, byte[] adv, int rssi) {
        byte[] payload = new byte[5 + mac.length + device.length + adv.length];
        byte[] strength = {(byte) rssi};
        Integer index = 0;
        index = addField(payload, strength, index);
        index = addField(payload, device, index);
        index = addField(payload, mac, index);
        addField(payload, adv, index);
        return Protocol.newPayload(Protocol.BEACON_LOOKUP, payload);
    }

    public static byte[] beaconRegister(byte[] name, byte[] mac) {
        byte[] payload = new byte[2+name.length+mac.length];
        addField(payload, name, addField(payload, name, 0));
        Integer index = 0;
        index = addField(payload, name, index);
        addField(payload, mac, index);
        return Protocol.newPayload(Protocol.BEACON_REGISTER, payload);
    }

    public static byte[] broadcastMessage(byte[] device, byte[] user, byte[] message, byte[] beacon) {
        byte[] payload = new byte[4+device.length+user.length+message.length+beacon.length];
        int index = 0;
        index = addField(payload, device, index);
        index = addField(payload, user, index);
        index = addField(payload, message, index);
        addField(payload, beacon, index);
        return Protocol.newPayload(Protocol.SEND_BROADCAST, payload);
    }

    public static byte[] joinChannel(byte[] device, byte[] beacon) {
        byte[] payload = new byte[2+device.length+beacon.length];
        addField(payload, beacon, addField(payload, device, 0));
        return Protocol.newPayload(Protocol.JOIN_CHANNEL, payload);
    }

    public static byte[] leaveChannel(byte[] device, byte[] channel) {
        byte[] payload = new byte[2+device.length+channel.length];
        addField(payload, channel, addField(payload, device, 0));
        return Protocol.newPayload(Protocol.LEAVE_CHANNEL, payload);
    }
}