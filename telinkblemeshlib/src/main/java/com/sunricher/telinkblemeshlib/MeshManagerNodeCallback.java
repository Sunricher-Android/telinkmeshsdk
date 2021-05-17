package com.sunricher.telinkblemeshlib;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanCallback;

import com.clj.fastble.data.BleDevice;

public abstract class MeshManagerNodeCallback {

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

    public void didGetMac(MeshManager manager, byte[] macBytes, int address) {

    }

    public void didConfirmNewNetwork(MeshManager manager, Boolean isSuccess) {

    }

}
