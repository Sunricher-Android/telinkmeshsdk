package com.sunricher.telinkblemeshlib;

public class MeshEntertainmentAction {

    /**
     * Action target address.
     */
    private int target;

    /**
     * Delay seconds, range [0, 60], default is 1.
     */
    private int delay = 1;

    private Boolean isOn = null;

    /**
     * Range [0, 100]
     */
    private Integer brightness = null;

    /**
     * Range [0, 255]
     */
    private Integer white = null;

    /**
     * Range [0, 100]
     */
    private Integer colorTemperature = null;

    /**
     * Range [0x000000, 0xFFFFFF],
     * 0xFF0000 = red,
     * 0x00FF00 = green,
     * 0x0000FF = blue, ...
     */
    private Integer rgb = null;
    private int actionIndex;

    public int getActionIndex() {
        return actionIndex;
    }

    public void setActionIndex(int actionIndex) {
        this.actionIndex = actionIndex;
    }

    public MeshEntertainmentAction(int target) {
        this.target = target;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Boolean getOn() {
        return isOn;
    }

    public void setOn(Boolean on) {
        isOn = on;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getWhite() {
        return white;
    }

    public void setWhite(Integer white) {
        this.white = white;
    }

    public Integer getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Integer colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    public Integer getRgb() {
        return rgb;
    }

    public void setRgb(Integer rgb) {
        this.rgb = rgb;
    }
}
