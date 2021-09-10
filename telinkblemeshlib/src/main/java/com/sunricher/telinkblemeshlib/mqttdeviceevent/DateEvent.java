package com.sunricher.telinkblemeshlib.mqttdeviceevent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DateEvent extends AbstractMqttDeviceEvent {

    private int shortAddress;
    private Date date;

    private DateEvent() {
    }

    public DateEvent(int shortAddress, Date date) {
        this.shortAddress = shortAddress;
        this.date = date;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public EventType getEventType() {
        return EventType.dateTime;
    }

    @Override
    public HashMap<String, Object> getPayloadValue() {

        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateString = dft.format(date);

        HashMap<String, Object> map = new HashMap<>();
        map.put("short_address", shortAddress);
        map.put("date", dateString);

        return map;
    }
}
