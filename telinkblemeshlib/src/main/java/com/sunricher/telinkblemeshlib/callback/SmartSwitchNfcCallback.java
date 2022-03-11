package com.sunricher.telinkblemeshlib.callback;

import com.sunricher.telinkblemeshlib.SmartSwitchManager;

public abstract class SmartSwitchNfcCallback {

    public void smartSwitchManagerDidConfigureSuccessful(SmartSwitchManager manager) {
    }

    public void smartSwitchManagerDidReadConfiguration(SmartSwitchManager manager, boolean isConfigured, int mode) {
    }

    public void smartSwitchManagerDidUnbindConfigurationSuccessful(SmartSwitchManager manager) {
    }

    public void smartSwitchManagerNfcReadWriteFailed(SmartSwitchManager manager, int state) {
    }

}
