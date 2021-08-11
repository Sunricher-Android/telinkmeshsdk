package com.sunricher.telinkblemeshlib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;
import com.sunricher.telinkblemeshlib.telink.Arrays;

import java.util.Timer;
import java.util.TimerTask;

public class MeshOtaManager {

    private static final String LOG_TAG = "MeshOtaManager";
    private final OtaPacketParser otaPacketParser = new OtaPacketParser();
    private int address;
    private MeshNetwork network;
    private Callback callback;
    private State state = State.stopped;
    private final BleWriteCallback writeCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(int current, int total, byte[] justWrite) {

            if (state != State.dataSending) {
                return;
            }

            boolean isLast = !otaPacketParser.hasNextPacket();
            boolean isProgressUpdated = otaPacketParser.invalidateProgress();
            int newProgress = otaPacketParser.getProgress();
            Log.i(LOG_TAG, "progress " + newProgress);

            long delay = (otaPacketParser.index == 0) ? 300 : 10;

            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendNextOtaPacketCommand();
                }
            }, delay);

            if (callback != null) {

                if (isLast) {

                    state = State.stopped;
                    callback.didUpdateComplete();

                } else if (isProgressUpdated) {

                    callback.didUpdateProgress(MeshOtaManager.this, otaPacketParser.getProgress());
                }
            }
        }

        @Override
        public void onWriteFailure(BleException exception) {

            state = State.stopped;
            if (callback != null) {
                callback.didUpdateFailed(MeshOtaManager.this, FailedReason.disconnected);
            }
        }
    };
    private Timer timer;

    private MeshOtaManager() {

    }

    public static MeshOtaManager getInstance() {
        return SingletonHolder.instance;
    }

    public MeshOtaFile getLatestOtaFile(MeshDeviceType deviceType) {

        try {

            return MeshOtaFile.getOtaFile(deviceType);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void startOta(int address, MeshNetwork network, MeshOtaFile otaFile, Context context) {

        this.address = address;
        this.network = network;
        this.state = State.connecting;
        this.otaPacketParser.clear();

        byte[] data = null;

        try {

            data = otaFile.getData(context);

        } catch (Exception e) {

            e.printStackTrace();
        }

        if (data == null) {

            this.state = State.stopped;
            if (callback != null) {
                callback.didUpdateFailed(this, FailedReason.invalidOtaFile);
            }
            return;
        }

        this.otaPacketParser.set(data);

        if (MeshManager.getInstance().getLogin()
                && MeshManager.getInstance().getConnectNode().getShortAddress() == address) {

            state = State.connecting;
            cancelTimer();
            startSendData();

        } else {

            connectNode();
        }
    }

    public void stopOta() {

        state = State.stopped;
        cancelTimer();
    }

    private void connectNode() {

        state = State.connecting;
        cancelTimer();

        MeshManager.getInstance().setNodeCallback(makeNodeCallback());
        MeshManager.getInstance().scanNode(network, false);

        timer = new Timer();
        long connectInterval = 16000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "Connect overtime");
                MeshManager.getInstance().stopScanNode();
                stopOta();

                new Handler(Looper.getMainLooper()).post(() -> {

                    state = State.stopped;
                    if (callback != null) {
                        callback.didUpdateFailed(MeshOtaManager.this, FailedReason.connectOvertime);
                    }
                });
            }
        }, connectInterval);
    }

    private void startSendData() {

        if (state == State.dataSending) {
            return;
        }

        state = State.dataSending;
        Log.i(LOG_TAG, "startSendData");

        sendData();
    }

    private void sendData() {

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendNextOtaPacketCommand();
        otaPacketParser.invalidateProgress();
        Log.i(LOG_TAG, "progress " + otaPacketParser.getProgress());
    }

    private NodeCallback makeNodeCallback() {

        return new NodeCallback() {

            @Override
            public void didDiscoverNode(MeshManager manager, MeshNode node) {

                if (state != State.connecting) {
                    return;
                }

                if (address != node.getShortAddress()) {
                    return;
                }

                if (manager.isConnected()) {
                    return;
                }

                manager.connect(node);
            }

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

                if (state != State.connecting) {
                    return;
                }

                cancelTimer();
                startSendData();
            }
        };
    }

    private void cancelTimer() {

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    private boolean sendNextOtaPacketCommand() {

        boolean isLast = false;
        Log.i(LOG_TAG, "sendNextOtaPacketCommand ");

        byte[] data;

        if (otaPacketParser.hasNextPacket()) {

            data = otaPacketParser.getNextPacket();

        } else {

            data = otaPacketParser.getCheckPacket();
            isLast = true;
        }

        MeshManager.getInstance().sendOtaData(data, writeCallback);

        return isLast;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private enum State {

        stopped, connecting, dataSending
    }

    public enum FailedReason {

        invalidOtaFile, disconnected, connectOvertime
    }

    public static abstract class Callback {

        public void didUpdateFailed(MeshOtaManager manager, FailedReason reason) {
        }

        /**
         * @param progress Range [0, 100]
         */
        public void didUpdateProgress(MeshOtaManager manager, int progress) {
        }

        public void didUpdateComplete() {
        }

    }

    private static class SingletonHolder {
        private static final MeshOtaManager instance = new MeshOtaManager();
    }

    private static final class OtaPacketParser {

        private int total;
        private int index = -1;
        private byte[] data;
        private int progress;

        public void set(byte[] data) {
            this.clear();

            this.data = data;
            int length = this.data.length;
            int size = 16;

            if (length % size == 0) {
                total = length / size;
            } else {
                total = (int) Math.floor((float) length / (float) size + 1.0f);
            }
        }

        public void clear() {
            this.progress = 0;
            this.total = 0;
            this.index = -1;
            this.data = null;
        }

        public boolean hasNextPacket() {
            return this.total > 0 && (this.index + 1) < this.total;
        }

        public int getNextPacketIndex() {
            return this.index + 1;
        }

        public byte[] getNextPacket() {

            int index = this.getNextPacketIndex();
            byte[] packet = this.getPacket(index);
            this.index = index;

            return packet;
        }

        public byte[] getPacket(int index) {

            int length = this.data.length;
            int size = 16;

            int packetSize;

            if (length > size) {
                if ((index + 1) == this.total) {
                    packetSize = length - index * size;
                } else {
                    packetSize = size;
                }
            } else {
                packetSize = length;
            }

            packetSize = packetSize + 4;
            byte[] packet = new byte[20];

            if (packetSize < packet.length) {
                for (int i = 2; i < packet.length - 2; i++) {
                    packet[i] = (byte) 0xFF;
                }
            }

            System.arraycopy(this.data, index * size, packet, 2, packetSize - 4);


            this.fillIndex(packet, index);
            int crc = this.crc16(packet);
            this.fillCrc(packet, crc);
            Log.i(LOG_TAG, "ota packet ---> index : " + index + " total : " + this.total + " crc : " + crc + " content : " + Arrays.bytesToHexString(packet, ":"));
            return packet;
        }

        public byte[] getCheckPacket() {
            byte[] packet = new byte[4];
            int index = this.getNextPacketIndex();
            this.fillIndex(packet, index);
            int crc = this.crc16(packet);
            this.fillCrc(packet, crc);
            Log.i(LOG_TAG, "ota check packet ---> index : " + index + " crc : " + crc + " content : " + Arrays.bytesToHexString(packet, ":"));
            return packet;
        }

        public void fillIndex(byte[] packet, int index) {
            int offset = 0;
            packet[offset++] = (byte) (index & 0xFF);
            packet[offset] = (byte) (index >> 8 & 0xFF);
        }

        public void fillCrc(byte[] packet, int crc) {
            int offset = packet.length - 2;
            packet[offset++] = (byte) (crc & 0xFF);
            packet[offset] = (byte) (crc >> 8 & 0xFF);
        }

        public int crc16(byte[] packet) {

            int length = packet.length - 2;
            short[] poly = new short[]{0, (short) 0xA001};
            int crc = 0xFFFF;
            int ds;

            for (int j = 0; j < length; j++) {

                ds = packet[j];

                for (int i = 0; i < 8; i++) {
                    crc = (crc >> 1) ^ poly[(crc ^ ds) & 1] & 0xFFFF;
                    ds = ds >> 1;
                }
            }

            return crc;
        }

        private boolean invalidateProgress() {

            float a = this.getNextPacketIndex();
            float b = this.total;

            int progress = (int) Math.floor((a / b * 100));

            if (progress == this.progress)
                return false;

            this.progress = progress;

            return true;
        }

        public int getProgress() {
            return this.progress;
        }
    }
}
