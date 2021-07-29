package com.sunricher.telinkblemeshlib;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
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

import androidx.core.provider.SelfDestructiveThread;

public class MeshPairingManager {

    private static final String LOG_TAG = "MeshPairingManager";

    private Callback callback;
    private MeshNetwork network;
    private Status status = Status.stopped;

    // milliseconds (ms)
    private long connectingInterval = 16000;
    private long scanningInterval = 2000;
    private long setNetworkInterval = 6000;
    private long waitingChangingAddressesInterval = 8000;

    private MeshAddressManager addressManager;

    private List<PendingData> pendingDataList = new ArrayList<>();
    private List<Integer> availableAddressList = new ArrayList<>();

    private double progress = 0;
    private Timer timer;
    private NodeCallback nodeCallback;
    private DeviceCallback deviceCallback;

    private MeshPairingManager() {

        this.nodeCallback = this.makeNodeCallback();
        this.deviceCallback = this.makeDeviceCallback();
    }

    public static MeshPairingManager getInstance() {
        return SingletonHolder.instance;
    }

    public void startPairing(MeshNetwork network, Application application, Callback callback) {

        progress = 0;

        this.addressManager = MeshAddressManager.getInstance(application);
        this.network = network;
        this.callback = callback;

        MeshManager.getInstance().setNodeCallback(nodeCallback);
        MeshManager.getInstance().setDeviceCallback(deviceCallback);

        this.pendingDataList.clear();
        this.availableAddressList = addressManager.getAvailableAddressList(network);
        Log.i(LOG_TAG, "availableAddressList count " + availableAddressList.size());

        if (this.availableAddressList.size() < 1) {

            if (callback == null) return;
            this.callback.pairingFailed(this, FailedReason.noMoreNewAddresses);
            return;
        }

//        this.scanExistDevices();
        connectFactoryNetwork();
    }

    public void stop() {

        status = Status.stopped;
        cancelTimer();
        pendingDataList.clear();
        availableAddressList.clear();
        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect(false);
    }

    private void scanExistDevices() {

        Log.i(LOG_TAG, "scanExistDevices");

        cancelTimer();
        status = Status.existDeviceScanning;
        MeshManager.getInstance().scanNode(network, true, false);

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
        MeshManager.getInstance().scanNode(MeshNetwork.factory, true, false);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                status = Status.stopped;
                Log.i(LOG_TAG, "factoryConnecting failed, cancel.");
                MeshManager.getInstance().stopScanNode();
                MeshManager.getInstance().disconnect(false);

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.pairingFailed(MeshPairingManager.this, FailedReason.noNewDevices);
                    }
                });
            }
        }, connectingInterval);
    }

    private void scanAllMac() {


        Log.i(LOG_TAG, "scanAllMac");
        pendingDataList.clear();

        cancelTimer();
        status = Status.allMacScanning;
        MeshCommand cmd = MeshCommand.requestMacDeviceType(MeshCommand.Address.all);
        MeshManager.getInstance().send(cmd);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "allMacScanning no more devices, next. Change pending devices " + pendingDataList.size());
                changePendingDevices();

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        progress = Math.max(0.42, progress);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
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

        double itemInterval = 1000;
        double consumeInterval = pendingDataList.size() * itemInterval;

        for (PendingData data : pendingDataList) {

            byte[] macData = data.macData;
            int oldAddress = data.oldAddress;
            int newAddress = data.newAddress;

            MeshCommand cmd = MeshCommand.changeAddress(oldAddress, newAddress, macData);
            MeshManager.getInstance().send(cmd, (long) itemInterval);
            addressManager.append(newAddress, network);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                setNewNetwork();
                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        progress = Math.max(progress, 0.56);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                    }
                });
            }
        }, waitingChangingAddressesInterval + (long) Math.ceil(consumeInterval));
    }

    private void setNewNetwork() {

        Log.i(LOG_TAG, "setNewNetwork");

        cancelTimer();
        status = Status.networkSetting;
        MeshManager.getInstance().setNewNetwork(network, true);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                status = Status.networkConnecting;
                Log.i(LOG_TAG, "networkSetting OK, next, networkConnecting");
                MeshManager.getInstance().scanNode(network, true, false);

                cancelTimer();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        status = Status.stopped;
                        Log.i(LOG_TAG, "networkConnecting failed, cancel.");
                        MeshManager.getInstance().stopScanNode();
                        MeshManager.getInstance().disconnect(false);

                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.pairingFailed(MeshPairingManager.this, FailedReason.noNewDevices);
                            }
                        });

                    }
                }, connectingInterval);

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        progress = Math.max(progress, 0.70);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                    }
                });

            }
        }, setNetworkInterval);
    }

    private void scanNetworkDevices() {

        Log.i(LOG_TAG, "scanNetworkDevices");

        cancelTimer();
        status = Status.newDevicesScanning;
        MeshManager.getInstance().scanMeshDevices();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                status = Status.stopped;

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        progress = Math.max(progress, 1.0);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                        callback.didFinishPairing(MeshPairingManager.this);
                    }
                });
            }
        }, scanningInterval);
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

                        progress = Math.max(progress, 0.16);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                    }
                });
            }

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

                switch (status) {

                    case existDeviceScanning:
                        Log.i(LOG_TAG, "existDeviceScanning login, scanAllDevices");
                        cancelTimer();
                        MeshManager.getInstance().scanMeshDevices();

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                Log.i(LOG_TAG, "existDeviceScanning overtime, next.");
                                connectFactoryNetwork();
                            }
                        }, scanningInterval);

                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.pairingFailed(MeshPairingManager.this, FailedReason.noNewDevices);
                            }
                        });
                        break;

                    case factoryConnecting:

                        Log.i(LOG_TAG, "factoryConnecting login, scanAllMac");
                        scanAllMac();

                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                progress = Math.max(progress, 0.28);
                                callback.didUpdateProgress(MeshPairingManager.this, progress);
                            }
                        });
                        break;

                    case networkConnecting:

                        Log.i(LOG_TAG, "networkConnecting OK, scanNetworkDevices");
                        scanNetworkDevices();

                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                progress = Math.max(progress, 0.84);
                                callback.didUpdateProgress(MeshPairingManager.this, progress);
                            }
                        });
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void didGetMac(MeshManager manager, byte[] macBytes, int address) {

//                handleDidGetMac(address, macBytes);
            }

        };
    }

    private DeviceCallback makeDeviceCallback() {

        return new DeviceCallback() {
            @Override
            public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {

                Log.i(LOG_TAG, "didUpdateMeshDevices pairing " + status);

                switch (status) {

                    case existDeviceScanning:

                        cancelTimer();
                        for (MeshDevice meshDevice : meshDevices) {
                            addressManager.append(meshDevice.getAddress(), network);
                            availableAddressList.add(meshDevice.getAddress());
                        }

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                Log.i(LOG_TAG, "existDeviceScanning overtime, next.");
                                connectFactoryNetwork();
                            }
                        }, scanningInterval);
                        break;

                    case newDevicesScanning:

                        cancelTimer();
                        for (MeshDevice meshDevice : meshDevices) {
                            addressManager.append(meshDevice.getAddress(), network);
                            availableAddressList.add(meshDevice.getAddress());
                        }

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                status = Status.stopped;

                                if (callback == null) return;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        progress = Math.max(progress, 1.0);
                                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                                        callback.didFinishPairing(MeshPairingManager.this);
                                    }
                                });
                            }
                        }, scanningInterval);

                        if (callback == null) return;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.didAddNewDevices(MeshPairingManager.this, meshDevices);
                            }
                        });

                        break;

                    default:
                        break;
                }
            }

            @Override
            public void didUpdateDeviceType(MeshManager manager, int address, MeshDeviceType deviceType, byte[] macData) {

                if (!deviceType.isSupportMeshAdd()) {

                    stop();
                    if (callback != null) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.terminalWithUnsupportedDevice(MeshPairingManager.this, address, deviceType, macData);
                            }
                        });
                    }
                    return;
                }

                handleDidGetMac(address, macData);
            }
        };
    }

    private void handleDidGetMac(int address, byte[] macBytes) {

        if (status != Status.allMacScanning) {

            Log.i(LOG_TAG, "Only for allMacScanning");
            return;
        }

        int newAddress = getNextAvailableAddress(address);
        if (newAddress < 1) {
            if (pendingDataList.size() == 0) {

                status = Status.stopped;
                cancelTimer();
                Log.i(LOG_TAG, "getNextAvailableAddress failed & pendingDevices.count == 0, stopped.");
                MeshManager.getInstance().stopScanNode();
                MeshManager.getInstance().disconnect(false);

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        callback.pairingFailed(MeshPairingManager.this, FailedReason.noMoreNewAddresses);
                    }
                });
            }
            return;
        }
        Log.i(LOG_TAG, "getNextAvailableAddress success new " + newAddress + ", old " + address);

        cancelTimer();
        PendingData data = new PendingData();
        data.macData = macBytes;
        data.oldAddress = address;
        data.newAddress = newAddress;
        pendingDataList.add(data);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "allMacScanning no more devices, next. Change pending devices " + pendingDataList.size());
                changePendingDevices();

                if (callback == null) return;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        progress = Math.max(progress, 0.42);
                        callback.didUpdateProgress(MeshPairingManager.this, progress);
                    }
                });
            }
        }, scanningInterval);
    }

    public enum FailedReason {

        noMoreNewAddresses, noNewDevices
    }

    enum Status {
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

    public abstract static class Callback {

        public void pairingFailed(MeshPairingManager manager, FailedReason reason) {}

        public void didAddNewDevices(MeshPairingManager manager, ArrayList<MeshDevice> meshDevices) {}

        public void didUpdateProgress(MeshPairingManager manager, double progress) {}

        public void didFinishPairing(MeshPairingManager manager) {}

        public void terminalWithUnsupportedDevice(MeshPairingManager manager, int address, MeshDeviceType deviceType, byte[] bytes) {}

    }

    private static class PendingData {
        byte[] macData;
        int oldAddress;
        int newAddress;
    }

}
