package com.meyersj.explore.communicate;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.meyersj.explore.utilities.Cons;


public class ProtocolMessage {

    public static final String HANDLER = "handler";
    public static final int SEARCH_HANDLER = 1;
    public static final int CHAT_HANDLER = 2;
    public int handler = 0;
    public String mac;
    public int rssi;
    public String name;
    public byte[] advertisement;
    public byte[] payload;
    public byte payloadFlag;
    public byte[] responseFlags;
    public byte[] response;


    public ProtocolMessage() {}

    public Message getThreadMessage() {
        Bundle bundle = new Bundle();
        bundle.putInt(HANDLER, handler);
        bundle.putString(Cons.MAC, mac);
        bundle.putByte(Cons.PAYLOAD_FLAGS, payloadFlag);
        bundle.putByteArray(Cons.PAYLOAD, payload);
        bundle.putByteArray(Cons.ADVERTISEMENT, advertisement);
        bundle.putInt(Cons.RSSI, rssi);
        bundle.putByteArray(Cons.RESPONSE_FLAGS, responseFlags);
        bundle.putByteArray(Cons.RESPONSE, response);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }

    public static String parseBeaconName(String value) {
        Log.d("PARSE", value);
        String[] fields = value.split("\t");
        if (fields.length == 3) {
            return fields[2];
        }
        return "error";
    }
}
