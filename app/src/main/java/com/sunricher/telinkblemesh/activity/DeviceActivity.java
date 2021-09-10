package com.sunricher.telinkblemesh.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.model.MyDevice;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MqttMessage;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class DeviceActivity extends AppCompatActivity {

    public static final String LOG_TAG = "DeviceActivity";

    public static MyDevice device;

    private ArrayList<Integer> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        TextView deviceTextView = (TextView) findViewById(R.id.device_tv);
        deviceTextView.setText(device.getTitle());

        setSettingsButton();
        setOnOffSwitch();
        setBrightnessSeekbar();
        setAllGroupsButton();
        setDeviceGroupsButton();
        setAddGroupButton();
        setDeleteGroupButton();

        MeshManager.getInstance().setDeviceCallback(new DeviceCallback() {
            @Override
            public void didGetGroups(MeshManager manager, int address, ArrayList<Integer> groups) {
                Log.i(LOG_TAG, "didGetGroups count " + groups.size());

                HashSet<Integer> temp = new HashSet<>(groupList);
                temp.addAll(groups);
                groupList = new ArrayList<Integer>(temp);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupList.sort(Comparator.naturalOrder());
                }
                Log.i(LOG_TAG, "new groups count " + groupList.size());
            }

            @Override
            public void didGetDeviceAddress(MeshManager manager, int address) {
                Log.i(LOG_TAG, "didGetDeviceAddress " + address);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        device = null;
    }

    private void setSettingsButton() {

        Button settingsButton = (Button) findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DeviceActivity.this, DeviceSettingsActivity.class);
                DeviceActivity.this.startActivity(intent);
            }
        });
    }

    private void setAllGroupsButton() {

        Button button = findViewById(R.id.groups_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                groupList.clear();

                MeshCommand command = MeshCommand.getGroups(MeshCommand.Address.all);
                MeshManager.getInstance().send(command);
            }
        });
    }

    private void setDeviceGroupsButton() {

        Button button = findViewById(R.id.device_groups_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MeshCommand command = MeshCommand.getGroups(device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(command);
            }
        });
    }

    private void setAddGroupButton() {

        Button button = findViewById(R.id.add_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MeshCommand command = MeshCommand.addGroup(0xFE, device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(command);
            }
        });
    }

    private void setDeleteGroupButton() {

        Button button = findViewById(R.id.delete_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MeshCommand command = MeshCommand.deleteGroup(0xFE, device.getMeshDevice().getAddress());
                MeshManager.getInstance().send(command);
            }
        });
    }

    private void setOnOffSwitch() {

        SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.onoff_switch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
                MeshCommand cmd = MeshCommand.turnOnOff(device.getMeshDevice().getAddress(), isOn, 0);
                MeshManager.getInstance().sendMqttMessage(MqttMessage.meshCommand(cmd, "wd"));
//                MeshManager.getInstance().send(cmd);
            }
        });

        boolean isShow = device.getDeviceType().getCapabilities().contains(MeshDeviceType.Capability.onOff);
        onOffSwitch.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        onOffSwitch.setChecked(device.getMeshDevice().getState() == MeshDevice.State.on);
    }

    private void setBrightnessSeekbar() {

        SeekBar brightnessSeekbar = (SeekBar) findViewById(R.id.brightness_seekbar);

        boolean isShow = device.getDeviceType().getCapabilities().contains(MeshDeviceType.Capability.brightness);
        brightnessSeekbar.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        brightnessSeekbar.setMax(100);
        brightnessSeekbar.setProgress(device.getMeshDevice().getBrightness());

        brightnessSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {

                if (fromUser) {

                    MeshCommand cmd = MeshCommand.setBrightness(device.getMeshDevice().getAddress(), value);
//                    MeshManager.getInstance().sendSample(cmd);
                    MeshManager.getInstance().sendMqttMessage(MqttMessage.meshCommand(cmd, "wd"), true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                MeshCommand cmd = MeshCommand.setBrightness(device.getMeshDevice().getAddress(), seekBar.getProgress());
//                MeshManager.getInstance().send(cmd);
                MeshManager.getInstance().sendMqttMessage(MqttMessage.meshCommand(cmd, "wd"), false);
            }
        });
    }
}