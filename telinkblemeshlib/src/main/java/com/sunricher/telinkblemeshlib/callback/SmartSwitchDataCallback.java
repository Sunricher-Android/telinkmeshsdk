package com.sunricher.telinkblemeshlib.callback;

import com.sunricher.telinkblemeshlib.SmartSwitchManager;

public abstract class SmartSwitchDataCallback {

    public void smartSwitchManagerDidReceiveData(SmartSwitchManager manager, int progress) {
    }

    public void smartSwitchManagerDidReceiveDataEnd(SmartSwitchManager manager) {
    }

    public void smartSwitchManagerDidReceiveDataFailed(SmartSwitchManager manager) {
    }

}
