package com.sunricher.telinkblemesh.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.model.MyDevice;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshManagerDeviceCallback;
import com.sunricher.telinkblemeshlib.MeshManagerNodeCallback;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DeviceSettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DeviceSettingsActivity";

    private EditText addressEditText;
    private MyDevice device;
    private int pendingAddress;
    private byte[] macData;
    private Handler handler;
    private Runnable changeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        device = DeviceActivity.device;
        setBroadcastReceivers();
        handler = new Handler(Looper.getMainLooper());
        changeRunnable = new Runnable() {
            @Override
            public void run() {

                Log.i(LOG_TAG, "failed");
                Toast.makeText(DeviceSettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        };

        addressEditText = (EditText) findViewById(R.id.new_address_et);

        Button changeButton = (Button) findViewById(R.id.change_btn);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int newAddress = DeviceSettingsActivity.this.getNewAddress();
                DeviceSettingsActivity.this.pendingAddress = newAddress;

                handler.postDelayed(changeRunnable, 4000);

                MeshCommand cmd = MeshCommand.changeAddress(device.getMeshDevice().getAddress(), newAddress, device.getMacData());
                MeshManager.getInstance().send(cmd);
            }
        });

        Button resetButton = (Button) findViewById(R.id.reset_btn);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MeshCommand cmd = MeshCommand.resetNetwork(device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(cmd);

                Toast.makeText(DeviceSettingsActivity.this, "Reset OK, restart all devices after resetting the network.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getNewAddress() {

        String valueString = addressEditText.getText().toString();
        if (valueString.length() <= 0) {
            return 0;
        }
        return Integer.parseInt(valueString);
    }

    private void setBroadcastReceivers() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MeshManager.didGetMac");
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                byte[] macBytes = intent.getByteArrayExtra("macBytes");
                int address = intent.getIntExtra("address", 0);

                if (address == DeviceSettingsActivity.this.pendingAddress
                        && macBytes != null
                        && Arrays.equals(DeviceSettingsActivity.this.device.getMacData(), macBytes)) {

                    Log.i(LOG_TAG, "successfully");
                    Toast.makeText(DeviceSettingsActivity.this, "Successfully", Toast.LENGTH_SHORT).show();

                    DeviceSettingsActivity.this.handler.removeCallbacks(changeRunnable);

                    MeshCommand cmd = MeshCommand.requestMacDeviceType(pendingAddress);
                    MeshManager.getInstance().send(cmd);
                }

            }
        }, intentFilter);
    }

}