package com.sunricher.telinkblemeshlib.util;

public class HexUtil {

    /**
     * byte[]转变为16进制String字符, 每个字节2位, 不足补0
     */
    public static String getStringByBytes(byte[] bytes) {
        String result = null;
        String hex = null;
        if (bytes != null && bytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
            for (byte byteChar : bytes) {
                hex = Integer.toHexString(byteChar & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                stringBuilder.append(hex.toUpperCase());
            }
            result = stringBuilder.toString();
        }
        return result;
    }

    /**
     * 把16进制String字符转变为byte[]
     */
    public static byte[] getBytesByString(String data) {
        byte[] bytes = null;
        if (data != null) {
            data = data.toUpperCase();
            int length = data.length() / 2;
            char[] dataChars = data.toCharArray();
            bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                bytes[i] = (byte) (charToByte(dataChars[pos]) << 4 | charToByte(dataChars[pos + 1]));
            }
        }
        return bytes;
    }

    /**
     * 取得在16进制字符串中各char所代表的16进制数
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 将 float 转成 bytes
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
    }

    /**
     * 将 bytes 转成 float
     *
     * @param bytes
     * @return
     */
    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

}
