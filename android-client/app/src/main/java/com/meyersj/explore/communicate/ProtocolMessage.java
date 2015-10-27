package com.meyersj.explore.communicate;

import android.os.Bundle;
import android.os.Message;


public class ProtocolMessage {

    public int rssi;
    public String hash;
    public byte[] advertisement;
    public byte[] payload;
    public byte payloadFlag;
    public byte responseFlag;
    public byte[] response;


    public ProtocolMessage() {}

    public Message getThreadMessage() {
        Bundle bundle = new Bundle();
        bundle.putByte("payload_flag", payloadFlag);
        bundle.putByteArray("payload", payload);
        bundle.putByteArray("advertisement", advertisement);
        bundle.putString("hash", hash);
        bundle.putInt("rssi", rssi);
        bundle.putByte("response_flag", responseFlag);
        bundle.putByteArray("response", response);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }
}
