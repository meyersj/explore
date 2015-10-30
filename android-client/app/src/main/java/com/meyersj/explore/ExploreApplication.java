package com.meyersj.explore;

import android.app.Application;
import android.content.Context;

import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.utilities.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jeff on 10/30/15.
 */
public class ExploreApplication extends Application {

    private Socket socket;
    private AdvertisementCommunicator communicator;

    private Socket newSocket(Context context) throws IOException {
        String host = Utils.getHost(context);
        Integer port = Utils.getPort(context);
        return new Socket(host, port);
    }

    public Socket getProtocolConnection(Context context) throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = newSocket(context);
            return socket;
        }
        else {
            return socket;
        }
    }

    public void closeProtocolConnection() throws IOException {
        if (socket != null && socket.isConnected()) {
            DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
            outStream.write(Protocol.closeConnection());
            socket.close();
        }
    }

    //public AdvertisementCommunicator getCommunicator(Context context) {
    //    if (communicator == null) {
    //        communicator = new AdvertisementCommunicator(context);
    //    }
    //}

}
