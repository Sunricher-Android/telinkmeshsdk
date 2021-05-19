package com.sunricher.telinkblemeshlib;

import android.util.Log;

import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SampleCommandCenter {

    private ScheduledExecutorService executorService;
    // milliseconds
    private long sampleInterval = 300;

    private ArrayList<MeshCommand> commands;

    SampleCommandCenter() {

        commands = new ArrayList<>();
    }

    void append(MeshCommand command) {

        commands.add(command);
        start();
    }

    void clear() {

        commands.clear();
    }

    private void start() {

        if (executorService != null) {

            if (executorService.isShutdown() || executorService.isTerminated()) {

                executorService = null;
                start();
            }

            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if (commands.isEmpty()) {
                    stop();
                    return;
                }

                int index = (int)Math.floor((commands.size() / 2.0));
                index = Math.max(index - 1, 0);

                Log.i("SampleCommandCenter", "consumeCommand " + index + " / " + commands.size());

                MeshCommand command = commands.remove(index);
                MeshManager.getInstance().send(command);

                if (commands.size() > 0) {
                    MeshCommand last = commands.get(commands.size() - 1);
                    commands.clear();
                    commands.add(last);
                }
            }
        }, 0, sampleInterval, TimeUnit.MILLISECONDS);
    }

    private void stop() {

        if (executorService != null) {

            executorService.shutdownNow();
        }
        executorService = null;
    }


}
