package com.sunricher.telinkblemesh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MqttMessage;
import com.sunricher.telinkblemeshlib.callback.DeviceEventCallback;
import com.sunricher.telinkblemeshlib.mqttdeviceevent.AbstractMqttDeviceEvent;
import com.sunricher.telinkblemeshlib.mqttdeviceevent.StateEvent;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MeshManager.getInstance().init(getApplication());

        MeshManager.getInstance().setDeviceEventCallback(new DeviceEventCallback() {
            @Override
            public void didUpdateEvent(MeshManager manager, AbstractMqttDeviceEvent event) {

                String message = MqttMessage.deviceEvent(event, "maginawin");
                Log.i(LOG_TAG, "didUpdateEvent " + message);

                switch (event.getEventType()) {

                    case state:
                        if (event.getClass() == StateEvent.class) {

                            StateEvent stateEvent = (StateEvent) event;
                            Log.i(LOG_TAG, "State event meshDevices.count " + stateEvent.getMeshDevices().size());
                        }
                        break;

                    default:
                        break;
                }

            }
        });

        Button defaultNetworkBtn = (Button) findViewById(R.id.default_network_btn);
        defaultNetworkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, DefaultNetworkActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button androidTelinkBtn = (Button) findViewById(R.id.android_telink_btn);
        androidTelinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AndroidTelinkActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button allDevices = (Button) findViewById(R.id.all_devices_btn);
        allDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AllDevicesActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

//        Button startScanBtn = (Button)findViewById(R.id.default_network_btn);
//        startScanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                MeshManager.getInstance().setNodeCallback(new MeshManagerNodeCallback() {
//
//                    @Override
//                    public void didDiscoverNode(MeshManager manager, MeshNode node) {
//
//                        if (node.getName().equals(MeshNetwork.factory.getName())) {
//
//                            manager.connect(node);
//                        }
//                    }
//
//                    @Override
//                    public void didFailToLoginNode(MeshManager manager) {
//
//                        Toast.makeText(MainActivity.this, "login failed", Toast.LENGTH_SHORT).show();
//                    }
//
//
//                    @Override
//                    public void didLoginNode(MeshManager manager, MeshNode node) {
//
//                        Toast.makeText(MainActivity.this, "login successful", Toast.LENGTH_SHORT).show();
//
//                        manager.scanMeshDevices();
//                    }
//                });
//
//                MeshManager.getInstance().scanNode(MeshNetwork.factory, false, true);
//            }
//        });
    }
}