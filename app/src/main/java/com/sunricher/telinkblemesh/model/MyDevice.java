package com.sunricher.telinkblemesh.model;

import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;

import java.util.Arrays;

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
        return macData != null || deviceType != null;
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {

        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;

        MyDevice another = (MyDevice) obj;

        return this.meshDevice.getAddress() == another.meshDevice.getAddress();
    }

    public MeshDevice getMeshDevice() {
        return meshDevice;
    }

    public void setMeshDevice(MeshDevice meshDevice) {
        this.meshDevice = meshDevice;
    }

    public byte[] getMacData() {
        return macData;
    }

    public void setMacData(byte[] macData) {

        if (macData != null) {
            this.macData = Arrays.copyOf(macData, macData.length);
        } else {
            this.macData = null;
        }
    }

    public MeshDeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(MeshDeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
