package com.sunricher.telinkblemeshlib;

import com.google.gson.Gson;
import com.sunricher.telinkblemeshlib.mqttdeviceevent.AbstractMqttDeviceEvent;
import com.sunricher.telinkblemeshlib.util.HexUtil;

import java.util.HashMap;

public class MqttMessage {

    public static String meshCommand(MeshCommand command, String userId) {
        return makeMqttMessage("Command", "1.0", userId, "COMMAND", HexUtil.getStringByBytes(command.getCommandData()));
    }

    public static String scanMeshDevices(String userId) {
        return makeMqttMessage("Command", "1.0", userId, "SCAN_MESH_DEVICES", "");
    }

    public static String deviceEvent(AbstractMqttDeviceEvent event, String userId) {
        return makeMqttMessage("Event", "1.0", userId, event.getPayloadType(), event.getPayloadValue());
    }

    public static String makeMqttMessage(String method, String version, String userId, String payloadType, Object value) {

        HashMap<String, Object> map = new HashMap<>();

        HashMap<String, Object> header = new HashMap<>();
        header.put("method", method);
        header.put("version", version);
        header.put("user_id", userId);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("type", payloadType);
        payload.put("value", value);

        map.put("header", header);
        map.put("payload", payload);

        Gson gson = new Gson();
        return gson.toJson(map);
    }

}
