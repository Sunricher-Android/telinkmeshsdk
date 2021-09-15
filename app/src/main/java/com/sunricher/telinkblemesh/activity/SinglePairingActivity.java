package com.sunricher.telinkblemesh.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.adapter.AllDevicesAdapter;
import com.sunricher.telinkblemeshlib.AccessoryPairingManager;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshNode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SinglePairingActivity extends AppCompatActivity {

    public static MeshNetwork network;

    private List<MeshNode> nodes = new ArrayList<>();

    private Button startButton;
    private TextView statusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_pairing);

        startButton = findViewById(R.id.start_btn);
        statusLabel = findViewById(R.id.status_tv);

        RecyclerView recyclerView = findViewById(R.id.nodes_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        AllDevicesAdapter adapter = new AllDevicesAdapter();
        recyclerView.setAdapter(adapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nodes.clear();
                adapter.clear();
                AccessoryPairingManager.getInstance().startPairing(network, getApplication());
            }
        });

        AccessoryPairingManager.getInstance().setCallback(new AccessoryPairingManager.Callback() {
            @Override
            public void terminalWithNoMoreNewAddresses(AccessoryPairingManager manager) {
                Toast.makeText(SinglePairingActivity.this, "No more addresses", Toast.LENGTH_LONG).show();
            }

            @Override
            public void didAddNode(AccessoryPairingManager manager, MeshNode node, int newAddress) {

                Log.i("AutoPairing","did add node");

                if (nodes.contains(node)) {
                    return;
                }

                nodes.add(node);
                adapter.addNode(node);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        AccessoryPairingManager.getInstance().stop();
    }
}