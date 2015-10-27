package com.meyersj.tracker.protocol;

import android.os.Bundle;
import android.os.Message;


public class ProtocolMessage {

    public byte[] payload;
    public byte payloadFlag;
    public byte responseFlag;
    public byte[] response;

    public ProtocolMessage(byte[] payload, byte payloadFlag) {
        this.payload = payload;
        this.payloadFlag = payloadFlag;
    }

    public Message getThreadMessage() {
        Bundle bundle = new Bundle();
        bundle.putByte("payload_flag", payloadFlag);
        bundle.putByteArray("payload", payload);
        bundle.putByte("response_flag", responseFlag);
        bundle.putByteArray("response", response);
        Message message = new Message();
        message.setData(bundle);
        return message;
    }
}
