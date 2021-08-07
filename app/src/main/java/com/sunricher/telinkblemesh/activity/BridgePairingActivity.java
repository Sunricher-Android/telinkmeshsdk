package com.sunricher.telinkblemesh.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.BridgePairingManager;
import com.sunricher.telinkblemeshlib.MeshDeviceType;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshNode;

public class BridgePairingActivity extends AppCompatActivity {

    public static MeshNetwork network;

    private TextView stateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bridge_pairing);

        stateTextView = findViewById(R.id.state_tv);

        Button startButton = findViewById(R.id.start_btn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stateTextView.setText("pairing...");

                BridgePairingManager.getInstance().setCallback(new BridgePairingManager.Callback() {
                    @Override
                    public void terminalWithNoMoreNewAddresses(BridgePairingManager manager) {
                        stateTextView.setText("no more new addresses");
                    }

                    @Override
                    public void failToConnect(BridgePairingManager manager) {
                        stateTextView.setText("fail to connect");
                    }

                    @Override
                    public void terminalWithNoBridgeFound(BridgePairingManager manager) {
                        stateTextView.setText("no bridge found");
                    }

                    @Override
                    public void didFinish(BridgePairingManager manager) {
                        stateTextView.setText("finish");
                    }

                    @Override
                    public void terminalWithUnsupportedDevice(BridgePairingManager manager, int address, MeshDeviceType deviceType, byte[] macData) {
                        stateTextView.setText("unsupported device " + address);
                    }
                });
                BridgePairingManager.getInstance().startPairing(network, BridgePairingActivity.this.getApplication());
            }
        });
    }
}