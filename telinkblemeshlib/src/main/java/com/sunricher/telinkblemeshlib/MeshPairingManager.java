package com.sunricher.telinkblemeshlib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MeshPairingManager {

    private static final String LOG_TAG = "MeshPairingManager";

    private Callback callback;
    private MeshNetwork network;
    private Status status = Status.stopped;

    // milliseconds (ms)
    private long connectingInterval = 8000;
    private long scanningInterval = 2000;
    private long setNetworkInterval = 4000;
    private long waitingChangingAddressesInterval = 8000;

    // Key is macData string
//    private Map<String, PendingData> pendingDataMap = new HashMap<>();
    private List<PendingData> pendingDataList = new ArrayList<>();
    private List<Integer> availableAddressList = new ArrayList<>();
    private MeshNode connectNode;

    private Timer timer;

    private MeshPairingManager() {

    }

    public MeshPairingManager getInstance() {
        return SingletonHolder.instance;
    }

    public void startPairing(MeshNetwork network, Context context, Callback callback) {

        this.network = network;
        this.callback = callback;

        this.pendingDataList.clear();
        this.availableAddressList = MeshAddressManager.getInstance().getAvailableAddressList(network, context);
        Log.i(LOG_TAG, "availableAddressList count " + availableAddressList.size());

        if (this.availableAddressList.size() < 1) {

            if (callback == null) return;
            this.callback.pairingFailed(this, FailedReason.noMoreNewAddresses);
            return;
        }

        this.scanExistDevices();
    }

    public void stop() {

        status = Status.stopped;
        cancelTimer();
        pendingDataList.clear();
        availableAddressList.clear();
        connectNode = null;
        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect(false);
    }

    private void scanExistDevices() {

        Log.i(LOG_TAG, "scanExistDevices");

        cancelTimer();
        status = Status.existDeviceScanning;
        connectNode = null;
        MeshManager.getInstance().scanNode(network);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "scanExistDevices overtime, next.");
                connectFactoryNetwork();
            }
        }, connectingInterval);
    }

    private void connectFactoryNetwork() {

        Log.i(LOG_TAG, "connectFactoryNetwork");

        cancelTimer();
        status = Status.factoryConnecting;
        connectNode = null;
        MeshManager.getInstance().scanNode(MeshNetwork.factory);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                status = Status.stopped;
                Log.i(LOG_TAG, "factoryConnecting failed, cancel.");
                connectNode = null;
                MeshManager.getInstance().stopScanNode();
                MeshManager.getInstance().disconnect(false);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback == null) return;
                        callback.pairingFailed(MeshPairingManager.this, FailedReason.noNewDevices);
                    }
                });
            }
        }, connectingInterval);
    }

    private void scanAllMac() {

        Log.i(LOG_TAG, "scanAllMac");

        cancelTimer();
        status = Status.allMacScanning;
        MeshCommand cmd = MeshCommand.requestAddressMac(MeshCommand.Address.all);
        MeshManager.getInstance().send(cmd);

        // TODO: new timer
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "allMacScanning no more devices, next. Change pending devices " + pendingDataList.size());
                changePendingDevices();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback == null) return;
                        callback.didUpdateProgress(MeshPairingManager.this, 0.42);
                    }
                });
            }
        }, scanningInterval);
    }

    private void changePendingDevices() {

        Log.i(LOG_TAG, "changePendingDevices");

        if (pendingDataList.size() < 1) {

            Log.i(LOG_TAG, "pendingDevices.count < 1, next .setNewNetwork");
            setNewNetwork();
            return;
        }

        cancelTimer();
        status = Status.addressChanging;

        double consumeInterval = pendingDataList.size() * 0.3;

        for (PendingData data : pendingDataList) {

            byte[] macData =  data.macData;
            int oldAddress = data.oldAddress;
            int newAddress = data.newAddress;

            MeshCommand cmd = MeshCommand.changeAddress(oldAddress, newAddress, macData);
            MeshManager.getInstance().send(cmd);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                setNewNetwork();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback == null) return;
                        callback.didUpdateProgress(MeshPairingManager.this, 0.56);
                    }
                });
            }
        }, waitingChangingAddressesInterval + connectingInterval);
    }

    private void setNewNetwork() {

        Log.i(LOG_TAG, "setNewNetwork");

        cancelTimer();
        status = Status.networkSetting;
        // TODO: MeshManager.getInstance().setNewNetwork(network)

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                status = Status.networkConnecting;
                Log.i(LOG_TAG, "networkSetting OK, next, networkConnecting");
                connectNode = null;
                MeshManager.getInstance().scanNode(network);

                cancelTimer();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        status = Status.stopped;
                        Log.i(LOG_TAG, "networkConnecting failed, cancel.");
                        connectNode = null;
                        MeshManager.getInstance().stopScanNode();
                        MeshManager.getInstance().disconnect(false);

                        if (callback == null) return;
                        callback.pairingFailed(MeshPairingManager.this, FailedReason.noNewDevices);
                    }
                }, connectingInterval);

                if (callback == null) return;
                callback.didUpdateProgress(MeshPairingManager.this, 0.70);
            }
        }, setNetworkInterval);
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

    private void startTimer() {


    }

    private void cancelTimer() {

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
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

        public abstract void didUpdateProgress(MeshPairingManager manager, double progress);

        public abstract void didFinishPairing(MeshAddressManager manager);

    }

    private class PendingData {
        byte[] macData;
        int oldAddress;
        int newAddress;
    }

}
