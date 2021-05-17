package com.sunricher.telinkblemesh.model;

import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class MyDevice {

    private MeshDevice meshDevice;
    private byte[] macData;
    private MeshDeviceType deviceType;

    public MyDevice(MeshDevice meshDevice) {
        this.meshDevice = meshDevice;
    }

    public String getTitle() {
        return meshDevice.getDescription();
    }

    public String getDetail() {
        if (!isValid()) {
            return "Invalid";
        }

        StringBuilder mac = new StringBuilder();
        for (byte macDatum : macData) {
            int value = (int) macDatum & 0xFF;
            mac.append(String.format("%02X", value));
        }

        return mac + ", " + deviceType.getCategoryTitle();
    }

    public Boolean isValid() {
        return macData != null && deviceType != null;
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {

        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;

        MyDevice another = (MyDevice) obj;

        return this.meshDevice.getAddress() == another.meshDevice.getAddress();
    }

    public void updateWithDevice(MyDevice device) {
        this.meshDevice = device.meshDevice;
        this.macData = device.macData;
        this.deviceType = device.deviceType;
    }

    public MeshDevice getMeshDevice() {
        return meshDevice;
    }

    public byte[] getMacData() {
        return macData;
    }

    public MeshDeviceType getDeviceType() {
        return deviceType;
    }

    public void setMacData(byte[] macData) {
        this.macData = macData;
    }

    public void setDeviceType(MeshDeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
