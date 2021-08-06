package com.sunricher.telinkblemeshlib;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sunricher.telinkblemeshlib.callback.NodeCallback;
import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AutoPairingManager {

    static final String LOG_TAG = "AutoPairingManager";
    private final long connectingInterval = 16000;
    private final long addressSettingInterval = 4000;
    private final long networkSettingInterval = 6000;
    private Callback callback;
    private MeshNetwork network;
    private Timer timer;
    private int newAddress = 0;
    private List<Integer> availableAddressList = new ArrayList<>();
    private State state = State.stopped;
    private Application ctx;

    private AutoPairingManager() {

    }

    public static AutoPairingManager getInstance() {
        return AutoPairingManager.SingletonHolder.instance;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void startPairing(MeshNetwork network, Application context) {

        Log.i(LOG_TAG, "startPairing " + network.getName() + ", " + network.getPassword());

        stop();
        this.ctx = context;

        availableAddressList = MeshAddressManager.getInstance(context).getAvailableAddressList(network);
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

        MeshManager.getInstance().setNodeCallback(new NodeCallback() {

            @Override
            public void didDiscoverNode(MeshManager manager, MeshNode node) {

                if (state != State.scanning) {
                    return;
                }

                cancelTimer();
                state = State.connecting;

                MeshManager.getInstance().stopScanNode();
                MeshManager.getInstance().connect(node);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        startPairing(network, ctx);
                        Log.i(LOG_TAG, "timer connecting");
                    }
                }, connectingInterval);
            }

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

                if (state != State.connecting) {
                    return;
                }

                cancelTimer();
                state = State.addressSetting;

                int newAddress = getNextAvailableAddress(node.getShortAddress());
                if (newAddress <= 0) {

                    stop();

                    if (callback == null) return;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            callback.terminalWithNoMoreNewAddresses(AutoPairingManager.this);
                        }
                    });

                    return;
                }

                AutoPairingManager.this.newAddress = newAddress;
                MeshCommand cmd = MeshCommand.changeAddress(MeshCommand.Address.connectNode, newAddress);
                MeshManager.getInstance().send(cmd);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        startPairing(network, ctx);
                        Log.i(LOG_TAG, "timer addressSetting");
                    }
                }, addressSettingInterval);
            }

            @Override
            public void didGetMac(MeshManager manager, byte[] macBytes, int address) {

                if (state != State.addressSetting) return;
                if (newAddress != address) return;

                MeshAddressManager.getInstance(ctx).append(address, network);
                availableAddressList.remove(Integer.valueOf(address));

                cancelTimer();
                state = State.networkSetting;

                MeshManager.getInstance().setNewNetwork(network, false);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        startPairing(network, ctx);
                        Log.i(LOG_TAG, "timer networkSetting");
                    }
                }, networkSettingInterval);
            }

            @Override
            public void didGetFirmware(MeshManager manager, String firmware) {

                MeshNode node = MeshManager.getInstance().getConnectNode();
                if (callback != null && node != null) {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            callback.didAddNode(AutoPairingManager.this, node, newAddress);
                        }
                    });
                }

                startPairing(network, ctx);
            }
        });

        MeshManager.getInstance().scanNode(MeshNetwork.factory);
    }

    public void stop() {

        Log.i(LOG_TAG, "stop auto pairing");

        state = State.stopped;
        cancelTimer();
        MeshManager.getInstance().stopScanNode();
        MeshManager.getInstance().disconnect();
    }

    private void cancelTimer() {

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
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

    private enum State {

        stopped,
        scanning,
        connecting,
        addressSetting,
        networkSetting,
    }

    private static class SingletonHolder {
        private static final AutoPairingManager instance = new AutoPairingManager();
    }

    public abstract static class Callback {

        public void terminalWithNoMoreNewAddresses(AutoPairingManager manager) {
        }

        public void didAddNode(AutoPairingManager manager, MeshNode node, int newAddress) {
        }
    }
}
