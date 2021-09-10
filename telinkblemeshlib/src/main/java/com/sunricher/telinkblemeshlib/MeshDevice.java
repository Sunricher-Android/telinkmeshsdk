package com.sunricher.telinkblemeshlib;

import java.util.ArrayList;
import java.util.HashMap;

public class MeshDevice {

    private State state = State.on;
    private int address = 0;
    private ArrayList<Integer> groupAddress = new ArrayList<>();
    private int brightness = 0;
    private String version = "nil";

    private MeshDevice() {

    }

    private static MeshDevice make(int address, Boolean isOnline, int brightness) {
        if (address == 0) return null;

        MeshDevice meshDevice = new MeshDevice();
        meshDevice.address = address;
        meshDevice.state = isOnline ? (brightness > 0 ? State.on : State.off) : State.offline;
        meshDevice.brightness = brightness;

        return meshDevice;
    }

    static ArrayList<MeshDevice> makeMeshDevices(byte[] bytes) {

        ArrayList<MeshDevice> devices = new ArrayList<MeshDevice>();

        int tag = (int) (bytes[7] & 0xFF);
        int vendorId0 = (int) (bytes[8] & 0xFF);
        int vendorId1 = (int) (bytes[9] & 0xFF);

        if (tag != 0xDC || vendorId0 != 0x11 || vendorId1 != 0x02) {
            return devices;
        }

        int firstDeviceAddress = (int) (bytes[10] & 0xFF);
        boolean isFirstOnline = (int) (bytes[11] & 0xFF) != 0;
        int firstBrightness = (int) (bytes[12] & 0xFF);

        MeshDevice firstDevice = MeshDevice.make(firstDeviceAddress, isFirstOnline, firstBrightness);
        if (firstDevice != null) {
            devices.add(firstDevice);
        }

        int secondDeviceAddress = (int) (bytes[14] & 0xFF);
        boolean isSecondOnline = (int) (bytes[15] & 0xFF) != 0;
        int secondBrightness = (int) (bytes[16] & 0xFF);

        MeshDevice secondDevice = MeshDevice.make(secondDeviceAddress, isSecondOnline, secondBrightness);
        if (secondDevice != null) {
            devices.add(secondDevice);
        }

        return devices;
    }

    public String getDescription() {

        String hexAddress = String.format("0x%02X", address);
        return "Address " + address + " (" + hexAddress
                + "), state " + state + ", brightness " + brightness;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public ArrayList<Integer> getGroupAddress() {
        return groupAddress;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, Object> getItemValue() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("short_address", getAddress());
        map.put("state", getState().getStateString());
        map.put("brightness", getBrightness());

        return map;
    }

    public enum State {

        offline, on, off;

        String getStateString() {
            switch (this) {
                case on:
                    return "ON";
                case off:
                    return "OFF";
                case offline:
                    return "OFFLINE";
            }
            return "";
        }
    }
}
