package com.sunricher.telinkblemesh.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshDevice;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshPairingManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

public class AddDeviceActivity extends AppCompatActivity {

    public static MeshNetwork network;
    public static List<Integer> existList = new ArrayList<>();

    private Set<Integer> newDeviceSet = new HashSet<>();

    private Button startButton;
    private TextView statusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        startButton = findViewById(R.id.start_btn);
        statusLabel = findViewById(R.id.status_label);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newDeviceSet.clear();
                statusLabel.setText("Pairing...");
                MeshPairingManager.getInstance().startPairing(network, AddDeviceActivity.this.getApplication(), new MeshPairingManager.Callback() {

                    @Override
                    public void pairingFailed(MeshPairingManager manager, MeshPairingManager.FailedReason reason) {
                        statusLabel.setText("Pairing failed " + reason);
                    }

                    @Override
                    public void didAddNewDevices(MeshPairingManager manager, ArrayList<MeshDevice> meshDevices) {
                        for (MeshDevice device : meshDevices) {
                            if (existList.contains(device.getAddress())) {
                                continue;
                            }
                            newDeviceSet.add(device.getAddress());
                        }
                        statusLabel.setText("Did add new devices " + newDeviceSet.size());
                    }

                    @Override
                    public void didUpdateProgress(MeshPairingManager manager, double progress) {
                        statusLabel.setText("didUpdateProgress " + progress);
                    }

                    @Override
                    public void didFinishPairing(MeshPairingManager manager) {
                        statusLabel.setText("finish pairing, add devices " + newDeviceSet.size());
                    }

                    @Override
                    public void terminalWithUnsupportedDevice(MeshPairingManager manager, int address, MeshDeviceType deviceType, byte[] bytes) {
                        statusLabel.setText("terminalWithUnsupportedDevice " + address);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MeshManager.getInstance().disconnect(false);
    }

}