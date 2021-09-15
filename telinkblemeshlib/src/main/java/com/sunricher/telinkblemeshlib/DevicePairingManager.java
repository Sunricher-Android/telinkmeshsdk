package com.sunricher.telinkblemeshlib;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.clj.fastble.utils.HexUtil;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;
import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

public class DevicePairingManager {

    private static final String LOG_TAG = "BridgePairingManager";
    private final long scanningInterval = 8000;
    private final long connectingInterval = 16000;
    private final long devicesGettingInterval = 4000;
    private final long addressChangingInterval = 8000;
    private final long networkSettingInterval = 4000;
    private MeshNetwork network;
    private Timer timer;
    private List<Integer> availableAddressList = new ArrayList<>();
    private State state = State.stopped;
    private List<PairingModel> models = new ArrayList<>();
    private Application ctx;
    private Callback callback;
    private MeshNode mainNode;

    private DevicePairingManager() {

    }

    public static DevicePairingManager getInstance() {
        return SingletonHolder.instance;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void startPairing(MeshNetwork network, Application ctx) {

        this.ctx = ctx;

        Log.i(LOG_TAG, "start pairing " + network.getName() + ", " + network.getPassword());

        stop();

        availableAddressList = MeshAddressManager.getInstance(ctx).getAvailableAddressList(network);
        Log.i(LOG_TAG, "availableAddressList count " + availableAddressList.size());

        if (availableAddressList.size() < 1) {

            if (callback != null) {
                callback.terminalWithNoMoreNewAddresses(this);
            }
            return;
        }

        this.network = network;

        cancelTimer();
        state = State.scanning;

        MeshManager.getInstance().setNodeCallback(makeNodeCallback());
        MeshManager.getInstance().setDeviceCallback(makeDeviceCallback());
        MeshManager.getInstance().scanNode(MeshNetwork.factory);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scanningConnectingTimerAction();
            }
        }, scanningInterval);
    }

    public void stop() {

        Log.i(LOG_TAG, "stop device pairing");

        mainNode = null;
        models.clear();
        state = State.stopped;
        cancelTimer();

        MeshManager.getInstance().setNodeCallback(null);
        MeshManager.getInstance().setDeviceCallback(null);
        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect();
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

                if (state == State.checking) {

                    if (mainNode == null) return;
                    if (mainNode.getMacAddress().equalsIgnoreCase(node.getMacAddress())) {

                        cancelTimer();
                        state = State.connecting;

                        MeshManager.getInstance().connect(node);

                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                scanningConnectingTimerAction();
                            }
                        }, connectingInterval);
                    }

                    return;
                }

                if (state != State.scanning) return;
                if (!node.getDeviceType().isSafeConnection()) return;

                cancelTimer();
                state = State.connecting;

                MeshManager.getInstance().connect(node);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        scanningConnectingTimerAction();
                    }
                }, connectingInterval);
            }

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

                if (mainNode != null && node.getMacAddress().equalsIgnoreCase(mainNode.getMacAddress())) {

                    cancelTimer();
                    state = State.networkSetting;

                    Log.i(LOG_TAG, "checking setNewNetwork");

                    MeshManager.getInstance().setNewNetwork(network, false);

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            networkSettingHandler();
                        }
                    }, networkSettingInterval);

                    return;
                }

                mainNode = node;

                if (state != State.connecting) return;

                cancelTimer();
                state = State.devicesGetting;

                MeshCommand cmd = MeshCommand.requestMacDeviceType(MeshCommand.Address.all);
                MeshManager.getInstance().send(cmd);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        deviceGettingTimerAction();
                    }
                }, devicesGettingInterval);
            }

            @Override
            public void didGetFirmware(MeshManager manager, String firmware) {

                if (state != State.networkSetting) return;
                Log.i(LOG_TAG, "Device pairing did get firmware " + firmware);
            }
        };
    }

    private DeviceCallback makeDeviceCallback() {
        return new DeviceCallback() {

            @Override
            public void didUpdateDeviceType(MeshManager manager, int address, MeshDeviceType deviceType, byte[] macData) {

                if (state != State.devicesGetting || macData == null) return;
                boolean containsMacData = false;
                for (PairingModel model : models) {
                    if (HexUtil.encodeHexStr(model.macData).equals(HexUtil.encodeHexStr(macData))) {
                        containsMacData = true;
                        break;
                    }
                }
                if (containsMacData) return;

                if (!deviceType.isSupportMeshAdd()) {

                    stop();
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.terminalWithUnsupportedDevice(DevicePairingManager.this, address, deviceType, macData);
                            }
                        });
                    }
                    return;
                }

                int newAddress = getNextAvailableAddress(address);
                if (newAddress <= 0) {

                    stop();
                    if (callback != null) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                callback.terminalWithNoMoreNewAddresses(DevicePairingManager.this);
                            }
                        });
                    }
                    return;
                }

                cancelTimer();

                PairingModel model = new PairingModel();
                model.oldAddress = address;
                model.newAddress = newAddress;
                model.deviceType = deviceType;
                model.macData = macData;
                models.add(model);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        deviceGettingTimerAction();
                    }
                }, devicesGettingInterval);
            }

        };
    }

    private void scanningConnectingTimerAction() {

        stop();
        if (callback == null) return;
        new Handler(Looper.getMainLooper()).post(new TimerTask() {
            @Override
            public void run() {

                callback.failToConnect(DevicePairingManager.this);
            }
        });
    }

    private void deviceGettingTimerAction() {

        Log.i(LOG_TAG, "change models addresses");

        cancelTimer();
        state = State.addressChanging;

        for (PairingModel model : models) {

            MeshCommand cmd = MeshCommand.changeAddress(model.oldAddress, model.newAddress);
            MeshManager.getInstance().send(cmd);
        }

        long consumeInterval = models.size() * MeshManager.getInstance().sendingTimeInterval + addressChangingInterval;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                addressChangingAction();
            }
        }, consumeInterval);
    }

    private void addressChangingAction() {

        cancelTimer();
        state = State.networkSetting;

        MeshAddressManager addressManager = MeshAddressManager.getInstance(ctx);
        for (PairingModel model : models) {
            addressManager.append(model.newAddress, network);
        }

        Log.i(LOG_TAG, "networkSetting");

        MeshManager.getInstance().setNewNetwork(network, false);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                networkSettingHandler();
            }
        }, networkSettingInterval);
    }

    private void networkSettingHandler() {

        Log.i(LOG_TAG, "networkSettingsHandler " + network.getName() + " " + network.getPassword());

        cancelTimer();
        state = State.checking;

        MeshManager.getInstance().setNodeCallback(makeNodeCallback());
        MeshManager.getInstance().setDeviceCallback(makeDeviceCallback());
        MeshManager.getInstance().scanNode(MeshNetwork.factory);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkingTimerAction();
            }
        }, scanningInterval);
    }

    private void checkingTimerAction() {

        stop();
        if (callback != null) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    callback.didFinish(DevicePairingManager.this);
                }
            });
        }
    }

    private enum State {
        stopped,
        scanning,
        connecting,
        devicesGetting,
        addressChanging,
        networkSetting,
        checking
    }

    private static class PairingModel {

        int oldAddress = 0;
        int newAddress = 0;
        MeshDeviceType deviceType;
        byte[] macData;

        @Override
        public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {

            if (this.macData == null || obj == null) {
                return false;
            }

            if (obj.getClass().equals(this.getClass())) {

                PairingModel other = (PairingModel) obj;
                if (other.macData == null) return false;

                return HexUtil.encodeHexStr(this.macData).equals(HexUtil.encodeHexStr(other.macData));
            }

            return false;
        }
    }

    private static final class SingletonHolder {
        public static final DevicePairingManager instance = new DevicePairingManager();
    }

    public abstract static class Callback {

        public void terminalWithNoMoreNewAddresses(DevicePairingManager manager) {
        }

        public void failToConnect(DevicePairingManager manager) {
        }

        public void didFinish(DevicePairingManager manager) {
        }

        public void terminalWithUnsupportedDevice(DevicePairingManager manager, int address, MeshDeviceType deviceType, byte[] macData) {
        }
    }
}
