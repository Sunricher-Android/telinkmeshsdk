package com.sunricher.telinkblemeshlib;

import android.util.Log;
import android.util.SparseArray;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.sunricher.telinkblemeshlib.util.ScanRecordUtil;

public class MeshNode {

    private static final String LOG_TAG = "MeshNode";

    private BleDevice bleDevice;
    private String name;
    private int manufacturerId;
    private int meshUuid;
    private String macAddress;
    private int productId;
    private int shortAddress;
    private int rssi;
    private MeshDeviceType deviceType;

    private MeshNode() {

    }

    static MeshNode make(BleDevice bleDevice) {

        int rssi = bleDevice.getRssi();
        byte[] scanRecord = bleDevice.getScanRecord();

        ScanRecordUtil records = ScanRecordUtil.parseFromBytes(scanRecord);
        if (records == null) {
            return null;
        }

        MeshNode node = new MeshNode();

        node.bleDevice = bleDevice;
        node.name = records.getDeviceName();
        if (node.name == null) {
            return null;
        }

        SparseArray<byte[]> manufacturerDataArray =
                records.getManufacturerSpecificData();
        if (manufacturerDataArray.size() < 1) {
            return null;
        }

        byte[] manufacturerData = manufacturerDataArray.get(0x0211);
        if (manufacturerData == null || manufacturerData.length <= 17) {
            return null;
        }

        String dataString = HexUtil.encodeHexStr(manufacturerData);

        int manufacturerId = Integer.parseInt(dataString.substring(0, 4), 16);
        if (manufacturerId != 0x1102) {
            return null;
        }
        node.manufacturerId = 0x1102;
        node.meshUuid = Integer.parseInt(dataString.substring(0, 4), 16);
        node.macAddress = bleDevice.getMac();
        node.productId = Integer.parseInt(dataString.substring(12, 16), 16);
        byte[] shortAddressBytes = {manufacturerData[10], manufacturerData[9]};
        node.shortAddress = Integer.parseInt(HexUtil.encodeHexStr(shortAddressBytes), 16);
        node.rssi = rssi;

        node.deviceType = new MeshDeviceType((node.productId >> 8) & 0xFF, node.productId & 0xFF);

        Log.i(LOG_TAG, node.getDescription());

        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeshNode another = (MeshNode) o;
        return this.macAddress.equals(another.getMacAddress());
    }

    public String getDescription() {
        return this.name
                + ", " + this.macAddress
                + ", productId " + String.format("%04X", this.productId)
                + ", shortAddress " + String.format("%04X", this.shortAddress);
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(int manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public int getMeshUuid() {
        return meshUuid;
    }

    public void setMeshUuid(int meshUuid) {
        this.meshUuid = meshUuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public MeshDeviceType getDeviceType() {
        return deviceType;
    }

    final static class UUID {

        public static final java.util.UUID accessService = java.util.UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1910");

        public static final java.util.UUID notifyCharacteristic = java.util.UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1911");

        public static final java.util.UUID commandCharacteristic = java.util.UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1912");

        public static final java.util.UUID pairingCharacteristic = java.util.UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1914");

        public static final java.util.UUID otaCharacteristic = java.util.UUID.fromString("00010203-0405-0607-0809-0A0B0C0D1913");

        public static final java.util.UUID deviceInformationService = java.util.UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

        public static final java.util.UUID firmwareCharacteristic = java.util.UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");


    }
}
