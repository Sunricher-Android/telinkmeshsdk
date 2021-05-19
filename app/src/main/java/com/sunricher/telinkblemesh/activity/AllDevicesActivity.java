package com.sunricher.telinkblemesh.activity;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.adapter.AllDevicesAdapter;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.MeshNode;
import com.sunricher.telinkblemeshlib.callback.NodeCallback;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class AllDevicesActivity extends AppCompatActivity {

    private ArrayList<MeshNode> nodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_devices);

        nodes = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        AllDevicesAdapter adapter = new AllDevicesAdapter();
        adapter.setClickListener(new AllDevicesAdapter.OnClickListener() {
            @Override
            public void onItemClick(AllDevicesAdapter.ViewHolder holder, int position) {
                Log.i("ALL ", "click " + position);
            }
        });
        recyclerView.setAdapter(adapter);

        MeshManager.getInstance().setNodeCallback(new NodeCallback() {
            @Override
            public void didDiscoverNode(MeshManager manager, MeshNode node) {

                if (nodes.contains(node)) {
                    return;
                }

                nodes.add(node);
                adapter.addNode(node);
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

            @Override
            public void didLoginNode(MeshManager manager, MeshNode node) {

            }

            @Override
            public void didFailToLoginNode(MeshManager manager) {

            }

            @Override
            public void didGetMac(MeshManager manager, byte[] macBytes, int address) {

            }

            @Override
            public void didConfirmNewNetwork(MeshManager manager, Boolean isSuccess) {

            }
        });
        MeshManager.getInstance().scanNode(MeshNetwork.factory, false, true);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (refreshLayout.isRefreshing()) {

                    AllDevicesActivity.this.reloadNodes(adapter);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MeshManager.getInstance().stopScanNode();
    }

    private void reloadNodes(AllDevicesAdapter adapter) {

        nodes.clear();
        adapter.clear();
        MeshManager.getInstance().scanNode(MeshNetwork.factory, false, true);
    }

}