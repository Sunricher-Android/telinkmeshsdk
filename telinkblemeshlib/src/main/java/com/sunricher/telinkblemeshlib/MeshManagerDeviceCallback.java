package com.sunricher.telinkblemeshlib;

import java.util.ArrayList;

public abstract class MeshManagerDeviceCallback {

    public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {

    }

    public void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData) {

    }

}

