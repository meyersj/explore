package com.meyersj.tracker.register;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meyersj.tracker.R;
import com.meyersj.tracker.Utils;
import com.meyersj.tracker.socket.Protocol;
import com.meyersj.tracker.socket.SendMessage;
import com.meyersj.tracker.ui.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RegisterClientFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    private String clientName;
    private String deviceID;
    private Boolean registered = false;

    @Bind(R.id.client_name) EditText clientNameEditText;
    @Bind(R.id.register_client) Button registerClientButton;
    @Bind(R.id.status_text) TextView statusText;

    public static RegisterClientFragment newInstance(int sectionNumber) {
        RegisterClientFragment fragment = new RegisterClientFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RegisterClientFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register_client, container, false);
        ButterKnife.bind(this, rootView);
        setRetainInstance(true);
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
        state.putString("client_name", clientNameEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "activity created");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            clientNameEditText.setText(savedInstanceState.getString("client_name"));
        }
    }

    private void setListeners() {
        registerClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceID = Utils.getDeviceID(getContext());
                clientName = clientNameEditText.getText().toString();
                if(clientName.isEmpty()) {
                    Toast.makeText(getActivity(), "Client name is required", Toast.LENGTH_SHORT).show();
                }
                else {
                    byte[] payload = Protocol.registerClient(deviceID.getBytes(), clientName.getBytes());
                    new Thread(new SendMessage(getContext(), payload)).start();
                    //statusText.post(new RegisterMessage(getContext(), payload));

                    //RegisterAsync a = new RegisterAsync();
                    //byte[] payloads = {payload};
                    //a.execute(payloads);
                }
            }
        });
    }

    /*
    public class RegisterAsync extends AsyncTask<Byte, Void, Void> {

        @Override
        protected Void doInBackground(Byte... payloads) {
            Socket socket;
            try {
                socket = new Socket("192.168.1.101", 8082);
                if (socket != null) {
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    //Log.d(TAG, "write " + Utils.getHexString(payloads[0]));
                    outStream.write(payloads[0]);
                    outStream.write(Protocol.closeConnection());

                    DataInputStream inStream = new DataInputStream(socket.getInputStream());
                    Byte b = inStream.readByte();
                }
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }


            return null;
        }
    }

    public class RegisterMessage extends SendMessage {

        public RegisterMessage(Context context, byte[] payload) {
            super(context, payload);
        }

        @Override
        public void run() {
            Log.d(TAG, "send message");

            if (openSocket()) {
                send(payload);
                Byte response = receive();
                if (response != null && response.equals(Protocol.SUCCESS)) {
                    statusText.setText("Updated client\n");
                }
                else {
                    statusText.setText("Failed to updated client\n");
                }
                closeSocket();
            }
        }

    }
    */

}