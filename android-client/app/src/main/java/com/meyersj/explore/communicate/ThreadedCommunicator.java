package com.meyersj.explore.communicate;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.meyersj.explore.ExploreApplication;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;


public class ThreadedCommunicator {


    private final String TAG = getClass().getCanonicalName();
    protected Context context;
    private Handler handler;
    private LinkedBlockingQueue<ProtocolMessage> queue;
    protected Boolean active;


    public ThreadedCommunicator(Context context, Handler handler) {
        this(context);
        this.handler = handler;
    }

    public ThreadedCommunicator(Context context) {
        this.context = context;
        this.queue = new LinkedBlockingQueue<>();
        this.active = false;
    }

    public void start() {
        if (active) return;
        active = true;
        new Thread(new ScannerThread()).start();
    }

    public void stop() {
        if (active) {
            active = false;
        }
    }

    public void addMessage(ProtocolMessage message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Log.d(TAG, e.toString());
        }
    }

    class ScannerThread implements Runnable {

        @Override
        public void run() {

            try {
                Log.d(TAG, "OPEN SOCKET");
                Socket socket = Protocol.openCommunication(context);
                if (socket != null) {
                    while (active) {
                        ProtocolMessage message = queue.poll();
                        if (message != null) {
                            Message threadMessage = sendMessage(socket, message);
                            handler.sendMessage(threadMessage);
                        } else {
                            DataInputStream inStream = new DataInputStream(socket.getInputStream());
                            if (inStream.available() >= 8) {
                                byte[] responseLength = new byte[4];
                                byte[] flags = new byte[4];
                                byte[] response = null;
                                inStream.readFully(responseLength);
                                inStream.readFully(flags);
                                ByteBuffer wrapped = ByteBuffer.wrap(responseLength);
                                Integer length = wrapped.getInt();
                                //Log.d(TAG, "LENGTH " + String.valueOf(length));
                                //Log.d(TAG, "FLAGS " + Utils.getHexString(flags));
                                if (length > 0) {
                                    response = new byte[length];
                                    inStream.readFully(response);
                                    Log.d(TAG, "RESPONSE " + response.toString());
                                }
                                ProtocolMessage broadcastMessage = new ProtocolMessage();
                                broadcastMessage.handler = ProtocolMessage.CHAT_HANDLER;
                                broadcastMessage.response = response;
                                broadcastMessage.responseFlags = flags;
                                broadcastMessage.payloadFlag = Protocol.RECEIVE_BROADCAST;
                                handler.sendMessage(broadcastMessage.getThreadMessage());
                            }
                            else {
                                Thread.sleep(Cons.PROTOCOL_POLL);
                            }
                        }
                    }
                    Log.d(TAG, "CLOSE SOCKET");
                    Protocol.closeCommunication(socket);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, e.toString());
            }
            catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    public static Message checkBroadcast(Socket socket, ProtocolMessage message) throws IOException {
        Log.d("ThreadedCommunicator", "check broadcast");
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        if (inStream.available() > 8) {
            ProtocolResponse response = ProtocolResponse.read(inStream);
            message.response = response.getResponse();
            Log.d("ThreadedCommunicator", "Broadcast: " + message.response.toString());
        }
        return null;
    }


    public static Message sendMessage(Socket socket, ProtocolMessage message) throws IOException {
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        outStream.write(message.payload);
        return getResponse(message, inStream).getThreadMessage();
    }

    public static ProtocolMessage getResponse(ProtocolMessage message, DataInputStream inStream) throws IOException {
        ProtocolResponse response = ProtocolResponse.read(inStream);
        message.response = response.getResponse();
        message.responseFlags = response.getFlags();
        return message;
    }
}
