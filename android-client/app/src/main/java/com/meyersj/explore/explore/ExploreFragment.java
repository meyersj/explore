package com.meyersj.explore.explore;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.activity.MainActivity;
import com.meyersj.explore.R;
import com.meyersj.explore.nearby.NearbyBeacon;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ExploreFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.start_button) Button startButton;
    @Bind(R.id.stop_button) Button stopButton;
    @Bind(R.id.status_text) TextView statusText;
    @Bind(R.id.nearby_list) ListView nearbyList;


    private com.meyersj.explore.communicate.AdvertisementCommunicator communicator;
    private ExploreBeaconAdapter exploreBeaconAdapter;

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
        ArrayList<NearbyBeacon> resultsList = new ArrayList<>();
        exploreBeaconAdapter = new ExploreBeaconAdapter(getContext(), resultsList);
        nearbyList.setAdapter(exploreBeaconAdapter);
        communicator = new AdvertisementCommunicator(getContext(), new ExploreHandler(this));
        setViewListeners();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG, "save instance state");
        //state.putCharSequence(App.VSTUP, vstup.getText());
        state.putString("status", statusText.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "on attach");
        super.onAttach(context);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "destroy view");
        communicator.stop();
    }

    private void setViewListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Start Exploring", Toast.LENGTH_SHORT).show();
                communicator.start();
                statusText.setText("Scan started");
                exploreBeaconAdapter.clear();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stop Exploring", Toast.LENGTH_SHORT).show();
                communicator.stop();
                statusText.setText("Scan stopped");
                exploreBeaconAdapter.clear();
            }
        });
    }

    // callback function from ExploreHandler
    // returns response received from socket
    public void update(Message message) {
        Bundle data = message.getData();
        if (data != null) {
            boolean registered = false;
            byte[] advertisement = data.getByteArray("advertisement");
            String  name = data.getString("hash");
            Integer rssi = data.getInt("rssi");
            byte flag = data.getByte("response_flag");
            Log.d(TAG, "FLAG: " + flag);
            switch (flag) {

                case 0x00:
                    registered = true;
                    byte[] response = data.getByteArray("response");
                    if (response != null) {
                        try {
                            name = new String(response, "UTF-8");
                            name = parseResponseName(name);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 0x01:
                    name = "Unregistered";
                    break;
            }
            exploreBeaconAdapter.add(new NearbyBeacon(registered, advertisement, name, rssi));
        }
    }

    public String parseResponseName(String value) {
        String[] split1 = value.split("\\|");
        if (split1.length == 2) {
            String[] split2 = split1[1].split(":");
            if (split2.length == 2) {
                String name = split2[0];
                String coordinates = split2[1];
                return name + " " + coordinates;
            }
        }
        return "Unregistered";

    }


}