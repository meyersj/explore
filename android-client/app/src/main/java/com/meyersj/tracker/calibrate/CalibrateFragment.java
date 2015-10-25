package com.meyersj.tracker.calibrate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.meyersj.tracker.ui.MainActivity;
import com.meyersj.tracker.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class CalibrateFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final String TAG = getClass().getCanonicalName();

    @Bind(R.id.start_calibrate_button) Button startCalibrateButton;
    @Bind(R.id.stop_calibrate_button) Button stopCalibrateButton;
    private CalibrateScanner scanner;


    public static CalibrateFragment newInstance(int sectionNumber) {
        CalibrateFragment fragment = new CalibrateFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CalibrateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calibrate, container, false);
        ButterKnife.bind(this, rootView);
        setRetainInstance(true);
        scanner = new CalibrateScanner(getContext());
        setListeners();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void setListeners() {
        startCalibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Start Calibration", Toast.LENGTH_SHORT).show();
                scanner.start();
            }
        });

        stopCalibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Stop Calibration", Toast.LENGTH_SHORT).show();
                scanner.stop();
            }
        });
    }
}