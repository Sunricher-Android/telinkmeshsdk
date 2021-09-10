package com.sunricher.telinkblemeshlib.mqttdeviceevent;

import java.util.HashMap;

public class FirmwareEvent extends AbstractMqttDeviceEvent {

    private int shortAddress;
    private String firmwareVersion;

    private FirmwareEvent() {
    }

    public FirmwareEvent(int shortAddress, String firmwareVersion) {
        this.shortAddress = shortAddress;
        this.firmwareVersion = firmwareVersion;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    public EventType getEventType() {
        return EventType.firmwareVersion;
    }

    @Override
    public HashMap<String, Object> getPayloadValue() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("short_address", shortAddress);
        map.put("firmware_version", firmwareVersion);

        return map;
    }
}
