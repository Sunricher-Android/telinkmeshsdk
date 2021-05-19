package com.sunricher.telinkblemeshlib.callback;


import android.bluetooth.BluetoothGatt;

import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNode;

public abstract class NodeCallback {

    public abstract void didDiscoverNode(MeshManager manager, MeshNode node);

    public abstract void didConnectNode(MeshManager manager, MeshNode node);

    public abstract void didDisconnectNode(MeshManager manager, Boolean isActiveDisConnected, MeshNode node, BluetoothGatt gatt);

    public abstract void didFailToConnectNode(MeshManager manager, MeshNode node);

    public abstract void didLoginNode(MeshManager manager, MeshNode node);

    public abstract void didFailToLoginNode(MeshManager manager);

    public abstract void didGetMac(MeshManager manager, byte[] macBytes, int address);

    public abstract void didConfirmNewNetwork(MeshManager manager, Boolean isSuccess);

}