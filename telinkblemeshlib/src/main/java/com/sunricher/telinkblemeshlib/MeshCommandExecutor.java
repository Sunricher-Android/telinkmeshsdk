package com.sunricher.telinkblemeshlib;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MeshCommandExecutor {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    MeshCommandExecutor() {

    }

    void executeNotify(byte[] data) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    MeshManager.getInstance().sendNotifyData(data);
                    Thread.sleep(500);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        });
    }

    void executeCommand(MeshCommand command) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    MeshManager.getInstance().writeCommand(command);
                    Thread.sleep(300);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        });
    }

}
