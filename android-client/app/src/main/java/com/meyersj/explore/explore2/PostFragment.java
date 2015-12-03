package com.meyersj.explore.explore2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meyersj.explore.R;

import butterknife.ButterKnife;

public class PostFragment extends Fragment {

    private static final String TAB_NUMBER = "tab_number";
    private final String TAG = getClass().getCanonicalName();

    public static PostFragment newInstance(int tabNumber) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        ButterKnife.bind(this, rootView);
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
        Log.d(TAG, "ON resume");
    }
}