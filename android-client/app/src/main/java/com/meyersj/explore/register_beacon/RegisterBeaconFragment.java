package com.meyersj.explore.register_beacon;

import android.content.Context;
import android.location.Location;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.meyersj.explore.R;
import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.communicate.ProtocolMessage;
import com.meyersj.explore.communicate.ResponseHandler;
import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RegisterBeaconFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.beacon_name) EditText beaconNameEditText;
    @Bind(R.id.start_button) Button startScanButton;
    @Bind(R.id.stop_button) Button stopScanButton;
    @Bind(R.id.nearby_list) ListView nearbyList;
    @Bind(R.id.register_beacon) ImageView registerBeaconButton;
    @Bind(R.id.status_text) TextView statusText;
    @Bind(R.id.coordinates_text) TextView coordinatesText;

    private AdvertisementCommunicator communicator;
    private RegisterBeaconAdapter registerBeaconAdapter;
    private NearbyBeacon selectedBeacon;
    private String beaconName;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdates = false;
    private Location currentLocation;


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
        registerBeaconAdapter = new RegisterBeaconAdapter(getContext(), resultsList);
        nearbyList.setAdapter(registerBeaconAdapter);
        communicator = new AdvertisementCommunicator(getContext(), new ResponseHandler(this));
        communicator.start();
        buildGoogleApiClient();
        createLocationRequest();
        setListeners();
        return rootView;
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

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    private void setListeners() {

        registerBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Utils.hideKeyboard(getActivity());
                if(selectedBeacon == null) {
                    statusText.setText("No beacon selected");
                }
                else {
                    beaconName = beaconNameEditText.getText().toString();
                    if(beaconName.isEmpty()) {
                        statusText.setText("Beacon name is required");
                    }
                    else {
                        //Double lat = selectedBeacon.lat;
                        byte[] lat = selectedBeacon.getLatitudeBytes();
                        byte[] lon = selectedBeacon.getLongitudeBytes();
                        byte[] rawBytes = selectedBeacon.advertisement;
                        Log.d(TAG, Utils.getHexString(rawBytes));
                        byte[] payload = Protocol.registerBeacon(beaconName.getBytes(), rawBytes, lat, lon);
                        ProtocolMessage message = new ProtocolMessage();
                        message.payload = payload;
                        message.payloadFlag = Protocol.REGISTER_BEACON;
                        communicator.addMessage(message);
                        //RegisterBeaconAsync registerAsync = new RegisterBeaconAsync();
                        //byte[][] payloads = {payload};
                        //registerAsync.execute(payloads);
                    }
                }
            }
        });

        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBeacon = registerBeaconAdapter.getItem(i);
                if (currentLocation != null) {
                    selectedBeacon.lat = currentLocation.getLatitude();
                    selectedBeacon.lon = currentLocation.getLongitude();
                }
                registerBeaconAdapter.toggleActiveBeacon(selectedBeacon);
                if (beaconNameEditText.getText().toString().isEmpty()) {
                    //TODO open up keyboard for edit text and focus it
                }
                else {
                    //Utils.hideKeyboard(getActivity());
                }
            }
        });

        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"start scan");
                if (!requestingLocationUpdates) {
                    Log.d(TAG,"start scan now");
                    requestingLocationUpdates = true;
                    googleApiClient.connect();
                    //Utils.hideKeyboard(getActivity());
                    statusText.setText("Starting beacon scan");
                    registerBeaconAdapter.clear();
                    communicator.startScan();
                }
            }
        });

        stopScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestingLocationUpdates) {
                    stopLocationUpdates();
                    //Utils.hideKeyboard(getActivity());
                    statusText.setText("Stopping beacon scan");
                    registerBeaconAdapter.clear();
                    communicator.stopScan();
                }
            }
        });

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "on connected");
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        String accuracy = Double.toString(location.getAccuracy());
        String coordinates = lat + " " + lon + " (" + accuracy + " accuracy)";
        if (selectedBeacon != null) {
            selectedBeacon.lat = location.getLatitude();
            selectedBeacon.lon = location.getLongitude();
        }
        coordinatesText.setText(coordinates);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        requestingLocationUpdates = true;
    }

    protected void stopLocationUpdates() {
        if (requestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
             googleApiClient.disconnect();
            requestingLocationUpdates = false;
        }
    }

    /*
    public class RegisterBeaconAsync extends AsyncTask<byte[], Void, String> {

        private boolean success = false;

        @Override
        protected String doInBackground(byte[]... payloads) {
            String display;
            Socket socket;
            try {
                socket = Protocol.openCommunication(getContext());
                if (socket != null) {
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inStream = new DataInputStream(socket.getInputStream());
                    Log.d(TAG, "write " + Utils.getHexString(payloads[0]));
                    outStream.write(payloads[0]);

                    ProtocolResponse response = ProtocolResponse.read(inStream);
                    Byte flag = response.getFlags()[0];

                    if (flag.equals(Protocol.SUCCESS)) {
                        display = "Registered beacon <" + beaconName + "> successfully";
                        success = true;
                    }
                    else {
                        display = "Error: Server failed to register beacon";
                    }

                    Protocol.closeCommunication(socket);
                }
                else {
                    display = "Error: Failed to open socket";

                }
            } catch (IOException e) {
                display = "Error: IOException: " + e.toString();
            }
            return display;
        }
        protected void onPostExecute(String response) {
            Log.d(TAG, response);
            statusText.setText(response);
            if (success) {
                stopLocationUpdates();
                communicator.stop();
                beaconNameEditText.setText("");
            }
        }
    }
    */

    // callback function from ExploreHandler
    // returns response received from socket
    public void update(Message message) {
        Bundle data = message.getData();
        Log.d(TAG, "update");
        if (data != null) {
            boolean registered = false;
            byte[] advertisement = data.getByteArray(Cons.ADVERTISEMENT);
            String  name = data.getString(Cons.BEACON_KEY);
            Integer rssi = data.getInt(Cons.RSSI);
            byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
            Log.d(TAG, "FLAG: " + flags[0]);

            switch(data.getByte(Cons.PAYLOAD_FLAGS)) {
                case Protocol.REGISTER_BEACON:
                    if (flags[0] == Protocol.SUCCESS) {
                        statusText.setText("Registered beacon <" + beaconName + "> successfully");
                    }
                    return;
            }


            switch (flags[0]) {
                case 0x00:
                    registered = true;
                    byte[] response = data.getByteArray(Cons.RESPONSE);
                    if (response != null) {
                        try {
                            name = new String(response, "UTF-8");
                            name = ProtocolMessage.parseBeaconName(name);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 0x01:
                    break;
            }
            registerBeaconAdapter.add(new NearbyBeacon(registered, advertisement, name, rssi));

            nearbyList.invalidate();
        }
    }
}