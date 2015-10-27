package com.meyersj.tracker.explore;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meyersj.tracker.ui.MainActivity;
import com.meyersj.tracker.R;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ExploreFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.start_button) Button startButton;
    @Bind(R.id.stop_button) Button stopButton;
    @Bind(R.id.status_text) TextView statusText;

    private int count = 0;
    private ExplorerCommunicator ExplorerCommunicator;

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
        ExplorerCommunicator = new ExplorerCommunicator(getContext(), new ExploreHandler(this));
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
        ExplorerCommunicator.stop();
    }

    private void setViewListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Start Exploring", Toast.LENGTH_SHORT).show();
                ExplorerCommunicator.start();
                statusText.setText("Scan started");
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stop Exploring", Toast.LENGTH_SHORT).show();
                ExplorerCommunicator.stop();
                statusText.setText("Scan stopped");
            }
        });
    }


    public void update(Message message) {
        byte flag = message.getData().getByte("response_flag");
        if (flag == 0x00) {
            byte[] response = message.getData().getByteArray("response");
            try {
                String responseString = new String(response, "UTF-8");
                statusText.setText(String.valueOf(++count) + " " + responseString);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

}