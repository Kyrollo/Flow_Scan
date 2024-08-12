package com.FlowScan.menuPages;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.FlowScan.DataBase.AppDatabase;
import com.FlowScan.R;
import com.FlowScan.Table.Data;

import java.util.Objects;

public class DataDisplayActivity extends AppCompatActivity {
    private AppDatabase db;
    private TextView dataShown, showBarCode;
    BroadcastReceiver myBroadcastReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getDatabase(this);
        showBarCode = findViewById(R.id.showBarCode);
        dataShown = findViewById(R.id.dataShown);
        Button btnInquiry = findViewById(R.id.btnInquiry);

        myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Objects.equals(action, getResources().getString(R.string.activity_intent_filter_action))) {
                    String scannedData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_data_String));
                    showBarCode.setText(scannedData);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);

        btnInquiry.setOnClickListener(v -> {
            String barcode = showBarCode.getText().toString().trim();
            if (!barcode.isEmpty()) {
                inquireData(barcode);
            } else {
                showBarCode.setText(getString(R.string.waiting_for_scan));
            }
        });

        // Enable the back arrow
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void inquireData(String barcode) {
        Data data = db.dataDao().getDataByBarcode(barcode);
        if (data != null) {
            String displayText = getString(R.string.data_desc) + data.getDescription() + "\n" +
                    getString(R.string.data_cat) + data.getCategory() + "\n" +
                    getString(R.string.data_stat) + data.getStatus() + "\n" +
                    getString(R.string.data_loc) + data.getLocation();
            dataShown.setText(displayText);
        } else {
            dataShown.setText("");
            Toast.makeText(this, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
        }
    }
}