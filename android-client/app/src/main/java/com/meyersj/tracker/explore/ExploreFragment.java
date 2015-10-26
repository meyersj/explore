package com.meyersj.tracker.explore;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meyersj.tracker.socket.SendMessage;
import com.meyersj.tracker.ui.MainActivity;
import com.meyersj.tracker.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ExploreFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.start_button) Button startButton;
    @Bind(R.id.stop_button) Button stopButton;
    @Bind(R.id.status_text) TextView statusText;

    private ExploreScanner scanner;

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
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.bind(this, rootView);
        scanner = new ExploreScanner(getContext(), statusText);
        // add START and STOP scan button listeners
        setViewListeners();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void setViewListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Start Discovery", Toast.LENGTH_SHORT).show();
                scanner.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stop Discovery", Toast.LENGTH_SHORT).show();
                scanner.stop();
            }
        });
    }


}