package com.meyersj.explore.communicate;

import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.meyersj.explore.background.ScannerService;
import com.meyersj.explore.explore.ExploreFragment;
import com.meyersj.explore.explore2.ChatFragment;
import com.meyersj.explore.explore2.SearchFragment;
import com.meyersj.explore.map.LocationMapFragment;

import java.lang.ref.WeakReference;


public class ResponseHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private WeakReference<SearchFragment> search;
    private WeakReference<ChatFragment> chat;
    private WeakReference<Fragment> fragment;
    private WeakReference<Service> service;

    public ResponseHandler(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
        this.service = null;
    }

    public ResponseHandler(Service service) {
        this.service = new WeakReference<>(service);
        this.fragment = null;
    }

    public ResponseHandler(SearchFragment search, ChatFragment chat) {
        //this.service = new WeakReference<>(service);
        //this.fragment = null;
        this.search = new WeakReference<>(search);
        this.chat = new WeakReference<>(chat);
    }

    @Override
    public void handleMessage(Message message){
        int handler = message.getData().getInt(ProtocolMessage.HANDLER);
        switch(handler) {
            case 0:
                break;
            case 1:
                // search handler
                SearchFragment sfrag = this.search.get();
                if (sfrag != null) {
                    sfrag.update(message);
                }
                break;
            case 2:
                // chat handler
                ChatFragment cfrag = this.chat.get();
                if (cfrag != null) {
                    cfrag.update(message);
                }
                break;
        }

        /*

        if (service != null) {
            Service service = this.service.get();
            if(service != null) {
                ((ScannerService) service).update(message);
            }
            return;
        }
        Fragment fragment = this.fragment.get();
        if (fragment != null) {
            if (fragment instanceof ExploreFragment) {
                ((ExploreFragment) fragment).update(message);
            }
            else if (fragment instanceof LocationMapFragment) {
                ((LocationMapFragment)fragment).update(message);
            }
            else if (fragment instanceof SearchFragment) {
                ((SearchFragment)fragment).update(message);
            }
            //else if (fragment instanceof ChatFragment) {
            //    ((chatFragment)fragment).update(message);
            //}
            //else if (fragment instanceof PostFragment) {
            //    ((postFragment)fragment).update(message);
            //}
        }
        */
    }
}
