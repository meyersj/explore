package com.meyersj.explore.communicate;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.meyersj.explore.ExploreApplication;
import com.meyersj.explore.utilities.Cons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
                            Thread.sleep(Cons.PROTOCOL_POLL);
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
