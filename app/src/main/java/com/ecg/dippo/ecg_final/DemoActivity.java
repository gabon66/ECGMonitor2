package com.ecg.dippo.ecg_final;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class DemoActivity extends AppCompatActivity {
    BluetoothSPP bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        bt = new BluetoothSPP(getApplicationContext());

        if(bt.isBluetoothAvailable()) {

            // any command for bluetooth is not available
            //bt.startService(BluetoothState.DEVICE_ANDROID);
            bt.startService(BluetoothState.DEVICE_OTHER);



            bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    // Do something when successfully connected
                    Toast.makeText(DemoActivity.this, "Se conecto", Toast.LENGTH_SHORT).show();
                }

                public void onDeviceDisconnected() {
                    // Do something when connection was disconnected
                }

                public void onDeviceConnectionFailed() {
                    // Do something when connection failed
                    Toast.makeText(DemoActivity.this, "Fallo", Toast.LENGTH_SHORT).show();
                }
            });

            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                @Override
                public void onDataReceived(byte[] data, String message) {
                    Log.e("data",message);
                }
            });

            bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
                public void onNewConnection(String name, String address) {
                    // Do something when earching for new connection device
                }

                public void onAutoConnectionStarted() {
                    // Do something when auto connection has started
                }
            });

            bt.startDiscovery();
            try {
                //bt.connect("C8:FD:19:4D:F8:41");
            }catch (Exception e){
                Log.e("Conectar Error",e.toString());
            }

        }
    }
}
