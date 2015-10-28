package com.meyersj.explore.communicate;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.meyersj.explore.utilities.Cons;


public class ProtocolMessage {

    public int rssi;
    public String key;
    public byte[] advertisement;
    public byte[] payload;
    public byte payloadFlag;
    public byte[] responseFlags;
    public byte[] response;


    public ProtocolMessage() {}

    public Message getThreadMessage() {
        Bundle bundle = new Bundle();
        bundle.putByte(Cons.PAYLOAD_FLAGS, payloadFlag);
        bundle.putByteArray(Cons.PAYLOAD, payload);
        bundle.putByteArray(Cons.ADVERTISEMENT, advertisement);
        bundle.putString(Cons.BEACON_KEY, key);
        bundle.putInt(Cons.RSSI, rssi);
        bundle.putByteArray(Cons.RESPONSE_FLAGS, responseFlags);
        bundle.putByteArray(Cons.RESPONSE, response);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }


    public static String parseBeaconName(String value) {
        Log.d("PARSE", value);
        String[] split1 = value.split("\\|");
        if (split1.length == 2) {
            String[] split2 = split1[1].split(":");
            if (split2.length == 2) {
                String name = split2[0];
                String coordinates = split2[1];
                return name + " " + coordinates;
            }
        }
        return value;
    }
}
