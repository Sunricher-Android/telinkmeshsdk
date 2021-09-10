package com.sunricher.telinkblemeshlib.mqttdeviceevent;

public abstract class AbstractMqttDeviceEvent {

    public enum EventType {

        state,
        deviceType,
        dateTime,
        lightOnOffDuration,
        firmwareVersion
    }

    public abstract EventType getEventType();

    public abstract Object getPayloadValue();

    public String getPayloadType() {

        switch (getEventType()) {

            case state:
                return "STATE";

            case deviceType:
                return "DEVICE_TYPE";

            case dateTime:
                return "DATE_TIME";

            case lightOnOffDuration:
                return "LIGHT_ON_OFF_DURATION";

            case firmwareVersion:
                return "FIRMWARE_VERSION";
        }
        return "";
    }
}
