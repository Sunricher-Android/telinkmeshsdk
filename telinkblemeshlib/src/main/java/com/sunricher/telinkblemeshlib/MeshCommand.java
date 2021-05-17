package com.sunricher.telinkblemeshlib;

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
     * [7], MeshCommandConst.TAG_XX
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
        data[2] = (byte) (seqNo & 0xFF);

        data[3] = (byte) (src & 0xFF);
        data[4] = (byte) ((src >> 8) & 0xFF);
        data[5] = (byte) (dst & 0xFF);
        data[6] = (byte) ((dst >> 8) & 0xFF);

        data[7] = (byte) tag;
        data[8] = (byte)((vendorId >> 8) & 0xFF);
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

    public static MeshCommand makeWithNotifyData(byte[] data) {

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
     * Telink cmd
     */
    public static MeshCommand requestAddressMac(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_REPLACE_ADDRESS;
        cmd.dst = address;
        cmd.param = 0xFF;
        cmd.userData[0] = (byte) 0xFF;
        cmd.userData[1] = (byte) 0x01;
        cmd.userData[2] = (byte) 0x10;
        return cmd;
    }

    public static MeshCommand changeAddress(int address, int newAddress, byte[] macData) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_REPLACE_ADDRESS;
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

    public static MeshCommand resetNetwork(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_RESET_NETWORK;
        cmd.dst = address;
        // 0x01 reset network name to default value, 0x00 reset to `out_of_mesh`.
        cmd.param = 0x01;
        return cmd;
    }

    /**
     * SR cmd
     */
    public static MeshCommand requestMacDeviceType(int address) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_APP_TO_NODE;
        cmd.dst = address;
        cmd.userData[0] = (byte) 0x76;
        return cmd;
    }

    /**
     *
     * @param address
     * @param isOn
     * @param delay Range [0, 0xFFFF], default is 0.
     * @return
     */
    public static MeshCommand turnOnOff(int address, Boolean isOn, int delay) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_ON_OFF;
        cmd.dst = address;
        cmd.param = isOn ? 0x01 : 0x00;
        cmd.userData[0] = (byte) (delay & 0xFF);
        cmd.userData[1] = (byte) ((delay >> 8) & 0xFF);
        return cmd;
    }

    /**
     *
     * @param address
     * @param brightness Range [0, 100].
     * @return
     */
    public static MeshCommand setBrightness(int address, int brightness) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_BRIGHTNESS;
        cmd.dst = address;
        cmd.param = brightness;
        return cmd;
    }

    /**
     *
     * @param address
     * @param value Range [0, 100], 0 means the coolest color, 100 means the warmest color.
     * @return
     */
    public static MeshCommand setColorTemperature(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_COLOR_TEMPERATURE;
        cmd.userData[0] = (byte) (value & 0xFF);
        cmd.userData[1] = (byte) 0b0000_0000;
        return cmd;
    }

    /**
     *
     * @param address
     * @param value Range [0, 255].
     * @return
     */
    public static MeshCommand setWhite(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_COLOR_TEMPERATURE;
        cmd.userData[0] = (byte) (value & 0xFF);
        cmd.userData[1] = (byte) 0b0001_0000;
        return cmd;
    }

    /**
     *
     * @param address
     * @param value Range [0, 255].
     * @return
     */
    public static MeshCommand setRed(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_RED;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     *
     * @param address
     * @param value Range [0, 255].
     * @return
     */
    public static MeshCommand setGreen(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_GREEN;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     *
     * @param address
     * @param value Range [0, 255].
     * @return
     */
    public static MeshCommand setBlue(int address, int value) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_BLUE;
        cmd.userData[0] = (byte) (value & 0xFF);
        return cmd;
    }

    /**
     *
     * @param address
     * @param red Range [0, 255].
     * @param green Range [0, 255].
     * @param blue Range [0, 255].
     * @return
     */
    public static MeshCommand setRgb(int address, int red, int green, int blue) {

        MeshCommand cmd = new MeshCommand();
        cmd.tag = MeshCommandConst.TAG_SINGLE_CHANNEL;
        cmd.dst = address;
        cmd.param = MeshCommandConst.SINGLE_CHANNEL_RGB;
        cmd.userData[0] = (byte) (red & 0xFF);
        cmd.userData[0] = (byte) (green & 0xFF);
        cmd.userData[0] = (byte) (blue & 0xFF);
        return cmd;
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
}
