package com.sunricher.telinkblemeshlib.mqttdeviceevent;

import java.util.HashMap;

public class LightOnOffDurationEvent extends AbstractMqttDeviceEvent {

    private int shortAddress;
    private int duration;

    private LightOnOffDurationEvent() {
    }

    public LightOnOffDurationEvent(int shortAddress, int duration) {
        this.shortAddress = shortAddress;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    @Override
    public EventType getEventType() {
        return EventType.lightOnOffDuration;
    }

    @Override
    public HashMap<String, Integer> getPayloadValue() {

        HashMap<String, Integer> map = new HashMap<>();
        map.put("short_address", shortAddress);
        map.put("duration", duration);

        return map;
    }
}
