package com.sunricher.telinkblemeshlib.mqttdeviceevent;

import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.util.HexUtil;

import java.util.HashMap;

public class DeviceTypeEvent extends AbstractMqttDeviceEvent {

    private int shortAddress;
    private MeshDeviceType deviceType;
    private byte[] macData = new byte[]{};

    private DeviceTypeEvent() {
    }

    public DeviceTypeEvent(int shortAddress, MeshDeviceType deviceType, byte[] macData) {
        this.shortAddress = shortAddress;
        this.deviceType = deviceType;
        this.macData = macData;
    }

    public byte[] getMacData() {
        return macData;
    }

    public void setMacData(byte[] macData) {
        this.macData = macData;
    }

    public MeshDeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(MeshDeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    @Override
    public EventType getEventType() {
        return EventType.deviceType;
    }

    @Override
    public HashMap<String, Object> getPayloadValue() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("short_address", shortAddress);
        map.put("main_type", deviceType.getRawValue1());
        map.put("sub_type", deviceType.getRawValue2());
        map.put("mac_data", HexUtil.getStringByBytes(macData));

        return map;
    }
}
