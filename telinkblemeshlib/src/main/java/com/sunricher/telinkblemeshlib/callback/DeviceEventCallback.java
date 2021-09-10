package com.sunricher.telinkblemeshlib.callback;

import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.mqttdeviceevent.AbstractMqttDeviceEvent;

public abstract class DeviceEventCallback {

    public abstract void didUpdateEvent(MeshManager manager, AbstractMqttDeviceEvent event);

}
