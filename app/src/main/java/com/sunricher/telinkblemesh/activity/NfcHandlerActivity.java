package com.sunricher.telinkblemesh.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sunricher.telinkblemesh.R;
import com.sunricher.telinkblemeshlib.MeshCommand;
import com.sunricher.telinkblemeshlib.MeshManager;
import com.sunricher.telinkblemeshlib.SmartSwitchManager;
import com.sunricher.telinkblemeshlib.callback.SmartSwitchNfcCallback;
import com.sunricher.telinkblemeshlib.models.SmartSwitchMode;

public class NfcHandlerActivity extends Activity {

    private static final String TAG = NfcHandlerActivity.class.getSimpleName();

    private int state = SmartSwitchManager.State.readConfig;
    private int mode = SmartSwitchMode.defaultMode;
    private int groupId;
    private int[] addressArray = new int[]{0xA1, 0x95, 0x9E, 0x9C};

    private TextView titleTextView;
    private TextView detailTextView;

    private NfcAdapter adapter;
    private PendingIntent pendingIntent;

    private NfcCallbackHandler nfcCallbackHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_handler);

        titleTextView = findViewById(R.id.title_tv);
        titleTextView.setText("Ready to Scan");
        detailTextView = findViewById(R.id.detail_tv);
        detailTextView.setText("Connecting...");

        Intent intent = getIntent();
        state = intent.getIntExtra("state", SmartSwitchManager.State.readConfig);
        mode = intent.getIntExtra("mode", SmartSwitchMode.defaultMode);
        groupId = intent.getIntExtra("groupId", 0);

        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            dismissAfterSeconds(1);
            return;
        }
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcCallbackHandler = new NfcCallbackHandler();
        SmartSwitchManager.getInstance().setNfcCallback(nfcCallbackHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter == null) {
            return;
        }

        try {
            adapter.enableForegroundDispatch(this, pendingIntent, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (adapter == null) {
            return;
        }

        adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        nfcCallbackHandler = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            Log.e(TAG, "Tag is null, connect failed.");
            detailTextView.setText("Connect failed.");
            return;
        }

        switch (state) {
            case SmartSwitchManager.State.readConfig:
                SmartSwitchManager.getInstance().readConfiguration(tag);
                break;

            case SmartSwitchManager.State.startConfig:
                SmartSwitchManager.getInstance().startConfiguration(tag, mode);
                break;

            case SmartSwitchManager.State.unbindConfig:
                SmartSwitchManager.getInstance().unbindConfiguration(tag);
                break;

            default:
                break;
        }

    }

    final class NfcCallbackHandler extends SmartSwitchNfcCallback {

        @Override
        public void smartSwitchManagerDidConfigureSuccessful(SmartSwitchManager manager) {

            detailTextView.setText("Configure successful!");

            // Add smart switch id for every address in this group.
            for (int address : addressArray) {
                MeshCommand cmd = MeshCommand.addSmartSwitchIdWithGroupId(address, groupId);
                MeshManager.getInstance().send(cmd);
            }

            dismissAfterSeconds(2);
        }

        @Override
        public void smartSwitchManagerDidReadConfiguration(SmartSwitchManager manager, boolean isConfigured, int mode) {

            detailTextView.setText("Read successful! " + (isConfigured ? "Configured" : "No configured") + ", mode " + mode);
            dismissAfterSeconds(2);
        }

        @Override
        public void smartSwitchManagerDidUnbindConfigurationSuccessful(SmartSwitchManager manager) {

            detailTextView.setText("Unbind successful!");
            dismissAfterSeconds(2);
        }

        @Override
        public void smartSwitchManagerNfcReadWriteFailed(SmartSwitchManager manager, int state) {

            detailTextView.setText("ReadWrite failed, " + getStateString(state));
            dismissAfterSeconds(2);
        }

        private String getStateString(int state) {

            String stateString = "Unknown state";
            switch (state) {
                case SmartSwitchManager.State.readConfig:
                    stateString = "Read";
                    break;
                case SmartSwitchManager.State.startConfig:
                    stateString = "Configure";
                    break;
                case SmartSwitchManager.State.unbindConfig:
                    stateString = "Unbind";
                    break;
            }
            return stateString;
        }

    }

    private void dismissAfterSeconds(int seconds) {

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                finish();
            }
        }, seconds * 1000L);
    }
}