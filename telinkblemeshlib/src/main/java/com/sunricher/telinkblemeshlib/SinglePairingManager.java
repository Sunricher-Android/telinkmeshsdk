package com.sunricher.telinkblemeshlib;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sunricher.telinkblemeshlib.callback.DeviceCallback;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;
import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SinglePairingManager {

    private static final String LOG_TAG = "SinglePairingManager";
    private final long connectingInterval = 16000;
    private final long setNetworkInterval = 6000;
    private final long waitingChangingAddressesInterval = 8000;
    private final long deviceTypeGettingInterval = 4000;
    private Callback callback;
    private MeshNetwork network = MeshNetwork.factory;
    private Status status = Status.stopped;
    private int oldAddress = 0;
    private int newAddress = 0;

    private List<Integer> availableAddressList = new ArrayList<>();

    private Timer timer;
    private NodeCallback nodeCallback;
    private DeviceCallback deviceCallback;

    private SinglePairingManager() {

        this.nodeCallback = this.makeNodeCallback();
        this.deviceCallback = this.makeDeviceCallback();
    }

    public static SinglePairingManager getInstance() {
        return SingletonHolder.instance;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void startScanning() {

        Log.i(LOG_TAG, "SinglePairingManager startScanning");
        status = Status.scanning;
        cancelTimer();

        MeshManager.getInstance().setNodeCallback(nodeCallback);
        MeshManager.getInstance().setDeviceCallback(deviceCallback);
        MeshManager.getInstance().scanNode(MeshNetwork.factory);
    }

    public void stop() {

        Log.i(LOG_TAG, "SinglePairingManager stop");
        status = Status.stopped;
        cancelTimer();

        oldAddress = 0;
        newAddress = 0;
        availableAddressList.clear();

        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect();
    }

    public void startPairing(MeshNetwork network, Application application, MeshNode node) {

        Log.i(LOG_TAG, "SinglePairingManager startPairing");
        status = Status.startPairing;
        cancelTimer();

        this.network = network;

        MeshManager.getInstance().stopScanNode();

        oldAddress = 0;
        newAddress = 0;
        MeshAddressManager addressManager = MeshAddressManager.getInstance(application);
        availableAddressList = addressManager.getAvailableAddressList(network);

        Log.i(LOG_TAG, "availableAddressList count " + availableAddressList.size());

        if (this.availableAddressList.size() < 1) {

            stop();
            if (this.callback == null) return;
            this.callback.terminalWithNodeNoMoreNewAddresses(this);
            return;
        }

        if (!node.getDeviceType().isSupportSingleAdd()) {

            if (this.callback == null) return;
            this.callback.terminalWithUnsupportedNode(this, node);
            return;
        }

        status = Status.connecting;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                stop();
                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.didFailToLoginNode(SinglePairingManager.this);
                    }
                });
            }
        }, connectingInterval);

        MeshManager.getInstance().connect(node);
    }

    private void cancelTimer() {

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    private NodeCallback makeNodeCallback() {

        return new NodeCallback() {

            @Override
            public void didDiscoverNode(MeshManager manager, MeshNode node) {

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.didDiscoverNode(SinglePairingManager.this, node);
                    }
                });
            }

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

                oldAddress = node.getShortAddress();
                newAddress = 0;
                cancelTimer();
                status = Status.deviceTypeGetting;

                MeshCommand command = MeshCommand.requestMacDeviceType(MeshCommand.Address.connectNode);
                MeshManager.getInstance().send(command);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        stop();
                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.didFailToLoginNode(SinglePairingManager.this);
                            }
                        });
                    }
                }, deviceTypeGettingInterval);
            }

            @Override
            public void didFailToLoginNode(MeshManager manager) {

                stop();
                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.didFailToLoginNode(SinglePairingManager.this);
                    }
                });
            }
        };
    }

    private DeviceCallback makeDeviceCallback() {

        return new DeviceCallback() {
            @Override
            public void didUpdateDeviceType(MeshManager manager, int address, MeshDeviceType deviceType, byte[] macData) {

                if (address != oldAddress) {
                    return;
                }

                cancelTimer();
                newAddress = getNextAvailableAddress(address);
                if (newAddress == 0) {

                    stop();
                    if (callback == null) return;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            callback.terminalWithNodeNoMoreNewAddresses(SinglePairingManager.this);
                        }
                    });
                    return;
                }

                status = Status.addressChanging;
                MeshCommand command = MeshCommand.changeAddress(MeshCommand.Address.connectNode, newAddress, macData);
                MeshManager.getInstance().send(command);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        handleNetworkSetting();
                    }
                }, waitingChangingAddressesInterval);
            }
        };
    }

    private void handleNetworkSetting() {

        cancelTimer();
        status = Status.networkSetting;
        MeshManager.getInstance().setNewNetwork(network, false);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                stop();
                Log.i(LOG_TAG, "singlePairingManagerDidFinishPairing");
                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.didFinishPairing(SinglePairingManager.this);
                    }
                });
            }
        }, setNetworkInterval);
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

    enum Status {
        stopped,
        scanning,
        startPairing,
        connecting,
        deviceTypeGetting,
        addressChanging,
        networkSetting
    }

    private static class SingletonHolder {
        private static final SinglePairingManager instance = new SinglePairingManager();
    }

    public abstract static class Callback {

        public void didDiscoverNode(SinglePairingManager manager, MeshNode node) {
        }

        public void terminalWithUnsupportedNode(SinglePairingManager manager, MeshNode node) {
        }

        public void terminalWithNodeNoMoreNewAddresses(SinglePairingManager manager) {
        }

        public void didFailToLoginNode(SinglePairingManager manager) {
        }

        public void didFinishPairing(SinglePairingManager manager) {
        }
    }
}
