package com.sunricher.telinkblemesh.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.model.MyDevice;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshOtaFile;
import com.sunricher.telinkblemeshlib.MeshOtaManager;
import com.sunricher.telinkblemeshlib.callback.DeviceCallback;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class OtaActivity extends AppCompatActivity {

    public static MeshNetwork network = MeshNetwork.factory;
    private MyDevice device;
    private TextView versionTextView;
    private TextView stateTextView;

    private MeshOtaFile otaFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);

        device = DeviceActivity.device;
        versionTextView = findViewById(R.id.version_tv);
        versionTextView.setText(null);

        MeshCommand cmd = MeshCommand.getFirmwareVersion(device.getMeshDevice().getAddress());
        MeshManager.getInstance().setDeviceCallback(makeDeviceCallback());
        MeshManager.getInstance().send(cmd);

        TextView latestTextView = findViewById(R.id.latest_tv);
        latestTextView.setText(null);
        otaFile = MeshOtaManager.getInstance().getLatestOtaFile(device.getDeviceType());
        if (otaFile != null) {
            latestTextView.setText(otaFile.getVersion());
        }

        stateTextView = findViewById(R.id.state_tv);
        stateTextView.setText("Stopped");

        setUpStartOtaButton();
        setUpStopButton();
    }

    private DeviceCallback makeDeviceCallback() {

        return new DeviceCallback() {

            @Override
            public void didUpdateMeshDevices(MeshManager manager, ArrayList<MeshDevice> meshDevices) {

            }

            @Override
            public void didUpdateDeviceType(MeshManager manager, int deviceAddress, MeshDeviceType deviceType, byte[] macData) {

            }

            @Override
            public void didGetFirmwareVersion(MeshManager manager, int address, String version) {

                if (address != device.getMeshDevice().getAddress()) {
                    return;
                }

                versionTextView.setText(version);
            }
        };
    }

    private void setUpStartOtaButton() {

        MeshOtaManager.getInstance().setCallback(makeOtaCallback());

        Button startOtaButton = findViewById(R.id.start_ota_btn);
        startOtaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current = versionTextView.getText().toString();
                if (current == null) {
                    stateTextView.setText("Current version is null");
                    return;
                }
                if (otaFile == null) {
                    stateTextView.setText("Ota file is null");
                    return;
                }
                if (!otaFile.isNeedUpdate(current)) {
                    stateTextView.setText("No updates");
                    return;
                }

                stateTextView.setText("Connecting...");
                MeshOtaManager.getInstance().startOta(device.getMeshDevice().getAddress(), network, otaFile, OtaActivity.this);
            }
        });
    }

    private MeshOtaManager.Callback makeOtaCallback() {

        return new MeshOtaManager.Callback() {
            @Override
            public void didUpdateFailed(MeshOtaManager manager, MeshOtaManager.FailedReason reason) {

                stateTextView.setText("OTA failed");
            }

            @Override
            public void didUpdateProgress(MeshOtaManager manager, int progress) {

                stateTextView.setText("Progress " + progress);
            }

            @Override
            public void didUpdateComplete() {

                stateTextView.setText("Complete");
            }
        };
    }

    private void setUpStopButton() {

        Button stopButton = findViewById(R.id.stop_btn);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stateTextView.setText("Stopped");
                MeshOtaManager.getInstance().stopOta();
            }
        });
    }
}