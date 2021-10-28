package com.sunricher.telinkblemeshlib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MeshCommand {

    /**
     * [0-2]
     */
    private static int seqNo = 1;

    /**
     * [3-4]
     */
    private int src = 0;

    /**
     * [5-6]
     */
    private int dst = 0;

    /**
     * [7], Const.TAG_XX
     */
    private int tag = 0;

    /**
     * [8-9]
     */
    private int vendorId = 0x1102;

    /**
     * [10]
     */
    private int param = 0x10;

    /**
     * [11, 19]
     */
    private byte[] userData;

    private MeshCommand() {

        this.userData = new byte[9];
    }

    static MeshCommand makeWithNotifyData(byte[] data) {

        if (data.length != 20) {
            return null;
        }

        MeshCommand command = new MeshCommand();

        int tempSrc = ((int) data[3] & 0xFF);
        tempSrc |= ((int) data[4] & 0xFF) << 8;
        command.src = tempSrc;

        int tempDst = ((int) data[5]);
        tempDst |= ((int) data[6] & 0xFF) << 8;
        command.dst = tempDst;

        command.tag = ((int) data[7] & 0xFF);

        int tempVendorId = ((int) data[8] & 0xFF) << 8;
        tempVendorId |= ((int) data[9] & 0xFF);
        command.vendorId = tempVendorId;

        command.param = ((int) data[10] & 0xFF);

        for (int i = 0; i < 9; i++) {
            int dataIndex = i + 11;
            command.userData[i] = data[dataIndex];
        }

        return command;
    }

    static MeshCommand makeWithMqttCommandData(byte[] data) {

        if (data.length != 20) {
            return null;
        }

        MeshCommand command = new MeshCommand();

        int tempSrc = ((int) data[3] & 0xFF);
        tempSrc |= ((int) data[4] & 0xFF) << 8;
        command.src = tempSrc;

        int tempDst = ((int) data[5]);
        tempDst |= ((int) data[6] & 0xFF) << 8;
        command.dst = tempDst;

        command.tag = ((int) data[7] & 0xFF);

        int tempVendorId = ((int) data[8] & 0xFF) << 8;
        tempVendorId |= ((int) data[9] & 0xFF);
        command.vendorId = tempVendorId;

        command.param = ((int) data[10] & 0xFF);

        for (int i = 0; i < 9; i++) {
            int dataIndex = i + 11;
            command.userData[i] = data[dataIndex];
        }

        return command;
    }

    /**
     * Telink cmd, request mac address.
     */
    public static MeshCommand requestAddressMac(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_REPLACE_ADDRESS;
        cmd.dst = address;
        cmd.param = 0xFF;
        cmd.userData[0] = (byte) 0xFF;
        cmd.userData[1] = (byte) 0x01;
        cmd.userData[2] = (byte) 0x10;
        return cmd;
    }

    /**
     * Change address
     *
     * @param address    Device address
     * @param newAddress New address
     * @param macData    mac address byte[]
     * @return
     */
    public static MeshCommand changeAddress(int address, int newAddress, byte[] macData) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_REPLACE_ADDRESS;
        cmd.src = 0;
        cmd.dst = address;
        cmd.param = newAddress & 0xFF;
        cmd.userData[0] = 0x00;
        cmd.userData[1] = 0x01;
        cmd.userData[2] = 0x10;
        cmd.userData[3] = macData[5];
        cmd.userData[4] = macData[4];
        cmd.userData[5] = macData[3];
        cmd.userData[6] = macData[2];
        cmd.userData[7] = macData[1];
        cmd.userData[8] = macData[0];
        return cmd;
    }

    public static MeshCommand changeAddress(int address, int newAddress) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_REPLACE_ADDRESS;
        cmd.src = 0;
        cmd.dst = address;
        cmd.param = newAddress & 0xFF;
        cmd.userData[0] = 0x00;
        return cmd;
    }

    /**
     * Reset to factory network
     *
     * @param address
     * @return
     */
    public static MeshCommand resetNetwork(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_RESET_NETWORK;
        cmd.dst = address;
        // 0x01 reset network name to default value, 0x00 reset to `out_of_mesh`.
        cmd.param = 0x01;
        return cmd;
    }

    /**
     * SR cmd, request mac address and device type.
     */
    public static MeshCommand requestMacDeviceType(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) 0x76;
        return cmd;
    }

    /**
     * @param address
     * @param isOn
     * @param delay   Range [0, 0xFFFF], default is 0.
     * @return
     */
    public static MeshCommand turnOnOff(int address, Boolean isOn, int delay) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_ON_OFF;
        cmd.dst = address;
        cmd.param = isOn ? 0x01 : 0x00;
        cmd.userData[0] = (byte) (delay & 0xFF);
        cmd.userData[1] = (byte) ((delay >> 8) & 0xFF);
        return cmd;
    }

    /**
     * @param address
     * @param brightness Range [0, 100].
     * @return
     */
    public static MeshCommand setBrightness(int address, int brightness) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_BRIGHTNESS;
        cmd.dst = address;
        cmd.param = brightness;
        return cmd;
    }

    /**
     * @param address
     * @param value   Range [0, 100], 0 means the coolest color, 100 means the warmest color.
     * @return
     */
    public static MeshCommand setColorTemperature(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_COLOR_TEMPERATURE;
        cmd.userData[0] = (byte) (value & 0xFF);
        cmd.userData[1] = (byte) 0b0000_0000;
        return cmd;
    }

    /**
     * @param address
     * @param value   Range [0, 255].
     * @return
     */
    public static MeshCommand setWhite(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_COLOR_TEMPERATURE;
        cmd.userData[0] = (byte) (value & 0xFF);
        cmd.userData[1] = (byte) 0b0001_0000;
        return cmd;
    }

    /**
     * @param address
     * @param value   Range [0, 255].
     * @return
     */
    public static MeshCommand setRed(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_RED;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     * @param address
     * @param value   Range [0, 255].
     * @return
     */
    public static MeshCommand setGreen(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_GREEN;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     * @param address
     * @param value   Range [0, 255].
     * @return
     */
    public static MeshCommand setBlue(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_BLUE;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     * @param address
     * @param red     Range [0, 255].
     * @param green   Range [0, 255].
     * @param blue    Range [0, 255].
     * @return
     */
    public static MeshCommand setRgb(int address, int red, int green, int blue) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = Const.SINGLE_CHANNEL_RGB;
        cmd.userData[0] = (byte) (red & 0xFF);
        cmd.userData[1] = (byte) (green & 0xFF);
        cmd.userData[2] = (byte) (blue & 0xFF);
        return cmd;
    }

    public static MeshCommand syncDatetime(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_SYNC_DATETIME;
        cmd.dst = address;

        Calendar cal = Calendar.getInstance(Locale.getDefault());

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        cmd.param = (year & 0xFF);
        cmd.userData[0] = (byte) ((year >> 8) & 0xFF);
        cmd.userData[1] = (byte) (month & 0xFF);
        cmd.userData[2] = (byte) (day & 0xFF);
        cmd.userData[3] = (byte) (hour & 0xFF);
        cmd.userData[4] = (byte) (minute & 0xFF);
        cmd.userData[5] = (byte) (second & 0xFF);
        return cmd;
    }

    public static MeshCommand getDatetime(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_GET_DATETIME;
        cmd.dst = address;
        cmd.param = 0x10;
        return cmd;
    }

    /**
     * @param duration Range `[1, 0xFFFF]`, unit `seconds`.
     */
    public static MeshCommand setLightOnOffDuration(int address, int duration) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = Const.LIGHT_CONTROL_MODE_LIGHT_ON_OFF_DURATION;
        cmd.userData[2] = 0x01; // set
        cmd.userData[3] = (byte) (duration & 0xFF);
        cmd.userData[4] = (byte) ((duration >> 8) & 0xFF);
        return cmd;
    }

    public static MeshCommand getLightOnOffDuration(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = Const.LIGHT_CONTROL_MODE_LIGHT_ON_OFF_DURATION;
        cmd.userData[2] = 0x00; // get
        return cmd;
    }

    public static MeshCommand getFirmwareVersion(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_GET_FIRMWARE;
        cmd.dst = address;
        return cmd;
    }

    public static MeshCommand getGroups(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_GET_GROUPS;
        cmd.dst = address;
        cmd.userData[0] = 0x01;
        return cmd;
    }

    public static MeshCommand getGroupDevices(int groupId) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_REPLACE_ADDRESS;
        cmd.dst = groupId;
        cmd.param = 0xFF;
        cmd.userData[0] = (byte) 0xFF;
        return cmd;
    }

    public static MeshCommand addGroup(int groupId, int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_GROUP_ACTION;
        cmd.dst = address;
        cmd.param = 0x01;
        cmd.userData[0] = (byte) (groupId & 0xFF);
        cmd.userData[1] = (byte) 0x80;
        return cmd;
    }

    public static MeshCommand deleteGroup(int groupId, int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_GROUP_ACTION;
        cmd.dst = address;
        cmd.param = 0x00;
        cmd.userData[0] = (byte) (groupId & 0xFF);
        cmd.userData[1] = (byte) 0x80;
        return cmd;
    }

    public static MeshCommand getLightRunningMode(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_GET_LIGHT_RUNNING_MODE;
        return cmd;
    }

    public static MeshCommand updateLightRunningMode(LightRunningMode mode) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = mode.address;
        cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_SET_LIGHT_RUNNING_MODE;
        cmd.userData[2] = (byte) mode.state;

        switch (mode.state) {

            case LightRunningMode.State.DEFAULT_MODE:
                cmd.userData[3] = (byte) mode.defaultMode;
                break;

            case LightRunningMode.State.CUSTOM_MODE:
                cmd.userData[3] = (byte) mode.customModeId;
                cmd.userData[4] = (byte) mode.customMode;
                break;
        }

        return cmd;
    }

    /**
     * @param address
     * @param speed   range [0x00, 0x0F], 0x00 -> fastest, 0x0F -> slowest.
     */
    public static MeshCommand updateLightRunningSpeed(int address, int speed) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_SET_LIGHT_RUNNING_SPEED;
        cmd.userData[2] = (byte) speed;
        return cmd;
    }

    public static MeshCommand getLightRunningCustomModeIdList(int address) {

        return getLightRunningCustomModeColors(address, 0x00);
    }

    /**
     * @param address
     * @param modeId  range [0x01, 0x10]
     */
    public static MeshCommand getLightRunningCustomModeColors(int address, int modeId) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_CUSTOM_LIGHT_RUNNING_MODE;
        cmd.userData[2] = (byte) 0x00;
        cmd.userData[3] = (byte) modeId;
        return cmd;
    }

    public static ArrayList<MeshCommand> updateLightRunningCustomModeColors(int address, int modeId, ArrayList<LightRunningMode.Color> colors) {

        assert (modeId >= 0x01 && modeId <= 0x10);
        assert (colors.size() > 0 && colors.size() <= 5);

        ArrayList<MeshCommand> commands = new ArrayList<>();

        for (int i = 0; i < colors.size(); i++) {

            int index = i + 1;
            LightRunningMode.Color color = colors.get(i);

            MeshCommand cmd = new MeshCommand();
            cmd.tag = Const.TAG_APP_TO_NODE;
            cmd.dst = address;
            cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
            cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_CUSTOM_LIGHT_RUNNING_MODE;
            cmd.userData[2] = (byte) 0x01;
            cmd.userData[3] = (byte) modeId;
            cmd.userData[4] = (byte) index;
            cmd.userData[5] = (byte) color.red;
            cmd.userData[6] = (byte) color.green;
            cmd.userData[7] = (byte) color.blue;

            commands.add(cmd);
        }

        return commands;
    }

    public static MeshCommand removeLightRunningCustomModeId(int address, int modeId) {

        assert (modeId >= 0x01 && modeId <= 0x10);

        MeshCommand cmd = new MeshCommand();
        cmd.tag = Const.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE;
        cmd.userData[1] = (byte) Const.LIGHT_CONTROL_MODE_CUSTOM_LIGHT_RUNNING_MODE;
        cmd.userData[2] = (byte) 0x02;
        cmd.userData[3] = (byte) modeId;

        return cmd;
    }

    private int increaseSeqNo() {

        seqNo += 1;
        if (seqNo >= 0xFFFFFF) {
            seqNo = 1;
        }

        return seqNo;
    }

    public byte[] getCommandData() {

        byte[] data = new byte[20];

        int seqNo = this.increaseSeqNo();
        data[0] = (byte) ((seqNo >> 16) & 0xFF);
        data[1] = (byte) ((seqNo >> 8) & 0xFF);
        data[2] = (byte) ((seqNo) & 0xFF);

        data[3] = (byte) (src & 0xFF);
        data[4] = (byte) ((src >> 8) & 0xFF);
        data[5] = (byte) (dst & 0xFF);
        data[6] = (byte) ((dst >> 8) & 0xFF);

        data[7] = (byte) tag;
        data[8] = (byte) ((vendorId >> 8) & 0xFF);
        data[9] = (byte) (vendorId & 0xFF);
        data[10] = (byte) (param);

        for (int i = 0; i < 9; i++) {
            int dataIndex = 11 + i;
            data[dataIndex] = userData[i];
        }

        return data;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public int getSrc() {
        return src;
    }

    public int getDst() {
        return dst;
    }

    public int getTag() {
        return tag;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getParam() {
        return param;
    }

    public byte[] getUserData() {
        return userData;
    }

    public static final class Address {

        public static final int all = 0xFFFF;

        public static final int connectNode = 0x0000;
    }

    static final class Const {

        static final int TAG_APP_TO_NODE = 0xEA;

        static final int TAG_NODE_TO_APP = 0xEB;

        static final int TAG_LIGHT_STATUS = 0xDC;

        static final int TAG_ON_OFF = 0xD0;

        static final int TAG_BRIGHTNESS = 0xD2;

        static final int TAG_SINGLE_CHANNEL = 0xE2;

        static final int TAG_REPLACE_ADDRESS = 0xE0;

        static final int TAG_DEVICE_ADDRESS_NOTIFY = 0xE1;

        static final int TAG_RESET_NETWORK = 0xE3;

        static final int TAG_SYNC_DATETIME = 0xE4;

        static final int TAG_GET_DATETIME = 0xE8;

        static final int TAG_DATETIME_RESPONSE = 0xE9;

        static final int TAG_GET_FIRMWARE = 0xC7;

        static final int TAG_FIRMWARE_RESPONSE = 0xC8;

        static final int TAG_GET_GROUPS = 0xDD;

        static final int TAG_RESPONSE_GROUPS = 0xD4;

        static final int TAG_GROUP_ACTION = 0xD7;


        static final int SR_IDENTIFIER_MAC = 0x76;


        static final int SINGLE_CHANNEL_RED = 0x01;

        static final int SINGLE_CHANNEL_GREEN = 0x02;

        static final int SINGLE_CHANNEL_BLUE = 0x03;

        static final int SINGLE_CHANNEL_RGB = 0x04;

        static final int SINGLE_CHANNEL_COLOR_TEMPERATURE = 0x05;


        static final int SR_IDENTIFIER_LIGHT_CONTROL_MODE = 0x01;

        static final int LIGHT_CONTROL_MODE_LIGHT_ON_OFF_DURATION = 0x0F;

        static final int LIGHT_CONTROL_MODE_GET_LIGHT_RUNNING_MODE = 0x00;

        static final int LIGHT_CONTROL_MODE_SET_LIGHT_RUNNING_MODE = 0x05;

        static final int LIGHT_CONTROL_MODE_SET_LIGHT_RUNNING_SPEED = 0x03;

        static final int LIGHT_CONTROL_MODE_CUSTOM_LIGHT_RUNNING_MODE = 0x01;

    }

    public static class LightRunningMode {

        private int address;

        private int state = State.STOPPED;

        /**
         * range [0x01, 0x14]
         */
        private int defaultMode = DefaultMode.COLORFUL_MIXED;

        /**
         * range [0x01, 0x06]
         */
        private int customMode = CustomMode.ASCEND_SHADE;

        /**
         * range [0x00, 0x0F]
         */
        private int speed = 0x0A;

        /**
         * range [0x01, 0x10]
         */
        private int customModeId = 0x01;

        private LightRunningMode() {

        }

        public LightRunningMode(int address, int state) {
            this.address = address;
            this.state = state;
        }

        static LightRunningMode makeWithUserData(int address, byte[] userData) {

            if (((int) userData[0] & 0xFF) != Const.SR_IDENTIFIER_LIGHT_CONTROL_MODE) return null;
            if (((int) userData[1] & 0xFF) != Const.LIGHT_CONTROL_MODE_GET_LIGHT_RUNNING_MODE)
                return null;

            int state = (int) userData[4] & 0xFF;
            if (state > 0x02) return null;

            LightRunningMode lightRunningMode = new LightRunningMode(address, state);
            lightRunningMode.speed = Math.max(0x00, Math.min(0x0F, (int) userData[2] & 0xFF));

            switch (state) {

                case State.STOPPED:
                    break;

                case State.DEFAULT_MODE:
                    lightRunningMode.defaultMode = (int) userData[5] & 0xFF;
                    break;

                case State.CUSTOM_MODE:
                    lightRunningMode.customModeId = Math.max(0x01, Math.min(0x10, (int) userData[5] & 0xFF));
                    lightRunningMode.customMode = (int) userData[6] & 0xFF;
                    break;
            }

            return lightRunningMode;
        }

        public int getAddress() {
            return address;
        }

        public void setAddress(int address) {
            this.address = address;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getDefaultMode() {
            return defaultMode;
        }

        public void setDefaultMode(int defaultMode) {
            this.defaultMode = defaultMode;
        }

        public int getCustomMode() {
            return customMode;
        }

        public void setCustomMode(int customMode) {
            this.customMode = customMode;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getCustomModeId() {
            return customModeId;
        }

        public void setCustomModeId(int customModeId) {
            this.customModeId = customModeId;
        }

        static final class State {

            static final int STOPPED = 0x00;
            static final int DEFAULT_MODE = 0x01;
            static final int CUSTOM_MODE = 0x02;
        }

        public static final class DefaultMode {

            public static final int COLORFUL_MIXED = 0x01;
            public static final int RED_SHADE = 0x02;
            public static final int GREEN_SHADE = 0x03;
            public static final int BLUE_SHADE = 0x04;
            public static final int YELLOW_SHADE = 0x05;
            public static final int CYAN_SHADE = 0x06;
            public static final int PURPLE_SHADE = 0x07;
            public static final int WHITE_SHADE = 0x08;
            public static final int RED_GREEN_SHADE = 0x09;
            public static final int RED_BLUE_SHADE = 0x0A;
            public static final int GREEN_BLUE_SHADE = 0x0B;
            public static final int COLORFUL_STROBE = 0x0C;
            public static final int RED_STROBE = 0x0D;
            public static final int GREEN_STROBE = 0x0E;
            public static final int BLUE_STROBE = 0x0F;
            public static final int YELLOW_STROBE = 0x10;
            public static final int CYAN_STROBE = 0x11;
            public static final int PURPLE_STROBE = 0x12;
            public static final int WHITE_STROBE = 0x13;
            public static final int COLORFUL_JUMP = 0x14;
        }

        public static final class CustomMode {

            public static final int ASCEND_SHADE = 0x01;
            public static final int DESCEND_SHADE = 0x02;
            public static final int ASCEND_DESCEND_SHADE = 0x03;
            public static final int MIXED_SHADE = 0x04;
            public static final int JUMP = 0x05;
            public static final int STROBE = 0x06;
        }

        /**
         * red, green, blue range [0, 255]
         */
        public static class Color {

            private int red;
            private int green;
            private int blue;

            public int getRed() {
                return red;
            }

            public void setRed(int red) {
                this.red = red;
            }

            public int getGreen() {
                return green;
            }

            public void setGreen(int green) {
                this.green = green;
            }

            public int getBlue() {
                return blue;
            }

            public void setBlue(int blue) {
                this.blue = blue;
            }
        }
    }

}
