package com.sunricher.telinkblemesh.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.sunricher.telinkblemeshlib.MeshPairingManager;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AndroidTelinkActivity extends AppCompatActivity {

    public static final MeshNetwork network = new MeshNetwork("srlink", "123456");
    private static final String LOG_TAG = "AndroidTelinkActivity";

    private TextView stateLabel;
    private RecyclerView recyclerView;

    private DefaultNetworkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_telink);

        stateLabel = (TextView) findViewById(R.id.state_label);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        setupAddButton();
        setupAddressButton();

        setConnecting(true);

        adapter = new DefaultNetworkAdapter();

        adapter.setClickListener(new DefaultNetworkAdapter.OnClickListener() {
            @Override
            public void onItemClick(DefaultNetworkAdapter.ViewHolder holder, int position, MyDevice device) {

                if (!device.isValid()) {
                    return;
                }

                Intent intent = new Intent(AndroidTelinkActivity.this, DeviceActivity.class);
                DeviceActivity.device = device;
                Log.i("DefaultNetwork ", "" + device.getMacData().length);
                AndroidTelinkActivity.this.startActivity(intent);
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
                MeshManager.getInstance().scanMeshDevices();
            }

            @Override
            public void didFailToLoginNode(MeshManager manager) {
                Log.i(LOG_TAG, "didFailToLoginNode ");

                MeshManager.getInstance().stopScanNode();
                AndroidTelinkActivity.this.finish();
            }

            @Override
            public void didGetMac(MeshManager manager, byte[] macBytes, int address) {

                MeshCommand cmd = MeshCommand.requestMacDeviceType(address);
                MeshManager.getInstance().send(cmd);

                Intent intent = new Intent("MeshManager.didGetMac");
                intent.putExtra("macBytes", macBytes);
                intent.putExtra("address", address);
                LocalBroadcastManager.getInstance(AndroidTelinkActivity.this).sendBroadcast(intent);
            }

            @Override
            public void didConfirmNewNetwork(MeshManager manager, Boolean isSuccess) {

            }


            @Override
            public void didDiscoverNode(MeshManager manager, MeshNode node) {

            }

            @Override
            public void didConnectNode(MeshManager manager, MeshNode node) {

            }

            @Override
            public void didDisconnectNode(MeshManager manager, Boolean isActiveDisConnected, MeshNode node, BluetoothGatt gatt) {

            }

            @Override
            public void didFailToConnectNode(MeshManager manager, MeshNode node) {

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

        MeshManager.getInstance().scanNode(network, true);
    }

    private void setupAddButton() {

        Button addButton = findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AndroidTelinkActivity.this, AddDeviceActivity.class);
                AddDeviceActivity.network = network;
                List<Integer> addressList = new ArrayList<>();
                List<MyDevice> deviceList = adapter.getDevices();
                for (MyDevice device : deviceList) {
                    addressList.add(device.getMeshDevice().getAddress());
                }
                AddDeviceActivity.existList = addressList;
                AndroidTelinkActivity.this.startActivity(intent);
            }
        });
    }

    private void setupAddressButton() {

        Button addressButton = findViewById(R.id.address_btn);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AndroidTelinkActivity.this, MeshAddressActivity.class);
                MeshAddressActivity.network = network;
                startActivity(intent);
            }
        });
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