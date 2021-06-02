package com.sunricher.telinkblemeshlib.callback;


import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;

import java.util.ArrayList;
import java.util.Date;

public abstract class DeviceCallback {

    public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {
    }

    public void didUpdateDeviceType(MeshManager manager, int address, MeshDeviceType deviceType, byte[] macData) {
    }

    public void didGetDate(MeshManager manager, int address, Date date) {
    }

    public void didGetLightOnOffDuration(MeshManager manager, int address, int duration) {
    }

    public void didGetFirmwareVersion(MeshManager manager, int address, String version) {
    }

}