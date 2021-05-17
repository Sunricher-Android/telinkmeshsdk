package com.sunricher.telinkblemeshlib;

import android.util.ArraySet;

public class MeshDeviceType {

    private int rawValue1;
    private int rawValue2;
    private Category category;
    private ArraySet<Capability> capabilities = new ArraySet<>();

    MeshDeviceType(int deviceType, int subDeviceType) {

        this.rawValue1 = deviceType;
        this.rawValue2 = subDeviceType;

        switch (deviceType) {

            case 0x01:

                this.category = Category.light;
                this.capabilities = this.getLightCapabilities(subDeviceType);
                break;

            case 0x02:
            case 0x03:
            case 0x0A:
            case 0x0B:
            case 0x0C:
            case 0x0D:
            case 0x0E:
            case 0x12:
            case 0x13:
            case 0x14:
                this.category = Category.remote;
                break;

            case 0x04:
                this.category = Category.sensor;
                break;

            case 0x05:
                this.category = Category.transmitter;
                break;

            case 0x06:
                this.category = Category.peripheral;
                break;

            case 0x07:
                this.category = Category.curtain;
                break;

            case 0x08:
                this.category = Category.outlet;
                break;

            case 0x50:
                this.category = Category.bridge;
                break;

            case 0x09:
            default:
                this.category = Category.unsupported;
                break;
        }
    }

    private ArraySet<Capability> getLightCapabilities(int subDeviceType) {

        ArraySet<Capability> capabilities = new ArraySet<>();

        switch (subDeviceType) {

            // OnOff
            case 0x08:
            case 0x12:
            case 0x14:
            case 0x30:
            case 0x36:
            case 0x37:
                capabilities.add(Capability.onOff);
                break;

                // DIM
            case 0x11:
            case 0x13:
            case 0x31:
            case 0x38:
                capabilities.add(Capability.onOff);
                capabilities.add(Capability.brightness);
                break;

                // CCT
            case 0x32:
            case 0x39:
                capabilities.add(Capability.onOff);
                capabilities.add(Capability.brightness);
                capabilities.add(Capability.colorTemperature);
                break;

                // RGB
            case 0x33:
                capabilities.add(Capability.onOff);
                capabilities.add(Capability.brightness);
                capabilities.add(Capability.rgb);
                break;

                // RGBW
            case 0x34:
                capabilities.add(Capability.onOff);
                capabilities.add(Capability.brightness);
                capabilities.add(Capability.rgb);
                capabilities.add(Capability.white);
                break;

                // RGB CCT
            case 0x35:
                capabilities.add(Capability.onOff);
                capabilities.add(Capability.brightness);
                capabilities.add(Capability.rgb);
                capabilities.add(Capability.colorTemperature);
                break;

        }

        return capabilities;
    }

    public int getRawValue1() {
        return rawValue1;
    }

    public int getRawValue2() {
        return rawValue2;
    }

    public Category getCategory() {
        return category;
    }

    public ArraySet<Capability> getCapabilities() {
        return capabilities;
    }

    public String getCategoryTitle() {

        switch (this.category) {

            case light:
                return "Light";

            case remote:
                return "Remote";

            case sensor:
                return "Sensor";

            case transmitter:
                return "Transmission module";

            case peripheral:
                return "Peripheral";

            case curtain:
                return "Curtain";

            case outlet:
                return "Outlet";

            case bridge:
                return "Bridge";

            default:
                return "Unsupported";
        }
    }

    public String getCapabilityTitle(Capability capability) {

        switch (capability) {

            case onOff:
                return "OnOff";

            case brightness:
                return "Brightness";

            case colorTemperature:
                return "Color temperature";

            case white:
                return "White";

            case rgb:
                return "RGB";

            default:
                return "Unsupported";
        }
    }

    public enum Category {
        light,
        remote,
        sensor,
        transmitter,
        peripheral,
        curtain,
        outlet,
        bridge,
        unsupported,
    }

    public enum Capability {
        onOff,
        brightness,
        colorTemperature,
        white,
        rgb,
    }

}
