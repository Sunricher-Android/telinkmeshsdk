package com.sunricher.telinkblemesh.activity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.SmartSwitchManager;
import com.sunricher.telinkblemeshlib.callback.SmartSwitchDataCallback;
import com.sunricher.telinkblemeshlib.models.SmartSwitchMode;

import androidx.appcompat.app.AppCompatActivity;

public class SmartSwitchActivity extends AppCompatActivity {

    private TextView secretKeyTextView;
    private int groupId = 0x8003;
    private int mode = SmartSwitchMode.defaultMode;

    private DataCallbackHandler dataCallbackHandler;

    private int[] groupAddressArray = new int[]{0x9E, 0xA1};


    private boolean isNfcAvailable() {
        return NfcAdapter.getDefaultAdapter(this) != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_switch);

        dataCallbackHandler = new DataCallbackHandler();
        SmartSwitchManager.getInstance().setDataCallback(dataCallbackHandler);

        setUpGetSecretKeyButton();
        setUpReadConfigButton();
        setUpStartConfigButton();
        setUpUnbindConfigButton();
        setUpUpdateConfigButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SmartSwitchManager.getInstance().clear();
        dataCallbackHandler = null;
    }

    private void setUpGetSecretKeyButton() {

        secretKeyTextView = findViewById(R.id.secret_key_tv);

        Button btn = findViewById(R.id.get_secret_key_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SmartSwitchManager.getInstance().clear();

                MeshCommand cmd = MeshCommand.getSmartSwitchSecretKey(mode, groupId);
                MeshManager.getInstance().send(cmd);
            }
        });
    }

    private void setUpStartConfigButton() {

        Button btn = findViewById(R.id.start_config_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkNfcAvailable()) {
                    return;
                }

                goToNfcHandler(SmartSwitchManager.State.startConfig);
            }
        });
    }

    private void setUpUnbindConfigButton() {

        Button btn = findViewById(R.id.unbind_config_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkNfcAvailable()) {
                    return;
                }

                goToNfcHandler(SmartSwitchManager.State.unbindConfig);
            }
        });
    }

    private void setUpUpdateConfigButton() {

        Button btn = findViewById(R.id.update_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int address : groupAddressArray) {
                    MeshCommand cmd = MeshCommand.addSmartSwitchIdWithGroupId(address, groupId);
                    MeshManager.getInstance().send(cmd);
                }
            }
        });
    }

    private void setUpReadConfigButton() {

        Button btn = findViewById(R.id.read_config_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkNfcAvailable()) {
                    return;
                }

                goToNfcHandler(SmartSwitchManager.State.readConfig);
            }
        });
    }

    private void goToNfcHandler(int state) {

        Intent intent = new Intent(this, NfcHandlerActivity.class);
        intent.putExtra("state", state);
        intent.putExtra("mode", mode);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private boolean checkNfcAvailable() {

        boolean isAvailable = isNfcAvailable();
        if (!isAvailable) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
        }

        return isAvailable;
    }

    final class DataCallbackHandler extends SmartSwitchDataCallback {

        @Override
        public void smartSwitchManagerDidReceiveData(SmartSwitchManager manager, int progress) {

            String text = "" + progress + "%";
            secretKeyTextView.setText(text);
        }

        @Override
        public void smartSwitchManagerDidReceiveDataFailed(SmartSwitchManager manager) {

            secretKeyTextView.setText("Failed");
        }

        @Override
        public void smartSwitchManagerDidReceiveDataEnd(SmartSwitchManager manager) {

            secretKeyTextView.setText("Successful");
        }
    }

}