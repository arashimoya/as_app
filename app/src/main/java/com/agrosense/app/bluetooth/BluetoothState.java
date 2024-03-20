package com.agrosense.app.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;


public class BluetoothState {
    private static final String TAG = "BluetoothConnection";
    BluetoothAdapter adapter;

    public BluetoothState() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void enableBluetooth(Context context, BroadcastReceiver toggleBluetoothReceiver) {
        if (adapter!= null && !adapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "toggleBluetooth: PERMISSION DENIED.");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }
            Log.d(TAG, "toggleBluetooth: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(context, toggleBluetoothReceiver, BTIntent);
        }
    }

    public void disableBluetooth(Context context, BroadcastReceiver receiver){
        if (adapter != null && adapter.isEnabled()) {
            Log.d(TAG, "toggleBluetooth: checking permission to disable BT.");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "toggleBluetooth: PERMISSION DENIED.");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    return;
                }
            }

            Log.d(TAG, "disableBluetooth: disabling BT.");
            adapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(context, receiver, BTIntent);
        }
        else {
            Log.d(TAG, "disableBluetooth: BT already disabled.");
        }
    }

    private void registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter intentFilter) {
        context.registerReceiver(receiver, intentFilter);
    }

    public void toggleDiscover(Activity activity, BroadcastReceiver discoverReceiver) {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "toggleDiscover: PERMISSION DENIED.");
                ActivityCompat.requestPermissions((Activity) activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
        }
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "toggleDiscover: PERMISSION DENIED.");
            ActivityCompat.requestPermissions((Activity) activity, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            return;
        }
        Log.d(TAG, "toggleDiscover: Looking for unpaired devices.");


        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
            Log.d(TAG, "toggleDiscover: Canceling discovery.");

            checkBTPermissions(activity);
            adapter.startDiscovery();
            activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
        if (!adapter.isDiscovering()) {
            checkBTPermissions(activity);
            adapter.startDiscovery();
            activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    private void checkBTPermissions(Activity activity) {
        int permissionCheck = activity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += activity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }
}
