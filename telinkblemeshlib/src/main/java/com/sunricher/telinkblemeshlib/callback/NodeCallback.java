package com.sunricher.telinkblemeshlib.callback;


import android.bluetooth.BluetoothGatt;

import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNode;

public abstract class NodeCallback {

    public void didDiscoverNode(MeshManager manager, MeshNode node) {
    }

    public void didConnectNode(MeshManager manager, MeshNode node) {
    }

    public void didDisconnectNode(MeshManager manager, Boolean isActiveDisConnected, MeshNode node, BluetoothGatt gatt) {
    }

    public void didFailToConnectNode(MeshManager manager, MeshNode node) {
    }

    public void didLoginNode(MeshManager manager, MeshNode node) {
    }

    public void didFailToLoginNode(MeshManager manager) {
    }

    public void didGetDeviceAddress(MeshManager manager, int address) {
    }

    public void didConfirmNewNetwork(MeshManager manager, Boolean isSuccess) {
    }

    public void didGetFirmware(MeshManager manager, String firmware) {
    }

}