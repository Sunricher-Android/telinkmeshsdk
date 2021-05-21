package com.sunricher.telinkblemeshlib;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleScanState;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;
import com.sunricher.telinkblemeshlib.telink.AES;
import com.sunricher.telinkblemeshlib.telink.Arrays;
import com.sunricher.telinkblemeshlib.telink.Command;
import com.sunricher.telinkblemeshlib.telink.Opcode;
import com.sunricher.telinkblemeshlib.telink.TelinkLog;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class MeshManager {

    private static final String LOG_TAG = "MeshManager";

    private static final int TAG_LOGIN_WRITE = 1;
    private static final int TAG_LOGIN_READ = 2;

    private final byte[] loginRandm = new byte[8];

    private MeshNetwork network;

    private Boolean isAutoLogin;
    private Boolean isScanIgnoreName;
    private Boolean isLogin;
    private MeshNode connectNode;
    private NodeCallback nodeCallback;
    private DeviceCallback deviceCallback;

    private BleScanCallback scanCallback;
    private BleGattCallback gattCallback;
    private BleNotifyCallback notifyCallback;
    private BleReadCallback loginReadCallback;
    private BleReadCallback notifyReadCallback;
    private BleReadCallback commandReadCallback;
    private BleWriteCallback notifyWriteCallback;
    private BleWriteCallback commandWriteCallback;

    private byte[] sessionKey;
    private Random random = new SecureRandom();
    private byte[] macBytes;

    private MeshCommandExecutor commandExecutor = new MeshCommandExecutor();
    private SampleCommandCenter sampleCommandCenter = new SampleCommandCenter();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private SetNetworkState setNetworkState = SetNetworkState.none;
    private byte[] factoryLtk = new byte[]{
            (byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4,
            (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xD8, (byte) 0xD9,
            (byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE,
            (byte) 0xDF};

    private MeshManager() {
        Log.i(LOG_TAG, "created");
    }

    public static MeshManager getInstance() {
        return MeshManagerHolder.instance;
    }

    public void init(Application application) {

        Log.i(LOG_TAG, "init");

        BleManager.getInstance().init(application);

        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(3, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);

        scanCallback = makeScanCallback();
        gattCallback = makeGattCallback();

        notifyCallback = makeNotifyCallback();
        loginReadCallback = makeLoginReadCallback();
        notifyReadCallback = makeNotifyReadCallback();
        commandReadCallback = makeCommandReadCallback();
        notifyWriteCallback = makeNotifyWriteCallback();
        commandWriteCallback = makeCommandWriteCallback();
    }

    public void scanNode(MeshNetwork network, Boolean autoLogin, Boolean ignoreName) {

        this.network = network;
        this.isAutoLogin = autoLogin;
        this.isScanIgnoreName = ignoreName;

        this.stopScanNode();
        this.disconnect(autoLogin);

        if (!BleManager.getInstance().isBlueEnable()) {
            return;
        }

        BleScanRuleConfig.Builder builder = new BleScanRuleConfig.Builder();
        if (!ignoreName) {
            builder.setDeviceName(true, network.getName());
        }
        builder.setScanTimeOut(0);
        BleScanRuleConfig config = builder.build();
        BleManager.getInstance().initScanRule(config);

        BleManager.getInstance().scan(scanCallback);
    }

    /**
     * this.scanNode(network, autoLogin, ignoreName: false)
     *
     * @param network
     * @param autoLogin
     */
    public void scanNode(MeshNetwork network, Boolean autoLogin) {
        this.scanNode(network, autoLogin, false);
    }

    /**
     * this.scan(network, autoLogin: false, ignoreName: false)
     *
     * @param network
     */
    public void scanNode(MeshNetwork network) {
        this.scanNode(network, false, false);
    }

    public void stopScanNode() {

        if (!BleManager.getInstance().isBlueEnable()) {
            return;
        }

        if (BleManager.getInstance().getScanSate() == BleScanState.STATE_SCANNING) {
            BleManager.getInstance().cancelScan();
        }
    }

    public void connect(MeshNode node) {

        this.sampleCommandCenter.clear();

        if (!BleManager.getInstance().isBlueEnable()) {
            return;
        }

        this.stopScanNode();
        this.disconnect(this.isAutoLogin);

        this.connectNode = node;
        BleManager.getInstance().connect(node.getBleDevice(), gattCallback);
    }

    /**
     * disconnect(autoLogin: false)
     */
    public void disconnect() {

        this.disconnect(false);
    }

    public void disconnect(Boolean autoLogin) {

        this.sampleCommandCenter.clear();

        this.isAutoLogin = autoLogin;
        this.isLogin = false;
        this.connectNode = null;

        BleManager.getInstance().disconnectAllDevice();
    }

    public Boolean isConnected() {
        return (connectNode != null)
                && BleManager.getInstance().isConnected(connectNode.getBleDevice());
    }

    public void setNodeCallback(NodeCallback nodeCallback) {
        this.nodeCallback = nodeCallback;
    }

    public void setDeviceCallback(DeviceCallback deviceCallback) {
        this.deviceCallback = deviceCallback;
    }

    /**
     * Scan mesh devices in the current network after login.
     */
    public void scanMeshDevices() {

        Log.i(LOG_TAG, "scanMeshDevices");

        byte[] data = new byte[]{0x01};
        this.commandExecutor.executeNotify(data);
    }

    void sendNotifyData(byte[] data) {

        this.write(MeshNode.UUID.accessService, MeshNode.UUID.notifyCharacteristic, data, notifyWriteCallback);
    }

    public void sendSample(MeshCommand command) {

        sampleCommandCenter.append(command);
    }

    /**
     * Send command with response, interval 300.
     *
     * @param command
     */
    public void send(MeshCommand command) {

        this.send(command, 300);
    }

    public void send(MeshCommand command, long interval) {

        this.commandExecutor.executeCommand(command, interval);
    }

    void setNewNetwork(MeshNetwork newNetwork) {

        setNetworkState = SetNetworkState.processing;
        Log.i(LOG_TAG, "setNetNetwork " + newNetwork.getName() + ", " + newNetwork.getPassword());

        byte[] nn;
        byte[] pwd;
        byte[] ltk;

        try {

            nn = AES.encrypt(this.sessionKey, newNetwork.getMeshName());
            pwd = AES.encrypt(this.sessionKey, newNetwork.getMeshPassword());
            ltk = AES.encrypt(this.sessionKey, this.factoryLtk);

            Arrays.reverse(nn, 0, nn.length - 1);
            Arrays.reverse(pwd, 0, pwd.length - 1);
            Arrays.reverse(ltk, 0, ltk.length - 1);

        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchPaddingException | UnsupportedEncodingException
                | IllegalBlockSizeException | BadPaddingException
                | NoSuchProviderException e) {

            return;
        }

        byte[] nnData = new byte[20];
        nnData[0] = Opcode.BLE_GATT_OP_PAIR_NETWORK_NAME.getValue();
        System.arraycopy(nn, 0, nnData, 1, nn.length);

        byte[] pwdData = new byte[20];
        pwdData[0] = Opcode.BLE_GATT_OP_PAIR_PASS.getValue();
        System.arraycopy(pwd, 0, pwdData, 1, pwd.length);

        byte[] ltkData = new byte[20];
        ltkData[0] = Opcode.BLE_GATT_OP_PAIR_LTK.getValue();
//        ltkData[17] = 0x01;
        System.arraycopy(ltk, 0, ltkData, 1, ltk.length);

        UUID serviceUUID = MeshNode.UUID.accessService;
        UUID pairUUID = MeshNode.UUID.pairingCharacteristic;

        new Thread(new Runnable() {
            @Override
            public void run() {

                BleWriteCallback writeCallback = new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {

                        Log.i(LOG_TAG, "setNetwork onWriteSuccess");
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {

                        Log.i(LOG_TAG, "setNetwork onWriteFailure " + exception.getDescription());
                    }
                };
                try {

                    Log.i(LOG_TAG, "will send " + HexUtil.encodeHexStr(nnData));
                    Log.i(LOG_TAG, "will send " + HexUtil.encodeHexStr(pwdData));
                    Log.i(LOG_TAG, "will send " + HexUtil.encodeHexStr(ltkData));

                    MeshManager.this.write(serviceUUID, pairUUID, nnData, writeCallback);
                    Thread.sleep(300);

                    MeshManager.this.write(serviceUUID, pairUUID, pwdData, writeCallback);
                    Thread.sleep(300);

                    MeshManager.this.write(serviceUUID, pairUUID, ltkData, writeCallback);
                    Thread.sleep(300);

                    MeshManager.this.read(serviceUUID, pairUUID, new BleReadCallback() {
                        @Override
                        public void onReadSuccess(byte[] data) {

                            MeshManager.this.setNetworkState = SetNetworkState.none;
                            Log.i(LOG_TAG, "setNetwork onReadSuccess pairUUID " + HexUtil.encodeHexStr(data));

                            if (data.length > 0 && data[0] == 0x07) {

                                Log.i(LOG_TAG, "setNetworkState success");
                                MeshManager.this.read(MeshNode.UUID.deviceInformationService, MeshNode.UUID.firmwareCharacteristic, new BleReadCallback() {
                                    @Override
                                    public void onReadSuccess(byte[] data) {

                                        Log.i(LOG_TAG, "setNetwork onReadSuccess firmwareCharacteristic " + HexUtil.encodeHexStr(data));
                                    }

                                    @Override
                                    public void onReadFailure(BleException exception) {

                                        Log.i(LOG_TAG, "setNetwork onReadFailure " + exception.getDescription());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onReadFailure(BleException exception) {

                            Log.i(LOG_TAG, "setNetwork onReadFailure " + exception.getDescription());
                        }
                    });
                    Thread.sleep(300);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        }).start();

    }

    public Boolean isBlueEnable() {
        return BleManager.getInstance().isBlueEnable();
    }

    public Boolean getLogin() {
        return isLogin;
    }

    public MeshNode getConnectNode() {
        return connectNode;
    }

    void writeCommand(MeshCommand command) {

        this.mainHandler.post(new Runnable() {
            @Override
            public void run() {

                if (!MeshManager.this.isConnected()) {
                    return;
                }

                byte[] commandData = command.getCommandData();

                byte[] sk = MeshManager.this.sessionKey;
                int sn = command.getSeqNo();

                Log.i(LOG_TAG, "send command " + HexUtil.encodeHexStr(commandData));

                byte[] macAddress = MeshManager.this.macBytes;
                byte[] nonce = MeshManager.this.getSecIVM(macAddress, sn);
                byte[] data = AES.encrypt(sk, nonce, commandData);

                MeshManager.this.write(MeshNode.UUID.accessService, MeshNode.UUID.commandCharacteristic, data, commandWriteCallback);
            }
        });
    }

    void write(UUID service, UUID characteristic, byte[] data, BleWriteCallback callback) {

        if (!this.isConnected()) {

            Log.e(LOG_TAG, "write failed, is not connected");
            return;
        }

        BleDevice bleDevice = MeshManager.this.connectNode.getBleDevice();
        BleManager.getInstance().write(bleDevice, service.toString(), characteristic.toString(), data, false, callback);
    }

    void read(UUID service, UUID characteristic, BleReadCallback callback) {

        if (!this.isConnected()) {

            Log.e(LOG_TAG, "read failed, is not connected");
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {

                BleDevice bleDevice = MeshManager.this.connectNode.getBleDevice();
                BleManager.getInstance().read(bleDevice, service.toString(), characteristic.toString(), callback);
            }
        });
    }

    private void reconnect() {

        Log.i(LOG_TAG, "reconnect");
        this.scanNode(this.network, this.isAutoLogin, this.isScanIgnoreName);
    }

    private void login() {
        if (this.network == null) {
            return;
        }

        Log.i(LOG_TAG, "login start");

        byte[] meshName = this.network.getMeshName();
        byte[] password = this.network.getMeshPassword();

        byte[] plaintext = new byte[16];

        for (int i = 0; i < 16; i++) {
            plaintext[i] = (byte) (meshName[i] ^ password[i]);
        }

        byte[] randm = this.generateRandom(this.loginRandm);
        byte[] sk = new byte[16];

        System.arraycopy(randm, 0, sk, 0, randm.length);

        byte[] encrypted;

        try {

            encrypted = AES.encrypt(sk, plaintext);

        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchPaddingException | UnsupportedEncodingException
                | IllegalBlockSizeException | BadPaddingException
                | NoSuchProviderException e) {

            this.disconnect(this.isAutoLogin);
            this.loginResultHandler(false);

            return;
        }

        UUID serviceUUID = MeshNode.UUID.accessService;
        UUID characteristicUUID = MeshNode.UUID.pairingCharacteristic;

        byte[] commandData = new byte[17];

        commandData[0] = Opcode.BLE_GATT_OP_PAIR_ENC_REQ.getValue();
        System.arraycopy(randm, 0, commandData, 1, randm.length);
        System.arraycopy(encrypted, 8, commandData, 9, 8);
        Arrays.reverse(commandData, 9, 16);

        Command wCmd = Command.newInstance();
        wCmd.type = Command.CommandType.WRITE;
        wCmd.data = commandData;
        wCmd.serviceUUID = serviceUUID;
        wCmd.characteristicUUID = characteristicUUID;
        wCmd.tag = TAG_LOGIN_WRITE;

        this.write(MeshNode.UUID.accessService, MeshNode.UUID.pairingCharacteristic, wCmd.data, new BleWriteCallback() {

            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                Log.i(LOG_TAG, "onWriteSuccess");
                MeshManager.this.read(MeshNode.UUID.accessService, MeshNode.UUID.pairingCharacteristic, MeshManager.this.loginReadCallback);
            }

            @Override
            public void onWriteFailure(BleException exception) {
                Log.i(LOG_TAG, "onWriteFailure " + exception.toString());
            }
        });
    }

    private byte[] generateRandom(byte[] randm) {
        this.random.nextBytes(randm);
        return randm;
    }

    private byte[] getSessionKey(byte[] meshName, byte[] password,
                                 byte[] randm, byte[] rands, byte[] sk) throws Exception {
        TelinkLog.d("getSessionKey -> 0 name : " + java.util.Arrays.toString(meshName));
        TelinkLog.d("getSessionKey -> 0 password : " + java.util.Arrays.toString(password));
        TelinkLog.d("getSessionKey -> 0 randm : " + java.util.Arrays.toString(randm));
        TelinkLog.d("getSessionKey -> 0 rands : " + java.util.Arrays.toString(rands));
        TelinkLog.d("getSessionKey -> 0 sk : " + java.util.Arrays.toString(sk));

        byte[] key = new byte[16];

        System.arraycopy(rands, 0, key, 0, rands.length);

        byte[] plaintext = new byte[16];

        for (int i = 0; i < 16; i++) {
            plaintext[i] = (byte) (meshName[i] ^ password[i]);
        }

        byte[] encrypted = AES.encrypt(key, plaintext);
        byte[] result = new byte[16];

        System.arraycopy(rands, 0, result, 0, rands.length);
        System.arraycopy(encrypted, 8, result, 8, 8);
        Arrays.reverse(result, 8, 15);

        if (!Arrays.equals(result, sk))
            return null;

        System.arraycopy(randm, 0, key, 0, randm.length);
        System.arraycopy(rands, 0, key, 8, rands.length);

        TelinkLog.d("getSessionKey -> 1 plaintext : " + java.util.Arrays.toString(plaintext));
        TelinkLog.d("getSessionKey -> 1 key : " + java.util.Arrays.toString(key));
        byte[] sessionKey = AES.encrypt(plaintext, key);
        Arrays.reverse(sessionKey, 0, sessionKey.length - 1);
        TelinkLog.d("getSessionKey -> 1 sessionKey : " + java.util.Arrays.toString(sessionKey));
        return sessionKey;
    }

    private byte[] makeMacBytes(String mac) {

        byte[] macBytes = null;

        String[] strArray = mac.split(":");
        int length = strArray.length;
        macBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            macBytes[i] = (byte) (Integer.parseInt(strArray[i], 16) & 0xFF);
        }

        Arrays.reverse(macBytes, 0, length - 1);

        return macBytes;
    }

    private byte[] getSecIVM(byte[] meshAddress, int sn) {

        byte[] ivm = new byte[8];

        System.arraycopy(meshAddress, 0, ivm, 0, meshAddress.length);

        ivm[4] = 0x01;
        ivm[5] = (byte) (sn >> 16 & 0xFF);
        ivm[7] = (byte) (sn & 0xFF);
        ivm[6] = (byte) (sn >> 8 & 0xFF);

        return ivm;
    }

    private byte[] getSecIVS(byte[] macAddress) {

        byte[] ivs = new byte[8];

        ivs[0] = macAddress[0];
        ivs[1] = macAddress[1];
        ivs[2] = macAddress[2];

        return ivs;
    }

    private BleNotifyCallback makeNotifyCallback() {

        return new BleNotifyCallback() {

            @Override
            public void onNotifySuccess() {
                Log.i(LOG_TAG, "onNotifySuccess");
            }

            @Override
            public void onNotifyFailure(BleException exception) {
                Log.i(LOG_TAG, "onNotifyFailure " + exception.toString());
            }

            @Override
            public void onCharacteristicChanged(byte[] data) {

                byte[] macAddress = MeshManager.this.macBytes;
                byte[] nonce = MeshManager.this.getSecIVS(macAddress);
                System.arraycopy(data, 0, nonce, 3, 5);
                byte[] sk = MeshManager.this.sessionKey;
                byte[] result = AES.decrypt(sk, nonce, data);

                if (result == null || result.length < 1) {
                    return;
                }

                Log.i(LOG_TAG, "onCharacteristicChanged " + HexUtil.encodeHexStr(result));
                MeshManager.this.handleNotifyValue(result);
            }
        };
    }

    private BleReadCallback makeLoginReadCallback() {

        return new BleReadCallback() {

            @Override
            public void onReadSuccess(byte[] data) {

                Log.i(LOG_TAG, "login onReadSuccess " + HexUtil.encodeHexStr(data));

                if (data.length <= 16) {

                    Log.i(LOG_TAG, "login failed, data length <= 16, " + data.length);
                    MeshManager.this.loginResultHandler(false);
                    return;
                }

                byte[] sk = new byte[16];
                byte[] rands = new byte[8];

                System.arraycopy(data, 1, sk, 0, 16);
                System.arraycopy(data, 1, rands, 0, 8);

                byte[] meshName = MeshManager.this.network.getMeshName();
                byte[] password = MeshManager.this.network.getMeshPassword();
                byte[] sessionKey;

                try {

                    sessionKey = getSessionKey(meshName, password, loginRandm, rands, sk);
                    MeshManager.this.sessionKey = sessionKey;

                    if (sessionKey == null) {

                        MeshManager.this.loginResultHandler(false);
                        return;
                    }

                    MeshManager.this.loginResultHandler(true);

                } catch (Exception e) {

                    MeshManager.this.loginResultHandler(false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onReadFailure(BleException exception) {

                Log.i(LOG_TAG, "onReadFailure " + exception.toString());
                MeshManager.this.loginResultHandler(false);
            }
        };
    }

    private void loginResultHandler(Boolean isSuccess) {

        if (isSuccess) {

            this.isLogin = true;
            Log.i(LOG_TAG, "login successful");

            if (this.nodeCallback != null) {
                this.nodeCallback.didLoginNode(this, this.connectNode);
            }

        } else {

            this.disconnect(this.isAutoLogin);
            Log.i(LOG_TAG, "login failed");

            if (this.nodeCallback != null) {
                this.nodeCallback.didFailToLoginNode(this);
            }
        }
    }

    private BleScanCallback makeScanCallback() {

        return new BleScanCallback() {

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.i(LOG_TAG, "onScanFinished");
            }

            @Override
            public void onScanStarted(boolean success) {
                Log.i(LOG_TAG, "onScanStarted");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {

                MeshNode node = MeshNode.make(bleDevice);
                if (node == null) {
                    return;
                }

                Log.i(LOG_TAG, "onScanning " + node.getDescription() + ", MAC " + node.getMacAddress());

                if (MeshManager.this.nodeCallback != null) {
                    MeshManager.this.nodeCallback.didDiscoverNode(MeshManager.this, node);
                }

                if (MeshManager.this.isAutoLogin
                        && MeshManager.this.network.getName().equals(node.getName())
                        && MeshManager.this.connectNode == null) {
                    MeshManager.this.connect(node);
                }
            }
        };
    }

    private BleGattCallback makeGattCallback() {

        return new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.i(LOG_TAG, "onStartConnect");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.i(LOG_TAG, "onConnectFail " + exception.toString());

                MeshManager.this.connectNode = null;
                if (MeshManager.this.isAutoLogin) {
                    MeshManager.this.reconnect();
                }

                if (nodeCallback != null) {
                    nodeCallback.didFailToConnectNode(MeshManager.this, MeshManager.this.connectNode);
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.i(LOG_TAG, "onConnectSuccess, status " + status);

                MeshNode node = MeshManager.this.connectNode;

                MeshManager.this.stopScanNode();
                MeshManager.this.macBytes = MeshManager.this.makeMacBytes(bleDevice.getMac());

                if (nodeCallback != null && MeshManager.this.connectNode != null) {
                    nodeCallback.didConnectNode(MeshManager.this, node);
                }

                BleManager.getInstance().notify(
                        bleDevice,
                        MeshNode.UUID.accessService.toString(),
                        MeshNode.UUID.notifyCharacteristic.toString(),
                        MeshManager.this.notifyCallback);

                List<BluetoothGattService> services = BleManager.getInstance().getBluetoothGattServices(bleDevice);
                for (BluetoothGattService service : services) {
                    if (service.getUuid().toString().equals(MeshNode.UUID.accessService.toString())) {
                        BluetoothGattCharacteristic commandCharacteristic = service.getCharacteristic(MeshNode.UUID.commandCharacteristic);
                        if (commandCharacteristic != null) {
                            commandCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            Log.w(LOG_TAG, "set commandCharacteristic writeType WRITE_TYPE_NO_RESPONSE");
                        }
                    }
                }

                MeshManager.this.login();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.i(LOG_TAG, "onDisConnected isActive " + isActiveDisConnected + ", status " + status + ", isAutoLogin " + MeshManager.this.isAutoLogin);

                MeshNode node = MeshManager.this.connectNode;
                MeshManager.this.connectNode = null;
                MeshManager.this.isLogin = false;

                if (MeshManager.this.isAutoLogin) {
                    MeshManager.this.reconnect();
                }

                if (nodeCallback != null) {
                    nodeCallback.didDisconnectNode(MeshManager.this, isActiveDisConnected, node, gatt);
                }
            }
        };
    }

    private BleReadCallback makeNotifyReadCallback() {

        return new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
                Log.i(LOG_TAG, "notifyReadCallback onReadSuccess " + HexUtil.encodeHexStr(data));
            }

            @Override
            public void onReadFailure(BleException exception) {
                Log.i(LOG_TAG, "notifyReadCallback onReadFailure");
            }
        };
    }

    private BleReadCallback makeCommandReadCallback() {

        return new BleReadCallback() {
            @Override
            public void onReadSuccess(byte[] data) {
                Log.i(LOG_TAG, "commandReadCallback onReadSuccess");
            }

            @Override
            public void onReadFailure(BleException exception) {
                Log.i(LOG_TAG, "commandReadCallback onReadFailure");
            }
        };
    }

    private BleWriteCallback makeNotifyWriteCallback() {

        return new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                Log.i(LOG_TAG, "notifyWriteCallback onWriteSuccess");
                MeshManager.this.read(MeshNode.UUID.accessService, MeshNode.UUID.notifyCharacteristic, MeshManager.this.notifyReadCallback);
            }

            @Override
            public void onWriteFailure(BleException exception) {
                Log.i(LOG_TAG, "notifyWriteCallback onWriteFailure");
            }
        };
    }

    private BleWriteCallback makeCommandWriteCallback() {

        return new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                Log.i(LOG_TAG, "commandWriteCallback onWriteSuccess");
                // !!! DO NOT READ WITHOUT RESPONSE
            }

            @Override
            public void onWriteFailure(BleException exception) {
                Log.i(LOG_TAG, "commandWriteCallback onWriteFailure " + exception.getDescription());
            }
        };
    }

    private void handleNotifyValue(byte[] data) {

        if (data.length != 20) {
            Log.e(LOG_TAG, "handleNotifyValue return because data.length != 20");
            return;
        }

        int tagValue = ((int) data[7]) & 0xFF;

        switch (tagValue) {

            case MeshCommand.Const.TAG_LIGHT_STATUS:
                Log.i(LOG_TAG, "light status tag");
                this.handleLightStatusData(data);
                break;

            case MeshCommand.Const.TAG_NODE_TO_APP:
                Log.i(LOG_TAG, "node to app tag");
                this.handleNodeToAppData(data);
                break;

            case MeshCommand.Const.TAG_APP_TO_NODE:
                Log.i(LOG_TAG, "app to node tag");
                break;

            case MeshCommand.Const.TAG_ON_OFF:
                Log.i(LOG_TAG, "on off tag");
                break;

            case MeshCommand.Const.TAG_BRIGHTNESS:
                Log.i(LOG_TAG, "brightness tag");
                break;

            case MeshCommand.Const.TAG_SINGLE_CHANNEL:
                Log.i(LOG_TAG, "single channel tag");
                break;

            case MeshCommand.Const.TAG_REPLACE_ADDRESS:
                Log.i(LOG_TAG, "replace address tag");
                break;

            case MeshCommand.Const.TAG_GET_MAC_NOTIFY:
                Log.i(LOG_TAG, "get mac tag");
                this.handleGetMacNotifyData(data);
                break;

            case MeshCommand.Const.TAG_RESET_NETWORK:
                Log.i(LOG_TAG, "reset network tag");
                break;

            default:
                Log.e(LOG_TAG, "handleNotifyValue unknown tag " + tagValue);
        }
    }

    private void handleLightStatusData(byte[] data) {

        ArrayList<MeshDevice> devices = MeshDevice.makeMeshDevices(data);
        if (devices.isEmpty()) {
            return;
        }

        for (MeshDevice device : devices) {
            Log.i(LOG_TAG, "Get MeshDevice " + device.getDescription());
        }

        if (deviceCallback != null) {
            deviceCallback.didUpdateMeshDevices(this, devices);
        }
    }

    private void handleNodeToAppData(byte[] data) {

        MeshCommand command = MeshCommand.makeWithNotifyData(data);
        if (command == null) {
            Log.e(LOG_TAG, "handleNodeToAppData failed, cannot covert to a MeshCommand");
            return;
        }

        byte[] userData = command.getUserData();
        int srIdentifier = (int) userData[0] & 0xFF;
        int address = command.getSrc();

        switch (srIdentifier) {

            case MeshCommand.Const.SR_IDENTIFIER_MAC:

                int rawValue1 = (int) command.getUserData()[1] & 0xFF;
                int rawValue2 = (int) command.getUserData()[2] & 0xFF;
                MeshDeviceType deviceType = new MeshDeviceType(rawValue1, rawValue2);
                byte[] macData = new byte[]{userData[8], userData[7], userData[6], userData[5], userData[4], userData[3]};

                Log.i(LOG_TAG, "DeviceType " + address + ", MAC " + HexUtil.encodeHexStr(macData));

                if (this.deviceCallback != null) {
                    this.deviceCallback.didUpdateDeviceType(this, address, deviceType, macData);
                }
                break;

            default:
                Log.e(LOG_TAG, "unknown srIdentifier " + srIdentifier);
        }
    }

    private void handleGetMacNotifyData(byte[] data) {

        MeshCommand command = MeshCommand.makeWithNotifyData(data);
        if (command == null) {
            Log.e(LOG_TAG, "handleGetMacNotifyData failed, cannot covert to a MeshCommand");
            return;
        }

        int newAddress = command.getParam();
        byte[] userData = command.getUserData();
        byte[] macData = new byte[]{userData[6], userData[5], userData[4], userData[3], userData[2], userData[1]};
        Log.i(LOG_TAG, "handleNewNodeAddressData address " + String.format("%02X", newAddress) + ", " + HexUtil.encodeHexStr(macData));

        if (nodeCallback != null) {
            nodeCallback.didGetMac(this, macData, newAddress);
        }
    }

    private enum SetNetworkState {
        none, processing
    }

    private static class MeshManagerHolder {
        private static final MeshManager instance = new MeshManager();
    }

}
