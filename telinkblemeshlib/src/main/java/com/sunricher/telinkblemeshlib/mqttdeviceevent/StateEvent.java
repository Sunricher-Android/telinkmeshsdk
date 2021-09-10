package com.sunricher.telinkblemeshlib.mqttdeviceevent;

import com.sunricher.telinkblemeshlib.MeshDevice;

import java.util.ArrayList;
import java.util.HashMap;

public class StateEvent extends AbstractMqttDeviceEvent {

    private ArrayList<MeshDevice> meshDevices = new ArrayList<>();

    private StateEvent() {

    }

    public StateEvent(ArrayList<MeshDevice> devices) {
        this.meshDevices = devices;
    }

    public ArrayList<MeshDevice> getMeshDevices() {
        return meshDevices;
    }

    public void setMeshDevices(ArrayList<MeshDevice> meshDevices) {
        this.meshDevices = meshDevices;
    }

    @Override
    public EventType getEventType() {
        return EventType.state;
    }

    @Override
    public ArrayList<HashMap<String, Object>> getPayloadValue() {

        ArrayList<HashMap<String, Object>> valueList = new ArrayList<>();
        for (MeshDevice device : meshDevices) {
            valueList.add(device.getItemValue());
        }
        return valueList;
    }
}
