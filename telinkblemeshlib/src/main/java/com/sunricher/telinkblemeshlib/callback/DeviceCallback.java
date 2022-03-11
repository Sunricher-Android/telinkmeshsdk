package com.sunricher.telinkblemeshlib.callback;


import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;

import java.util.ArrayList;
import java.util.Date;

public abstract class DeviceCallback {

    public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {
    }

    public void didUpdateDeviceType(MeshManager manager, int address, MeshDeviceType deviceType, byte[] macData) {
    }

    public void didGetDate(MeshManager manager, int address, Date date) {
    }

    public void didGetLightOnOffDuration(MeshManager manager, int address, int duration) {
    }

    public void didGetFirmwareVersion(MeshManager manager, int address, String version) {
    }

    public void didGetGroups(MeshManager manager, int address, ArrayList<Integer> groups) {
    }

    public void didGetDeviceAddress(MeshManager manager, int address) {
    }

    public void didGetLightRunningMode(MeshManager manager, int address, MeshCommand.LightRunningMode mode) {
    }

    public void didGetLightRunningModeIdList(MeshManager manager, int address, ArrayList<Integer> idList) {
    }

    public void didGetLightRunningModeId(MeshManager manager, int address, int modeId, int colorsCount, int colorIndex, MeshCommand.LightRunningMode.Color color) {
    }

    public void didGetLightSwitchType(MeshManager manager, int address, int switchType) {
    }

    public void didGetLightPwmFrequency(MeshManager manager, int address, int frequency) {
    }

    public void didGetRgbIndependenceState(MeshManager manager, int address, boolean isEnabled) {
    }

    /**
     * @param manager
     * @param address
     * @param isNegative    Timezone is negative?
     * @param hour          Timezone hour
     * @param minute        Timezone minute
     * @param sunriseHour
     * @param sunriseMinute
     * @param sunsetHour
     * @param sunsetMinute
     */
    public void didGetTimezone(MeshManager manager, int address, boolean isNegative, int hour, int minute, int sunriseHour, int sunriseMinute, int sunsetHour, int sunsetMinute) {
    }

    public void didGetLocation(MeshManager manager, int address, float longitude, float latitude) {
    }

    public void didGetSunriseSunsetAction(MeshManager manager, int address, MeshCommand.SunriseSunsetAction action) {
    }

    public void didGetScene(MeshManager manager, int address, MeshCommand.Scene scene) {
    }

    public void didGetAlarm(MeshManager manager, int address, MeshCommand.AbstractAlarm alarm) {
    }

    /**
     *
     * @param manager
     * @param address Device address.
     * @param switchId If value is 0 means is not a valid switch ID.
     * @param index Index of the smart switch.
     * @param count Total smart switch count.
     */
    public void didGetSmartSwitchId(MeshManager manager, int address, long switchId, int index, int count) {
    }

}