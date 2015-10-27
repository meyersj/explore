package com.meyersj.tracker.explore;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;


public class ExploreHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private final WeakReference<ExploreFragment> fragment;

    public ExploreHandler(ExploreFragment fragment) {
        this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message message){
        ExploreFragment fragment = this.fragment.get();
        if (fragment != null){
            fragment.update(message);
        }
    }

}
