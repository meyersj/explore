package com.meyersj.explore.explore2;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.meyersj.explore.R;
import com.meyersj.explore.activity.MainActivity2;
import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.communicate.MessageBuilder;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.communicate.ProtocolMessage;
import com.meyersj.explore.explore.MessageDisplay;
import com.meyersj.explore.explore.MessageDisplayAdapter;
import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatFragment extends Fragment {

    private static final String TAB_NUMBER = "tab_number";
    private final String TAG = getClass().getCanonicalName();
    private MessageDisplayAdapter messageDisplayAdapter;
    private AdvertisementCommunicator communicator;
    private NearbyBeacon selectedBeacon;
    private byte[] activeChannel = null;

    @Bind(R.id.location_text) TextView beaconText;
    @Bind(R.id.status_text) TextView statusText;
    @Bind(R.id.display_list) ListView displayList;
    @Bind(R.id.message) EditText messageText;
    @Bind(R.id.save_message_icon) ImageView saveMessageButton;
    @Bind(R.id.display_layout) LinearLayout displayLayout;

    public static ChatFragment newInstance(int tabNumber) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);
        communicator =  ((MainActivity2) getActivity()).communicator;
        messageDisplayAdapter = new MessageDisplayAdapter(getContext());
        displayList.setAdapter(messageDisplayAdapter);
        setViewListeners();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ON resume");
    }

    private void setViewListeners() {

        saveMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(getActivity());
                String message = messageText.getText().toString().trim();
                if (message.isEmpty()) {
                    statusText.setText(getString(R.string.status_empty_message));
                }
                else {
                    broadcastMessage(message);
                }
            }
        });
    }

    private void updateVisibility() {
        if(messageDisplayAdapter.isEmpty()) {
            displayLayout.setVisibility(View.GONE);
        }
        else {
            displayLayout.setVisibility(View.VISIBLE);
        }
    }

    // callback function from ExploreHandler
    // returns response received from socket
    public void update(Message message) {
        Bundle data = message.getData();
        if (data != null) {
            switch (data.getByte(Cons.PAYLOAD_FLAGS)) {
                case Protocol.SEND_BROADCAST:
                    broadcastMessageResponse(data);
                    break;
                case Protocol.RECEIVE_BROADCAST:
                    receiveBroadcastMessage(data);
                    break;
            }
        }
    }

    public void joinChannel(NearbyBeacon beacon) {
        leaveChannel();
        selectedBeacon = beacon;
        beaconText.setText(selectedBeacon.name);
        byte[] device = Utils.getDeviceID(getContext()).getBytes();
        byte[] payload = MessageBuilder.joinChannel(device, selectedBeacon.mac.getBytes());
        ProtocolMessage protocolMessage = new ProtocolMessage();
        protocolMessage.handler = ProtocolMessage.CHAT_HANDLER;
        protocolMessage.payload = payload;
        protocolMessage.payloadFlag = Protocol.JOIN_CHANNEL;
        communicator.addMessage(protocolMessage);
        activeChannel = selectedBeacon.mac.getBytes();
    }

    public void leaveChannel() {
        if (activeChannel == null) return;
        byte[] device = Utils.getDeviceID(getContext()).getBytes();
        byte[] payload = MessageBuilder.leaveChannel(device, activeChannel);
        ProtocolMessage protocolMessage = new ProtocolMessage();
        protocolMessage.handler = ProtocolMessage.CHAT_HANDLER;
        protocolMessage.payload = payload;
        protocolMessage.payloadFlag = Protocol.LEAVE_CHANNEL;
        communicator.addMessage(protocolMessage);
        selectedBeacon = null;
        activeChannel = null;
        beaconText.setText("");
    }

    private void broadcastMessage(String message) {
        byte[] device = Utils.getDeviceID(getContext()).getBytes();
        byte[] mac = selectedBeacon.mac.getBytes();
        Log.d(TAG, "Broadcast: " + selectedBeacon.mac + " " + message);
        byte[] user = Utils.getUser(getContext()).getBytes();
        byte[] payload = MessageBuilder.broadcastMessage(device, user, message.getBytes(), mac);
        ProtocolMessage protocolMessage = new ProtocolMessage();
        protocolMessage.handler = ProtocolMessage.CHAT_HANDLER;
        protocolMessage.payload = payload;
        protocolMessage.payloadFlag = Protocol.SEND_BROADCAST;
        communicator.addMessage(protocolMessage);
    }

    public void broadcastMessageResponse(Bundle data) {
        Log.d(TAG, "broadcast response");
        byte[] response = data.getByteArray(Cons.RESPONSE);
        if (response != null) {
            try {
                String responseString = new String(response, "UTF-8");
                String[] messages = responseString.split("\t");
                if (messages.length == 3) {
                    String client = messages[1];
                    String message = messages[2];
                    String display = getString(R.string.status_message_upload_success);
                    //long epoch = System.currentTimeMillis();
                    //SimpleDateFormat format = new SimpleDateFormat("h:mm a");
                    //String timestamp = format.format(new Date(epoch));
                    Log.d(TAG, message);
                    statusText.setText(display);
                }
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, e.toString());
            }
        }
        Log.d(TAG, "protocol message");
    }

    public void receiveBroadcastMessage(Bundle data) {
        Log.d(TAG, "receive broadcast message");
        byte[] response = data.getByteArray(Cons.RESPONSE);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        Log.d(TAG, new String(response));
        if (flags == null || response == null) return;
        switch (flags[0]) {
            case 0x00:
                try {
                    String responseString = new String(response, "UTF-8");
                    String[] fields = responseString.split("\t");
                    if (fields.length == 2) {
                        String client = fields[0];
                        String message = fields[1];
                        Log.d(TAG, "RECEIVE Broadcast " + client + " " + message);
                        long epoch = System.currentTimeMillis();
                        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
                        String timestamp = format.format(new Date(epoch));
                        Log.d(TAG, timestamp);
                        messageDisplayAdapter.add(new MessageDisplay(client, message, timestamp));
                        updateVisibility();
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, e.toString());
                }
                break;
        }
    }
}