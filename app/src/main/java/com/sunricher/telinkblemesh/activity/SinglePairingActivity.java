package com.sunricher.telinkblemesh.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.adapter.AllDevicesAdapter;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshNode;
import com.sunricher.telinkblemeshlib.SinglePairingManager;

import java.util.ArrayList;
import java.util.List;

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
        adapter.setClickListener(new AllDevicesAdapter.OnClickListener() {
            @Override
            public void onItemClick(AllDevicesAdapter.ViewHolder holder, int position) {

                MeshNode node = nodes.get(position);
                if (node == null) return;
                statusLabel.setText("pairing...");
                SinglePairingManager.getInstance().startPairing(network, getApplication(), node);
            }
        });
        recyclerView.setAdapter(adapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nodes.clear();
                adapter.clear();
                SinglePairingManager.getInstance().startScanning();
            }
        });

        SinglePairingManager.getInstance().setCallback(new SinglePairingManager.Callback() {
            @Override
            public void didDiscoverNode(SinglePairingManager manager, MeshNode node) {

                if (nodes.contains(node)) {
                    return;
                }

                nodes.add(node);
                adapter.addNode(node);
            }

            @Override
            public void terminalWithUnsupportedNode(SinglePairingManager manager, MeshNode node) {
                statusLabel.setText("terminalWithUnsupportedNode");
            }

            @Override
            public void terminalWithNodeNoMoreNewAddresses(SinglePairingManager manager) {
                statusLabel.setText("terminalWithNodeNoMoreNewAddresses");
            }

            @Override
            public void didFailToLoginNode(SinglePairingManager manager) {
                statusLabel.setText("didFailToLoginNode");
            }

            @Override
            public void didFinishPairing(SinglePairingManager manager) {
                statusLabel.setText("didFinishPairing");
            }
        });

    }
}