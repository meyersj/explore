package com.meyersj.explore.explore;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.meyersj.explore.R;
import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.communicate.ProtocolMessage;
import com.meyersj.explore.communicate.ResponseHandler;
import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExploreFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.start_button) Button startButton;
    @Bind(R.id.stop_button) Button stopButton;
    @Bind(R.id.status_text) TextView statusText;
    @Bind(R.id.nearby_list) ListView nearbyList;
    @Bind(R.id.display_list) ListView displayList;
    @Bind(R.id.message) EditText messageText;
    @Bind(R.id.save_message_icon) ImageView saveMessageButton;
    @Bind(R.id.beacon_layout) LinearLayout beaconLayout;
    @Bind(R.id.display_layout) LinearLayout displayLayout;
    @Bind(R.id.action_layout) FrameLayout messageLayout;
    @Bind(R.id.action_icon) ImageView actionIcon;

    private AdvertisementCommunicator communicator;
    private ExploreBeaconAdapter exploreBeaconAdapter;
    private MessageDisplayAdapter messageDisplayAdapter;
    private boolean scanning = false;
    private NearbyBeacon selectedBeacon;
    private InputMode actionModeInput;

        public static ExploreFragment newInstance(int sectionNumber) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ExploreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "create view");
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.bind(this, rootView);
        exploreBeaconAdapter = new ExploreBeaconAdapter(getContext(), new ArrayList<NearbyBeacon>());
        nearbyList.setAdapter(exploreBeaconAdapter);
        messageDisplayAdapter = new MessageDisplayAdapter(getContext());
        displayList.setAdapter(messageDisplayAdapter);
        communicator = new AdvertisementCommunicator(getContext(), new ResponseHandler(this));
        actionModeInput = new InputMode(getContext(), actionIcon, messageText);
        communicator.start();
        setViewListeners();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG, "save instance state");
        state.putString("status", statusText.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        communicator.stopScan();
        communicator.stop();
    }

    private void setViewListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stopScan() will change scanning flag so we
                // need to check to update button status properly
                stopScan();
                view.setSelected(true);
            }
        });

        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NearbyBeacon newSelected = exploreBeaconAdapter.getItem(i);
                if (newSelected == null) return;
                Log.d(TAG, Boolean.toString(newSelected.registered));
                int selected = exploreBeaconAdapter.toggleActiveBeacon(newSelected);
                switch (selected) {
                    case -1:
                        // undo selection
                        selectedBeacon = null;
                        messageDisplayAdapter.clear();
                        updateVisibility();
                        return;
                }

                if (newSelected.registered) {
                    switch (selected) {
                        case 0:
                            // change selection, clear old messages and fetch new
                            stopScan();
                            messageDisplayAdapter.clear();
                            fetchBeaconMessages(newSelected);
                            selectedBeacon = newSelected;
                            actionModeInput.activateMessage();
                            break;
                        case 1:
                            // create selection, fetch new messages
                            stopScan();
                            fetchBeaconMessages(newSelected);
                            selectedBeacon = newSelected;
                            actionModeInput.activateMessage();
                            break;
                    }
                } else {
                    switch (selected) {
                        case 0:
                            stopScan();
                            messageDisplayAdapter.clear();
                            fetchBeaconMessages(newSelected);
                            selectedBeacon = newSelected;
                            actionModeInput.activateRegister();
                            break;
                        case 1:
                            stopScan();
                            fetchBeaconMessages(newSelected);
                            selectedBeacon = newSelected;
                            actionModeInput.activateRegister();
                            break;
                    }
                }
                updateVisibility();
            }
        });

        saveMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(getActivity());
                String message = messageText.getText().toString();
                if (message.isEmpty()) {
                    statusText.setText("Message is empty");
                } else if (selectedBeacon != null) {
                    statusText.setText("");
                    messageText.setText("");

                    String mode = actionModeInput.getMode();
                    if (mode.isEmpty()) {
                        return;
                    }
                    if (mode.equals(InputMode.MESSAGE)) {
                        putMessage(message);
                    } else if (mode.equals(InputMode.REGISTER)) {
                        registerBeacon(message);
                        actionModeInput.activateMessage();
                    }
                }
            }
        });

        messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "message text opened");

            }
        });
    }

    // callback function from ExploreHandler
    // returns response received from socket
    public void update(Message message) {
        Log.d(TAG, "update");
        Bundle data = message.getData();
        if (data != null) {
            switch(data.getByte(Cons.PAYLOAD_FLAGS)) {
                case Protocol.PUT_MESSAGE:
                    putMessageResponse(data);
                    break;
                case Protocol.CLIENT_UPDATE:
                    clientUpdateResponse(data);
                    break;
                case Protocol.GET_MESSAGE:
                    getMessageResponse(data);
                    break;
                case Protocol.REGISTER_BEACON:
                    getRegisterBeaconMessage(data);
                    break;
            }
            updateVisibility();
        }
    }

    private void startScan() {
        if (!scanning) {
            statusText.setText("Scan started");
            communicator.startScan();
            selectedBeacon = null;
            scanning = true;
            startButton.setSelected(true);
            stopButton.setSelected(false);
        }
        exploreBeaconAdapter.clear();
        exploreBeaconAdapter.setActive(null);
        messageDisplayAdapter.clear();
        updateVisibility();
    }

    private void stopScan() {
        if(scanning) {
            communicator.stopScan();
            statusText.setText("Scan stopped");
            scanning = false;
            startButton.setSelected(false);
            stopButton.setSelected(false);
        }
        updateVisibility();
    }

    private void updateVisibility() {
        // always show beacons regardless of it one is selected or not
        if (exploreBeaconAdapter.isEmpty()) {
            beaconLayout.setVisibility(View.GONE);
        }
        else {
            beaconLayout.setVisibility(View.VISIBLE);
        }
        // if beacon is selected show input message view
        // and if display messages are not empty then show that
        if (selectedBeacon != null) {
            messageLayout.setVisibility(View.VISIBLE);
            if(messageDisplayAdapter.isEmpty()) {
                displayLayout.setVisibility(View.GONE);
            }
            else {
                displayLayout.setVisibility(View.VISIBLE);
            }
        }
        else {
            // no beacon selected so input message view and message display should not be visible
            messageLayout.setVisibility(View.GONE);
            displayLayout.setVisibility(View.GONE);
        }
    }

    private void fetchBeaconMessages(NearbyBeacon selectedBeacon) {
        byte[] payload = Protocol.getMessages(selectedBeacon.advertisement);
        ProtocolMessage protocolMessage = new ProtocolMessage();
        protocolMessage.payload = payload;
        protocolMessage.payloadFlag = Protocol.GET_MESSAGE;
        communicator.addMessage(protocolMessage);
    }

    private void putMessage(String message) {
        byte[] device = Utils.getDeviceID(getContext()).getBytes();
        byte[] beacon = selectedBeacon.advertisement;
        byte[] user = Utils.getUser(getContext()).getBytes();
        byte[] payload = Protocol.sendMessage(device, user, message.getBytes(), beacon);
        ProtocolMessage protocolMessage = new ProtocolMessage();
        protocolMessage.payload = payload;
        protocolMessage.payloadFlag = Protocol.PUT_MESSAGE;
        communicator.addMessage(protocolMessage);
    }

    private void registerBeacon(String name) {
        //Double lat = selectedBeacon.lat;
        byte[] lat = selectedBeacon.getLatitudeBytes();
        byte[] lon = selectedBeacon.getLongitudeBytes();
        byte[] rawBytes = selectedBeacon.advertisement;
        Log.d(TAG, Utils.getHexString(rawBytes));
        byte[] payload = Protocol.registerBeacon(name.getBytes(), rawBytes, lat, lon);
        ProtocolMessage message = new ProtocolMessage();
        message.payload = payload;
        message.payloadFlag = Protocol.REGISTER_BEACON;
        communicator.addMessage(message);
    }

    public void clientUpdateResponse(Bundle data) {
        boolean registered = false;
        byte[] advertisement = data.getByteArray(Cons.ADVERTISEMENT);
        String  name = data.getString(Cons.BEACON_KEY);
        Integer rssi = data.getInt(Cons.RSSI);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        byte[] response = data.getByteArray(Cons.RESPONSE);
        if (flags == null) return;

        switch (flags[0]) {
            case 0x00:
                registered = true;
                if (response != null) {
                    try {
                        name = new String(response, "UTF-8");
                        name = ProtocolMessage.parseBeaconName(name);
                        Log.d(TAG, "hey " + name);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 0x01:
                break;
        }
        exploreBeaconAdapter.add(new NearbyBeacon(registered, advertisement, name, rssi));
    }

    public boolean inLast24(Date aDate) {
        return aDate.getTime() > System.currentTimeMillis() - Cons.HOURS_24;
    }

    public void putMessageResponse(Bundle data) {
        byte[] response = data.getByteArray(Cons.RESPONSE);
        if (response != null) {
            try {
                String responseString = new String(response, "UTF-8");
                String[] messages = responseString.split("\t");
                if (messages.length == 3) {
                    String beacon = messages[0];
                    String client = messages[1];
                    String message = messages[2];
                    String display = "Successfully uploaded message";
                    //String display = "(" + beacon + ") " + client + ": " + message;
                    long epoch = System.currentTimeMillis();
                    SimpleDateFormat format = new SimpleDateFormat("h:mm a");
                    String timestamp = format.format(new Date(epoch));
                    messageDisplayAdapter.add(new MessageDisplay(client, message, timestamp));
                    if (display.length() > 144) {
                        display = display.substring(0, 144);
                    }
                    statusText.setText(display);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "protocol message");
    }

    public void getMessageResponse(Bundle data) {
        Log.d(TAG, "message response");
        byte[] response = data.getByteArray(Cons.RESPONSE);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        Log.d(TAG,"get messsage");
        if (flags == null) return;
        switch (flags[0]) {
            case 0x00:
                if (response != null) {
                    try {
                        String responseString = new String(response, "UTF-8");
                        String[] messages = responseString.split("\n");
                        // parse and reverse order of messages
                        for (int i = messages.length - 1; i >= 0; i--) {
                            Log.d(TAG, messages[i]);
                            String[] fields = messages[i].split("\t");
                            if (fields.length == 4) {
                                String username = fields[1];
                                String message = fields[2];
                                String epochString = fields[3];
                                Log.d(TAG, epochString);
                                long epoch = Long.parseLong(epochString) * 1000;
                                Date date = new Date(epoch);
                                SimpleDateFormat format = new SimpleDateFormat("MM/dd ");
                                if (inLast24(date)) {
                                    format = new SimpleDateFormat("h:mm a");
                                }
                                String timestamp = format.format(new Date(epoch));
                                Log.d(TAG, timestamp);
                                messageDisplayAdapter.add(new MessageDisplay(username, message, timestamp));
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 0x01:
                break;
        }
    }

    public void getRegisterBeaconMessage(Bundle data) {
        Log.d(TAG, "message response");
        byte[] response = data.getByteArray(Cons.RESPONSE);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        Log.d(TAG, new String(response));
        if (flags == null || response == null) return;
        switch (flags[0]) {
            case 0x00:
                try {
                    String responseString = new String(response, "UTF-8");
                    String[] fields = responseString.split("\t");
                    if (fields.length == 3) {
                        //String key = fields[0];
                        String name = fields[1];
                        //String coordinates = fields[2];
                        selectedBeacon.registered = true;
                        selectedBeacon.beaconKey = name;
                        exploreBeaconAdapter.notifyDataSetChanged();
                        statusText.setText("Location saved\n...store a message below");
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, e.toString());
                }
                break;
        }
    }
}