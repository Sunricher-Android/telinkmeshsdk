package com.sunricher.telinkblemeshlib.callback;


import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;

import java.util.ArrayList;
import java.util.Date;

public abstract class DeviceCallback {

    public abstract void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices);

    public abstract void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData);

    public void didGetDate(MeshManager manager, int address, Date date) {
    }

    public void didGetLightOnOffDuration(MeshManager manager, int address, int duration) {
    }

}