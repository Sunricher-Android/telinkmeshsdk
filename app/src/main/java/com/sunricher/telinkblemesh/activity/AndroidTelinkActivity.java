package com.sunricher.telinkblemesh.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sunricher.telinkblemesh.R;

public class AndroidTelinkActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AndroidTelinkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_telink);

        setupAddButton();
        setupAddressButton();
    }

    private void setupAddButton() {

        Button addButton = findViewById(R.id.add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setupAddressButton() {

        Button addressButton = findViewById(R.id.address_btn);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AndroidTelinkActivity.this, MeshAddressActivity.class);
                startActivity(intent);
            }
        });
    }

}