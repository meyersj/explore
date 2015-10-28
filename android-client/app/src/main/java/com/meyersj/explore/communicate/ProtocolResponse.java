package com.meyersj.explore.communicate;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class ProtocolResponse {

    private byte[] flags;
    private byte[] response;


    public ProtocolResponse(byte[] flags, byte[] response) {
        this.flags = flags;
        this.response = response;
    }

    public static ProtocolResponse read(DataInputStream inStream) throws IOException {
        byte[] responseLength = new byte[4];
        byte[] flags = new byte[4];
        byte[] response;
        inStream.read(responseLength);
        inStream.read(flags);
        ByteBuffer wrapped = ByteBuffer.wrap(responseLength);
        Integer length = wrapped.getInt();
        if (length > 0) {
            response = new byte[length];
            inStream.read(response);
            return new ProtocolResponse(flags, response);
        }
        return new ProtocolResponse(flags, null);
    }

    public byte[] getFlags() {
        return this.flags;
    }

    public byte[] getResponse() {
        return this.response;
    }

}
