package com.meyersj.tracker.register;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.meyersj.tracker.ui.MainActivity;
import com.meyersj.tracker.socket.Protocol;
import com.meyersj.tracker.R;
import com.meyersj.tracker.socket.SendMessage;
import com.meyersj.tracker.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RegisterBeaconFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.beacon_name) EditText beaconNameEditText;
    @Bind(R.id.start_scan_beacon) Button startScanButton;
    @Bind(R.id.stop_scan_beacon) Button stopScanButton;
    @Bind(R.id.nearby_list) ListView nearbyList;
    @Bind(R.id.register_beacon) Button registerBeaconButton;

    private RegisterScanner scanner;
    private NearbyAdapter nearbyAdapter;
    private NearbyBeacon selectedBeacon;

    public static RegisterBeaconFragment newInstance(int sectionNumber) {
        RegisterBeaconFragment fragment = new RegisterBeaconFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RegisterBeaconFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register_beacon, container, false);
        ButterKnife.bind(this, rootView);
        setRetainInstance(true);
        Log.d(TAG, "Device: " + Utils.getDeviceID(getContext()));
        ArrayList<NearbyBeacon> resultsList = new ArrayList<>();
        nearbyAdapter = new NearbyAdapter(getContext(), resultsList);
        nearbyList.setAdapter(nearbyAdapter);
        scanner = new RegisterScanner(nearbyAdapter);
        setListeners();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG, "save state");
        state.putString("beacon_name", beaconNameEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "activity created");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            beaconNameEditText.setText(savedInstanceState.getString("beacon_name"));
        }
    }

    private void setListeners() {

        registerBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedBeacon == null) {
                    Toast.makeText(getActivity(), "No beacon selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    String beaconName = beaconNameEditText.getText().toString();
                    if(beaconName.isEmpty()) {
                        Toast.makeText(getActivity(), "Beacon name is required", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        byte[] rawBytes = selectedBeacon.result.getScanRecord().getBytes();
                        Log.d(TAG, Utils.getHexString(rawBytes));
                        byte[] payload = Protocol.registerBeacon(beaconName.getBytes(), rawBytes);
                        new Thread(new SendMessage(getContext(), payload)).start();
                    }
                }

            }
        });

        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBeacon = nearbyAdapter.getItem(i);
                nearbyAdapter.setActiveBeacon(selectedBeacon);
            }
        });

        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Start Calibration", Toast.LENGTH_SHORT).show();
                scanner.start();
            }
        });

        stopScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stop Calibration", Toast.LENGTH_SHORT).show();
                scanner.stop();
            }
        });

    }

}