package com.sunricher.telinkblemesh.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshNetwork;

public class AddDeviceActivity extends AppCompatActivity {

    public static MeshNetwork network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
    }
}