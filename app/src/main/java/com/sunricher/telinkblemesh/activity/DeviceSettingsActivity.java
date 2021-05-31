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
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

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

        getLightOnOffDuration();

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

        Button syncDatetimeButton = findViewById(R.id.sync_datetime_btn);
        syncDatetimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeshCommand cmd = MeshCommand.syncDatetime(device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(cmd);
            }
        });

        Button getDatetimeButton = findViewById(R.id.get_datetime_btn);
        getDatetimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeshManager.getInstance().setDeviceCallback(new DeviceCallback() {
                    @Override
                    public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {

                    }

                    @Override
                    public void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData) {

                    }

                    @Override
                    public void didGetDate(MeshManager manager, int address, Date date) {

                        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String dateString = dft.format(date);

                        Toast.makeText(DeviceSettingsActivity.this, "Datetime: " + dateString, Toast.LENGTH_LONG).show();
                    }
                });
                MeshCommand cmd = MeshCommand.getDatetime(device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(cmd);
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

    private void setLightOnOffDuration() {

        MeshCommand cmd = MeshCommand.setLightOnOffDuration(device.getMeshDevice().getAddress(), 2);
        MeshManager.getInstance().send(cmd);
    }

    private void getLightOnOffDuration() {

        MeshCommand cmd = MeshCommand.getLightOnOffDuration(device.getMeshDevice().getAddress());
        MeshManager.getInstance().setDeviceCallback(new DeviceCallback() {
            @Override
            public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {

            }

            @Override
            public void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData) {

            }

            @Override
            public void didGetLightOnOffDuration(MeshManager manager, int address, int duration) {

                Toast.makeText(DeviceSettingsActivity.this, "duration " + duration, Toast.LENGTH_LONG).show();
            }
        });
        MeshManager.getInstance().send(cmd);
    }

}