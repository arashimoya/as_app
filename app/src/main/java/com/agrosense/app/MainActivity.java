package com.agrosense.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.agrosense.app.dsl.db.AgroSenseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AgroSenseDatabase.Companion.getDatabase(this);
        startActivity(new Intent(this, BluetoothActivity.class));
    }
}