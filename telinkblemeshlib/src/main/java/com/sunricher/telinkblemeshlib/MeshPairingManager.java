package com.sunricher.telinkblemeshlib;

import android.content.Context;
import android.util.Log;

import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class MeshPairingManager {

    private static final String LOG_TAG = "MeshPairingManager";

    private Callback callback;
    private MeshNetwork network;
    private Status status = Status.stopped;
    // TODO: Timer
    // milliseconds
    private double connectingInterval = 8000;
    private double scanningInterval = 2000;
    private double setNetworkInterval = 4000;
    private double waitingChangingAddressesInterval = 8000;

    // Key is macData string
    private Map<String, PendingDeviceData> pendingDeviceDataMap = new HashMap<>();
    private List<Integer> availableAddressList = new ArrayList<>();
    private MeshNode connectNode;

    private MeshPairingManager() {

    }

    public MeshPairingManager getInstance() {
        return SingletonHolder.instance;
    }

    public void startPairing(MeshNetwork network, Context context, Callback callback) {

        this.network = network;
        this.callback = callback;

        this.pendingDeviceDataMap.clear();
        this.availableAddressList = MeshAddressManager.getInstance().getAvailableAddressList(network, context);
        Log.i(LOG_TAG, "availableAddressList count " + availableAddressList.size());

        if (this.availableAddressList.size() < 1) {

            this.callback.pairingFailed(this, FailedReason.noMoreNewAddresses);
            return;
        }

        this.scanExistDevices();
    }

    public void stop() {

        status = Status.stopped;
        // TODO: timer invalidate
        pendingDeviceDataMap.clear();
        availableAddressList.clear();
        connectNode = null;
        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect(false);
    }

    private void scanExistDevices() {

        Log.i(LOG_TAG, "scanExistDevices");

        // TODO: timer.invalidate
        status = Status.existDeviceScanning;
        connectNode = null;
        MeshManager.getInstance().scanNode(network);

        // TODO: new timer
    }

    private void connectFactoryNetwork() {

        Log.i(LOG_TAG, "connectFactoryNetwork");

        // TODO: timer.invalidate
        status = Status.factoryConnecting;
        connectNode = null;
        MeshManager.getInstance().scanNode(MeshNetwork.factory);

        // TODO: new timer
    }

    private void scanAllMac() {

        Log.i(LOG_TAG, "scanAllMac");

        // TODO: timer.invalidate
        status = Status.allMacScanning;
        MeshCommand cmd = MeshCommand.requestAddressMac(MeshCommand.Address.all);
        MeshManager.getInstance().send(cmd);

        // TODO: new timer
    }

    private void changePendingDevices() {

        Log.i(LOG_TAG, "changePendingDevices");

        if (pendingDeviceDataMap.size() < 1) {

            Log.i(LOG_TAG, "pendingDevices.count < 1, next .setNewNetwork");
            setNewNetwork();
            return;
        }

        // TODO: timer.invalidate
        status = Status.addressChanging;

        // TODO: Change pending devices address

        // TODO: new timer
    }

    private void setNewNetwork() {

        Log.i(LOG_TAG, "setNewNetwork");

        // TODO: timer.invalidate
        status = Status.networkSetting;
        // TODO: MeshManager.getInstance().setNewNetwork(network)

        // TODO: new timer
    }

    private void scanNetworkDevices() {

        Log.i(LOG_TAG, "scanNetworkDevices");

        // TODO: timer.invalidate
        status = Status.newDevicesScanning;
        MeshManager.getInstance().scanMeshDevices();

        // TODO: new timer
    }

    private void timerAction() {
        // TODO: timer action switch
    }

    private int getNextAvailableAddress(int oldAddress) {

        Log.i(LOG_TAG, "getNextAvailableAddress");

        List<Integer> addressList = availableAddressList;

        for (int address : addressList) {

            if (address != oldAddress) {

                availableAddressList.remove(Integer.valueOf(address));
                return address;
            }
        }

        return 0;
    }

    public enum FailedReason {

        noMoreNewAddresses, noNewDevices
    }

    public enum Status {
        stopped,
        existDeviceScanning,
        factoryConnecting,
        allMacScanning,
        addressChanging,
        networkSetting,
        networkConnecting,
        newDevicesScanning
    }

    private static class SingletonHolder {
        private static final MeshPairingManager instance = new MeshPairingManager();
    }

    public abstract class Callback {

        public abstract void pairingFailed(MeshPairingManager manager, FailedReason reason);

        public abstract void didUpdateNewDevice(MeshPairingManager manager, MeshDevice meshDevice);

        public abstract void didUpdateProgress(MeshPairingManager manager, float progress);

        public abstract void didFinishPairing(MeshAddressManager manager);

    }

    class PendingDeviceData {
        byte[] macData;
        int oldAddress;
        int newAddress;
    }

}
