package com.sunricher.telinkblemesh.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.adapter.DefaultNetworkAdapter;
import com.sunricher.telinkblemesh.model.MyDevice;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshNode;
import com.sunricher.telinkblemeshlib.MqttMessage;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DefaultNetworkActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DefaultNetworkActivity";

    private TextView stateLabel;
    private RecyclerView recyclerView;

    private DefaultNetworkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_network);

        stateLabel = (TextView) findViewById(R.id.state_label);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        setConnecting(true);

        adapter = new DefaultNetworkAdapter();
        adapter.setClickListener(new DefaultNetworkAdapter.OnClickListener() {
            @Override
            public void onItemClick(DefaultNetworkAdapter.ViewHolder holder, int position, MyDevice device) {

                if (!device.isValid()) {
                    return;
                }

                Intent intent = new Intent(DefaultNetworkActivity.this, DeviceActivity.class);
                DeviceActivity.device = device;
                Log.i("DefaultNetwork ", "" + device.getMacData().length);
                DefaultNetworkActivity.this.startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        MeshManager.getInstance().setNodeCallback(new NodeCallback() {

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {
                Log.i(LOG_TAG, "didLoginNode " + node.getDescription());

                setConnecting(false);
//                MeshManager.getInstance().scanMeshDevices();

                String message = MqttMessage.scanMeshDevices("wd");
                MeshManager.getInstance().sendMqttMessage(message);
            }

            @Override
            public void didFailToLoginNode(MeshManager manager) {
                Log.i(LOG_TAG, "didFailToLoginNode ");

                MeshManager.getInstance().stopScanNode();
                DefaultNetworkActivity.this.finish();
            }

            @Override
            public void didGetDeviceAddress(MeshManager manager, int address) {

                MeshCommand cmd = MeshCommand.requestMacDeviceType(address);
                MeshManager.getInstance().send(cmd);

                Intent intent = new Intent("MeshManager.didGetMac");
                intent.putExtra("address", address);
                LocalBroadcastManager.getInstance(DefaultNetworkActivity.this).sendBroadcast(intent);
            }
        });

        MeshManager.getInstance().setDeviceCallback(new DeviceCallback() {

            @Override
            public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {
                Log.i(LOG_TAG, "didUpdateMeshDevices " + meshDevices.size());

                handleUpdateMeshDevices(meshDevices);
            }

            @Override
            public void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData) {
                Log.i(LOG_TAG, "didUpdateDeviceType " + deviceAddress + ", " + deviceType.getRawValue1() + ", " + deviceType.getRawValue2());

                adapter.updateDeviceType(deviceAddress, deviceType, macData);
            }
        });

        MeshManager.getInstance().scanNode(MeshNetwork.factory, true);
    }

    private void setConnecting(Boolean isConnecting) {

        this.stateLabel.setVisibility(isConnecting ? View.VISIBLE : View.INVISIBLE);
        this.recyclerView.setVisibility(isConnecting ? View.INVISIBLE : View.VISIBLE);
    }

    private void handleUpdateMeshDevices(ArrayList<MeshDevice> meshDevices) {

        for (MeshDevice meshDevice : meshDevices) {

            MyDevice device = new MyDevice(meshDevice);
            Boolean isUpdate = adapter.addOrUpdate(device);

            if (!isUpdate) {
                MeshCommand cmd = MeshCommand.requestMacDeviceType(device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(cmd);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MeshManager.getInstance().disconnect(false);
    }
}