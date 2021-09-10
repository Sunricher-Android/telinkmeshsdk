package com.sunricher.telinkblemeshlib;


import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.sunricher.telinkblemeshlib.util.HexUtil;

import java.util.HashMap;

class MqttCommand {

    static final String METHOD_COMMAND = "Command";

    static final String VERSION_V1_0 = "1.0";

    static final String PAYLOAD_TYPE_COMMAND = "COMMAND";
    static final String PAYLOAD_TYPE_SCAN_MESH_DEVICES = "SCAN_MESH_DEVICES";

    private byte[] data = new byte[]{};
    private String commandType = PAYLOAD_TYPE_COMMAND;

    private MqttCommand() {

    }

    public String getCommandType() {
        return commandType;
    }

    public byte[] getData() {
        return data;
    }

    static MqttCommand makeCommandWithMqttMessage(String jsonMessage) {

        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map = gson.fromJson(jsonMessage, map.getClass());
        if (map == null) return null;
        LinkedTreeMap<String, Object> header = (LinkedTreeMap<String, Object>) map.get("header");
        LinkedTreeMap<String, Object> payload = (LinkedTreeMap<String, Object>) map.get("payload");
        if (header == null || payload == null) return null;
        String methodString = (String) header.get("method");
        String version = (String) header.get("version");
        String typeString = (String) payload.get("type");
        if (methodString == null || version == null || typeString == null) return null;

        MqttCommand mqttCommand = new MqttCommand();

        switch (methodString) {

            case METHOD_COMMAND:

                switch (typeString) {

                    case PAYLOAD_TYPE_COMMAND:

                        String value = (String)payload.get("value");
                        if (value == null) return null;
                        mqttCommand.commandType = PAYLOAD_TYPE_COMMAND;
                        mqttCommand.data = HexUtil.getBytesByString(value);
                        break;

                    case PAYLOAD_TYPE_SCAN_MESH_DEVICES:
                        mqttCommand.commandType = PAYLOAD_TYPE_SCAN_MESH_DEVICES;
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }

        return mqttCommand;
    }

    private static class Header {

        String method;
        String version;
        String userId;
    }

    private static class Payload {

        String type;
        String value;
    }


}
