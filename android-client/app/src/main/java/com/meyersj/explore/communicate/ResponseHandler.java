package com.meyersj.explore.communicate;

import android.os.Handler;
import android.os.Message;

import com.meyersj.explore.background.ScannerService;
import com.meyersj.explore.chat.ChatFragment;
import com.meyersj.explore.search.SearchFragment;

import java.lang.ref.WeakReference;


public class ResponseHandler extends Handler {

    private final String TAG = getClass().getCanonicalName();
    private WeakReference<SearchFragment> search;
    private WeakReference<ChatFragment> chat;
    private WeakReference<ScannerService> service;

    public ResponseHandler(ScannerService service) {
        this.service = new WeakReference<>(service);
    }

    public ResponseHandler(SearchFragment search, ChatFragment chat) {
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
            case 3:
                // service handler
                ScannerService service = this.service.get();
                if (service != null) {
                    service.update(message);
                }
                break;
        }
    }
}
