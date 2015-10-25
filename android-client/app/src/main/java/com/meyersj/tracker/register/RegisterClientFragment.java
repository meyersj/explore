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
        state.putString("status_text", statusText.getText().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "activity created");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            clientNameEditText.setText(savedInstanceState.getString("client_name"));
            statusText.setText(savedInstanceState.getString("status_text"));
        }
    }

    private void setListeners() {
        registerClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(getActivity());
                deviceID = Utils.getDeviceID(getContext());
                clientName = clientNameEditText.getText().toString();
                if(clientName.isEmpty()) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                    statusText.setText("Error: Client name is required");
                }
                else {
                    byte[] payload = Protocol.registerClient(deviceID.getBytes(), clientName.getBytes());
                    RegisterClientAsync registerAsync = new RegisterClientAsync();
                    byte[][] payloads = {payload};
                    registerAsync.execute(payloads);
                }
            }
        });
    }


    public class RegisterClientAsync extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... payloads) {
            String response;
            Socket socket;
            try {
                socket = Utils.openSocket(getContext());
                if (socket != null) {
                    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                    Log.d(TAG, "write " + Utils.getHexString(payloads[0]));
                    outStream.write(payloads[0]);
                    DataInputStream inStream = new DataInputStream(socket.getInputStream());
                    Byte respByte = inStream.readByte();
                    if (respByte != null && respByte.equals(Protocol.SUCCESS)) {
                        response = "Registered client <" + clientName + "> successfully";
                    }
                    else {
                        response = "Error: Server failed to save name";
                    }
                    outStream.write(Protocol.closeConnection());
                    socket.close();
                }
                else {
                    response = "Error: Failed to open socket";

                }
            } catch (IOException e) {
                response = "Error: IOException: " + e.toString();
            }
            return response;
        }
        protected void onPostExecute(String response) {
            Log.d(TAG, response);
            statusText.setText(response);
        }
    }

}