package com.meyersj.tracker.explore;

import com.meyersj.tracker.Utils;

public class ExploreTask {

    // Gets a handle to the object that creates the thread pools
    private ExploreScanner scanner;
    private BeaconBroadcast broadcast;
    public Integer state = 1;
    public String code = "not finished";

    public ExploreTask(ExploreScanner scanner) {
        this.scanner = scanner;
    }

    public void updateBroadcast(BeaconBroadcast broadcast) {
        this.broadcast = broadcast;
        if (broadcast != null) {
            this.code = Utils.getHexString(broadcast.getPayload());
            handleBroadcast();
        }
    }


    public void handleBroadcast() {
        scanner.handleBroadcast(this);
    }

}
