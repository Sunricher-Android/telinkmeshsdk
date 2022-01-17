package com.sunricher.telinkblemeshlib;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeshEntertainmentManager {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int index = 0;
    private boolean isStarted = false;
    private ArrayList<MeshEntertainmentAction> actions = new ArrayList<>();

    private MeshEntertainmentManager() {

    }

    public static MeshEntertainmentManager getInstance() {
        return MeshEntertainmentManager.SingletonHolder.instance;
    }

    public void start(List<MeshEntertainmentAction> actions, int index) {

        this.actions = new ArrayList<>(actions);
        this.index = index;

        if (this.isStarted) {
            return;
        }

        this.isStarted = true;

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                while (isStarted) {

                    if (actions.size() > 0) {

                        for (MeshEntertainmentAction action : actions) {

                            if (!isStarted) {
                                return;
                            }

                            try {
                                Thread.sleep(action.getDelay() * 1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sendAction(action);
                        }

                    } else {

                        isStarted = false;
                    }
                }
            }
        });
    }

    public void start(List<MeshEntertainmentAction> actions) {
        this.start(actions, 0);
    }

    public void stop() {
        this.isStarted = false;
        this.actions = new ArrayList<>();
    }

    private void sendAction(MeshEntertainmentAction action) {

        Log.i("MeshEntertainmentManager", "Send action");

        if (action.getRgb() != null) {
            int rgb = action.getRgb();
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            MeshCommand cmd = MeshCommand.setRgb(action.getTarget(), red, green, blue);
            MeshManager.getInstance().send(cmd);
        }

        if (action.getColorTemperature() != null) {
            int cct = action.getColorTemperature();
            MeshCommand cmd = MeshCommand.setColorTemperature(action.getTarget(), cct);
            MeshManager.getInstance().send(cmd);
        }

        if (action.getWhite() != null) {
            int white = action.getWhite();
            MeshCommand cmd = MeshCommand.setWhite(action.getTarget(), white);
            MeshManager.getInstance().send(cmd);
        }

        if (action.getBrightness() != null) {
            int brightness = action.getBrightness();
            MeshCommand cmd = MeshCommand.setBrightness(action.getTarget(), brightness);
            MeshManager.getInstance().send(cmd);
        }

        if (action.getOn() != null) {
            boolean isOn = action.getOn();
            MeshCommand cmd = MeshCommand.turnOnOff(action.getTarget(), isOn);
            MeshManager.getInstance().send(cmd);
        }
    }

    public int getIndex() {
        return index;
    }

    public boolean isStarted() {
        return isStarted;
    }

    private static final class SingletonHolder {
        public static final MeshEntertainmentManager instance = new MeshEntertainmentManager();
    }
}
