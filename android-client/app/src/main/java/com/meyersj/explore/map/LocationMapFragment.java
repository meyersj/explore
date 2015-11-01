package com.meyersj.explore.map;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
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

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.explore.R;
import com.meyersj.explore.communicate.AdvertisementCommunicator;
import com.meyersj.explore.communicate.Protocol;
import com.meyersj.explore.communicate.ProtocolMessage;
import com.meyersj.explore.communicate.ResponseHandler;
import com.meyersj.explore.communicate.ThreadedCommunicator;
import com.meyersj.explore.explore.ExploreBeaconAdapter;
import com.meyersj.explore.explore.InputMode;
import com.meyersj.explore.explore.MessageDisplay;
import com.meyersj.explore.explore.MessageDisplayAdapter;
import com.meyersj.explore.nearby.NearbyBeacon;
import com.meyersj.explore.utilities.Cons;
import com.meyersj.explore.utilities.Utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LocationMapFragment extends Fragment {

    private static final String TAB_NUMBER = "tab_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.mapview) MapView mapView;
    @Bind(R.id.gps_location) FloatingActionButton gpsLocationIcon;

    private ThreadedCommunicator communicator;

    public static LocationMapFragment newInstance(int tabNumber) {
        LocationMapFragment fragment = new LocationMapFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LocationMapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);
        communicator = new ThreadedCommunicator(getContext(), new ResponseHandler(this));
        communicator.start();
        initializeMap(savedInstanceState);
        gpsLocationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapView.isMyLocationEnabled()) {
                    mapView.setMyLocationEnabled(false);
                }
                else {
                    mapView.setMyLocationEnabled(true);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        mapView.onSaveInstanceState(state);
        super.onSaveInstanceState(state);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        communicator.stop();
        mapView.onDestroy();
        mapView = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mapView.setMyLocationEnabled(false);
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mapView.setMyLocationEnabled(true);
        mapView.onResume();
    }


    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    private void initializeMap(Bundle savedInstanceState) {
        mapView.setAccessToken(Utils.getMapboxToken(getContext()));
        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        LatLng portland = new LatLng(45.5, -122.5);
        mapView.setCenterCoordinate(portland);
        mapView.setZoomLevel(8);
        mapView.onCreate(savedInstanceState);
    }

    // callback function from ResponseHandler
    // returns response received from socket
    public void update(Message message) {
        Log.d(TAG, "update");
        Bundle data = message.getData();
        if (data != null) {
            switch(data.getByte(Cons.PAYLOAD_FLAGS)) {
                case Protocol.GET_BEACONS:
                    getBeaconsResponse(data);
                    break;
            }
        }
    }

    public void getBeaconsResponse(Bundle data) {
        Log.d(TAG, "get beacons");
        byte[] flags = data.getByteArray(Cons.RESPONSE_FLAGS);
        byte[] response = data.getByteArray(Cons.RESPONSE);
        if (flags == null || response == null) return;

        if (flags[0] == 0x00) {
            mapView.removeAllAnnotations();
            try {
                String responseString = new String(response, "UTF-8");
                String[] beacons = responseString.split("\n");
                for(String beacon: beacons) {
                    String[] fields = beacon.split("\t");
                    Log.d(TAG, beacon);
                    if (fields.length == 3) {
                        String key = fields[0];
                        String name = fields[1];
                        String coordinatesString = fields[2];
                        Log.d(TAG, coordinatesString);
                        String[] coordinates = coordinatesString.split(" ");
                        if (coordinates.length == 2) {
                            Double lat = Double.parseDouble(coordinates[0]);
                            Double lon = Double.parseDouble(coordinates[1]);
                            LatLng location = new LatLng(lat, lon);
                            addMarker(location, name, key);;
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    public void addMarker(LatLng location, String title, String snippet) {
        if (mapView != null) {
            mapView.addMarker(new MarkerOptions()
                    .position(location).title(title).snippet(snippet));
        }
    }

    public void fetchBeaconLocations() {
        ProtocolMessage message = new ProtocolMessage();
        message.payloadFlag = Protocol.GET_BEACONS;
        byte[] empty = new byte[0];
        message.payload = Protocol.newPayload(Protocol.GET_BEACONS, empty);
        communicator.addMessage(message);
    }
}