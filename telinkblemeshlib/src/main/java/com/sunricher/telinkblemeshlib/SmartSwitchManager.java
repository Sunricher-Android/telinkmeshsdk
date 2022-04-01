package com.sunricher.telinkblemeshlib;

import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sunricher.telinkblemeshlib.callback.SmartSwitchDataCallback;
import com.sunricher.telinkblemeshlib.callback.SmartSwitchNfcCallback;
import com.sunricher.telinkblemeshlib.models.SmartSwitchMode;
import com.sunricher.telinkblemeshlib.util.HexUtil;

import java.util.ArrayList;

public class SmartSwitchManager {

    private static final String LOG_TAG = "SmartSwitchManager";

    private static final int MAX_INDEX = 92;
    private static final int MAX_COUNT = 93;

    private SmartSwitchDataCallback dataCallback;
    private SmartSwitchNfcCallback nfcCallback;

    private ArrayList<MeshCommand> dataList;
    private int state;
    private int mode = SmartSwitchMode.defaultMode;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private SmartSwitchManager() {

        dataList = new ArrayList<>();
        state = State.readConfig;
    }

    public static SmartSwitchManager getInstance() {
        return SingletonHolder.instance;
    }

    public SmartSwitchDataCallback getDataCallback() {
        return dataCallback;
    }

    public void setDataCallback(SmartSwitchDataCallback dataCallback) {
        this.dataCallback = dataCallback;
    }

    public SmartSwitchNfcCallback getNfcCallback() {
        return nfcCallback;
    }

    public void setNfcCallback(SmartSwitchNfcCallback nfcCallback) {
        this.nfcCallback = nfcCallback;
    }

    public byte[] getSecretKey() {

        if (dataList.size() != MAX_COUNT) return null;

        byte[] data = new byte[MAX_INDEX * 8]; // 736 bytes
        for (int i = 0; i < MAX_INDEX; i++) {

            MeshCommand cmd = dataList.get(i);
            byte[] temp = cmd.getUserData();

            for (int j = 0; j < 8; j++) {

                int index = i * 8 + j;
                data[index] = temp[j + 1];
            }
        }

        return data;
    }

    public int getCheckSum() {

        if (dataList.size() != MAX_COUNT) return 0;
        MeshCommand cmd = dataList.get(MAX_COUNT - 1);

        int s1 = (int) cmd.getUserData()[1] & 0xFF;
        int s2 = ((int) cmd.getUserData()[2] & 0xFF) << 8;
        int s3 = ((int) cmd.getUserData()[3] & 0xFF) << 16;
        int s4 = ((int) cmd.getUserData()[4] & 0xFF) << 24;

        return s1 | s2 | s3 | s4;
    }

    public boolean isValid() {
        return getCheckSum() != 0;
    }

    public void clear() {
        dataList.clear();
    }

    public void startConfiguration(Tag tag, int mode) {

        this.mode = mode;
        connectNfcTag(tag, State.startConfig);
    }

    public void readConfiguration(Tag tag) {

        connectNfcTag(tag, State.readConfig);
    }

    public void unbindConfiguration(Tag tag) {

        connectNfcTag(tag, State.unbindConfig);
    }

    private void connectNfcTag(Tag tag, int state) {

        this.state = state;

        NfcA nfca = NfcA.get(tag);

        if (nfca == null) {

            if (nfcCallback != null) {
                nfcReadWriteFailedHandler();
            }

            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    nfca.connect();

                    // Auth
                    byte[] authBytes = new byte[]{(byte) 0x1B, (byte) 0x00, (byte) 0x00, (byte) 0xE2, (byte) 0x15};
                    byte[] authResult = nfca.transceive(authBytes);
                    String authString = HexUtil.getStringByBytes(authResult);
                    Log.i(LOG_TAG, "auth result: " + authString);

                    switch (state) {
                        case State.startConfig:

                            byte[] secretKey = getSecretKey();

                            if (!isValid() || secretKey.length != 736) {

                                nfcReadWriteFailedHandler();
                                break;
                            }

                            // Secret Key
                            for (int index = 0; index <= 183; index++) {

                                int page = index + 8;
                                int dataIndex = index * 4;
                                byte[] secretKeyBytes = new byte[]{
                                        (byte) 0xA2, (byte) page,
                                        secretKey[dataIndex],
                                        secretKey[dataIndex + 1],
                                        secretKey[dataIndex + 2],
                                        secretKey[dataIndex + 3]};
                                nfca.transceive(secretKeyBytes);
                            }

                            // Check Sum
                            int sum = getCheckSum();
                            byte sum1 = (byte) (sum & 0xFF);
                            byte sum2 = (byte) ((sum >> 8) & 0xFF);
                            byte sum3 = (byte) ((sum >> 16) & 0xFF);
                            byte sum4 = (byte) ((sum >> 24) & 0xFF);
                            byte[] checkSumBytes = new byte[]{
                                    (byte) 0xA2, (byte) 196, sum1, sum2, sum3, sum4
                            };
                            nfca.transceive(checkSumBytes);

                            // Tag
                            byte[] tagBytes = new byte[]{
                                    (byte) 0xA2, 0x07, (byte) 0x5A, (byte) 0x38, 0x00, (byte) mode,
                            };
                            nfca.transceive(tagBytes);

                            startConfigSuccessfulHandler();
                            break;

                        case State.readConfig:

                            byte[] readConfigBytes = new byte[]{(byte) 0x30, 0x07};
                            byte[] readBytes = nfca.transceive(readConfigBytes);
                            readConfigHandler(readBytes);
                            break;

                        case State.unbindConfig:

                            byte[] unbindBytes = new byte[]{(byte) 0xA2, 0x07, 0x00, 0x00, 0x00, 0x00};
                            nfca.transceive(unbindBytes);
                            unbindConfigHandler();
                            break;

                        default:
                            break;
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                    nfcReadWriteFailedHandler();

                } finally {

                    try {
                        nfca.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).run();
    }

    private void startConfigSuccessfulHandler() {

        if (nfcCallback == null) return;

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                nfcCallback.smartSwitchManagerDidConfigureSuccessful(SmartSwitchManager.this);
            }
        });
    }

    private void nfcReadWriteFailedHandler() {

        if (nfcCallback == null) return;

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                nfcCallback.smartSwitchManagerNfcReadWriteFailed(SmartSwitchManager.this, state);
            }
        });
    }

    private void readConfigHandler(byte[] bytes) {

        if (nfcCallback == null) return;

        if (bytes != null && bytes.length >= 4) {

            int first = (int) bytes[0] & 0xFF;
            int second = (int) bytes[1] & 0xFF;
            final boolean isConfigured = first == 0x5A && second == 0x38;
            final int mode = (int) bytes[3] & 0xFF;

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    nfcCallback.smartSwitchManagerDidReadConfiguration(SmartSwitchManager.this, isConfigured, mode);
                }
            });

        } else {

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    nfcCallback.smartSwitchManagerDidReadConfiguration(SmartSwitchManager.this, false, 0);
                }
            });
        }
    }

    private void unbindConfigHandler() {

        if (nfcCallback == null) return;

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                nfcCallback.smartSwitchManagerDidUnbindConfigurationSuccessful(SmartSwitchManager.this);
            }
        });
    }

    void append(MeshCommand command) {

        if (command.getTag() != MeshCommand.Const.TAG_APP_TO_NODE) return;
        if (command.getParam() != 0x11) return;

        int index = command.getUserData()[0];
        if (index != dataList.size()) {

            // Error data
            dataList.clear();
            Log.i(LOG_TAG, "smart switch failed, index != dataList.size");

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (dataCallback != null) {
                        dataCallback.smartSwitchManagerDidReceiveDataFailed(SmartSwitchManager.this);
                    }
                }
            });

            return;
        }

        dataList.add(command);

        Log.i(LOG_TAG, "smart switch getting index " + index + ", count " + dataList.size());

        if (dataCallback == null) return;

        int progress = (int) Math.round((float) index * 100.0 / (float) (MAX_INDEX));
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                dataCallback.smartSwitchManagerDidReceiveData(SmartSwitchManager.this, progress);
            }
        });

        if (index == MAX_INDEX && dataList.size() == MAX_COUNT) {

            Log.i(LOG_TAG, "smart switch data end.");

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataCallback.smartSwitchManagerDidReceiveDataEnd(SmartSwitchManager.this);
                }
            });
        }
    }

    private static final class SingletonHolder {
        static final SmartSwitchManager instance = new SmartSwitchManager();
    }

    public static final class State {

        public static final int startConfig = 1;
        public static final int readConfig = 2;
        public static final int unbindConfig = 3;
    }

}
