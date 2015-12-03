package com.meyersj.explore.explore2;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment {

    private static final String TAB_NUMBER = "tab_number";
    private final String TAG = getClass().getCanonicalName();
    private AdvertisementCommunicator communicator;
    private NearbyAdapter nearbyAdapter;
    private NearbyBeacon selectedBeacon;
    private boolean scanning = false;

    @Bind(R.id.nearby_list) ListView nearbyList;
    @Bind(R.id.status_text) TextView statusText;
    @Bind(R.id.nearby_layout) LinearLayout beaconLayout;
    @Bind(R.id.action_layout) FrameLayout actionLayout;
    @Bind(R.id.message) EditText messageText;
    @Bind(R.id.save_location_icon) ImageView saveLocation;

    public static SearchFragment newInstance(int tabNumber) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, rootView);
        nearbyAdapter = new NearbyAdapter(getContext(), new ArrayList<NearbyBeacon>());
        nearbyList.setAdapter(nearbyAdapter);
        communicator =  ((MainActivity2) getActivity()).communicator;
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
        selectedBeacon = null;
        nearbyAdapter.setActive(selectedBeacon);
        updateVisibility();
        communicator.start();
        startScan();
        Log.d(TAG, "ON resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
        communicator.stop();
        selectedBeacon = null;
        updateVisibility();
        Log.d(TAG, "ON pause");
    }

    private void setViewListeners() {
        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NearbyBeacon newSelection = nearbyAdapter.getItem(i);
                if (newSelection == null) return;
                int selected = nearbyAdapter.toggleActiveBeacon(newSelection);

                // unselected beacon from list
                if (selected < 0) {
                    selectedBeacon = null;
                    startScan();
                    return;
                }
                selectedBeacon = newSelection;
                updateVisibility();
                if (newSelection.registered) {
                    startChat();
                }
            }
        });

        saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(getActivity());
                String message = messageText.getText().toString().trim();
                if (message.isEmpty()) {
                    statusText.setText(getString(R.string.status_empty_message));
                } else if (selectedBeacon != null) {
                    statusText.setText("");
                    messageText.setText("");
                    registerBeacon(message);
                }
            }
        });
    }

    public void startScan() {
        Log.d(TAG, "start scan");
        if (!scanning) {
            statusText.setText(getString(R.string.status_scan_started));
            scanning = true;
            communicator.startScan();
        }
        nearbyAdapter.setActive(null);
        updateVisibility();
    }

    public void stopScan() {
        if (scanning) {
            communicator.stopScan();
            scanning = false;
        }
    }

    private void updateVisibility() {
        // always show beacons regardless of it one is selected or not
        if (nearbyAdapter.isEmpty()) {
            beaconLayout.setVisibility(View.GONE);
        } else {
            beaconLayout.setVisibility(View.VISIBLE);
        }

        // toggle action view
        if (selectedBeacon != null && !selectedBeacon.registered) {
            actionLayout.setVisibility(View.VISIBLE);
        }
        else {
            actionLayout.setVisibility(View.GONE);
        }
    }

    // callback function from ExploreHandler
    // returns response received from socket
    public void update(Message message) {
        Bundle data = message.getData();
        if (data != null) {
            switch (data.getByte(Cons.PAYLOAD_FLAGS)) {
                case Protocol.BEACON_LOOKUP:
                    clientUpdateResponse(data);
                    break;
                case Protocol.BEACON_REGISTER:
                    registerBeaconResponse(data);
            }
            updateVisibility();
        }
    }

    private void registerBeacon(String name) {
        Log.d(TAG, "Register: " + selectedBeacon.mac);
        byte[] mac = selectedBeacon.mac.getBytes();
        byte[] payload = MessageBuilder.beaconRegister(name.getBytes(), mac);
        ProtocolMessage message = new ProtocolMessage();
        message.handler = ProtocolMessage.SEARCH_HANDLER;
        message.payload = payload;
        message.payloadFlag = Protocol.BEACON_REGISTER;
        communicator.addMessage(message);
    }

    public void clientUpdateResponse(Bundle data) {
        Log.d(TAG, "client update response");
        boolean registered = false;
        String mac = data.getString(Cons.MAC);
        String name = mac;
        Integer rssi = data.getInt(Cons.RSSI);
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        byte[] response = data.getByteArray(Cons.RESPONSE);
        if (flags == null) return;
        Log.d(TAG, String.valueOf(flags[0]));
        switch (flags[0]) {
            case 0x00:
                registered = true;
                if (response != null) {
                    try {
                        name = new String(response, "UTF-8");
                        name = ProtocolMessage.parseBeaconName(name);
                    } catch (UnsupportedEncodingException e) {
                        Log.d(TAG, e.toString());
                    }
                }
                break;
            case 0x01:
                break;
        }
        nearbyAdapter.add(new NearbyBeacon(registered, mac, name, rssi));
    }

    public void registerBeaconResponse(Bundle data) {
        Log.d(TAG, "register message response");
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
                        String name = fields[1];
                        selectedBeacon.registered = true;
                        selectedBeacon.name = name;
                        nearbyAdapter.notifyDataSetChanged();
                        startChat();
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, e.toString());
                }
                break;
        }
    }

    public void startChat() {
        ((MainActivity2) getActivity()).startChat(selectedBeacon);
    }
}