package com.sunricher.telinkblemeshlib;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MeshCommandExecutor {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    MeshCommandExecutor() {

    }

    void executeCommand(MeshCommand command) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    Thread.sleep(300);
                    MeshManager.getInstance().writeCommand(command);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        });
    }

}
