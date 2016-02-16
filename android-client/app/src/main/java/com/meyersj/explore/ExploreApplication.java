package com.meyersj.explore;

import android.app.Application;

import com.meyersj.explore.utilities.Utils;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.logging.AgentLog;


public class ExploreApplication extends Application {

    public ExploreApplication() {
        NewRelic.withApplicationToken(Utils.getNewRelicToken(getApplicationContext()))
                .withLogLevel(AgentLog.DEBUG)
                .start(getApplicationContext());
    }
}
