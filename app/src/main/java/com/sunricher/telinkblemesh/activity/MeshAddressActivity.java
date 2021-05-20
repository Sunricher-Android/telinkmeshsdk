package com.sunricher.telinkblemesh.activity;

import android.os.Bundle;
import android.util.Log;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemesh.adapter.MeshAddressAdapter;
import com.sunricher.telinkblemeshlib.MeshNetwork;
import com.sunricher.telinkblemeshlib.db.MeshAddressManager;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MeshAddressActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MeshAddressActivity";
    public static MeshNetwork network = MeshNetwork.factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_address);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        MeshAddressAdapter adapter = new MeshAddressAdapter();
        List<Integer> existsList = MeshAddressManager.getInstance(this).getExistsAddressList(network);
        adapter.setExistAddressList(existsList);
        List<Integer> availableList = MeshAddressManager.getInstance(this).getAvailableAddressList(network);
        adapter.setAvailableAddressList(availableList);
        recyclerView.setAdapter(adapter);

        adapter.setClickListener(new MeshAddressAdapter.OnClickListener() {
            @Override
            public void onItemClick(MeshAddressAdapter.ViewHolder holder, int position, int address) {

                MeshAddressManager.getInstance(MeshAddressActivity.this).remove(address, network);
                adapter.removeAt(position);
                adapter.notifyDataSetChanged();
            }
        });

    }
}