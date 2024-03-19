package com.agrosense.app.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClient {
    private static final String TAG = "BluetoothClient";
    private static BluetoothClient instance;
    private OutputStream outputStream;

    public static BluetoothClient getInstance() {
        if (instance == null)
            instance = new BluetoothClient();
        return instance;
    }

    public void connect(BluetoothDevice bluetoothDevice, Context context) throws IOException {
        UUID uuid = UUID.randomUUID();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            }
            return;
        }

        try (BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)) {
            socket.connect();
            outputStream = socket.getOutputStream();
        }
    }

    public void sendRequest(String value) throws IOException {
        byte[] request = value.getBytes();
        Log.d(TAG, "sendRequest: Sending request = " + value);
        outputStream.write(request);
    }


}
