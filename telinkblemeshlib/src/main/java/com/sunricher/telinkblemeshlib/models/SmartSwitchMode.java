package com.sunricher.telinkblemeshlib.models;

public class SmartSwitchMode {

    public static final int onOffDim = 0xF1;
    public static final int onOffS1S2 = 0xF2;
    public static final int onOffWwCw = 0xF3;
    public static final int onOffCwRgb = 0xF4;
    public static final int s1S2S3S4 = 0xF5;
    public static final int onOffG2 = 0xF6;

    public static final int defaultMode = onOffCwRgb;

    public static String getTitle(int mode) {

        switch (mode) {
            case onOffDim:
                return "On/Off & DIM";
            case onOffS1S2:
                return "On/Off & S1/S2";
            case onOffWwCw:
                return "On/Off & WW/CW";
            case onOffCwRgb:
                return "On/Off & CW/RGB";
            case s1S2S3S4:
                return "S1/S2/S3/S4";
            case onOffG2:
                return "On/Off & On/Off";
            default:
                return "Unknown";
        }
    }

}
