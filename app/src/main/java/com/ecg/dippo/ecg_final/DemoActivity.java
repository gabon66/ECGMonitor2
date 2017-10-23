package com.ecg.dippo.ecg_final;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;

import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;

import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class DemoActivity extends AppCompatActivity {
    BluetoothSPP bt;
    BluetoothLeService service;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);


        BluetoothConfiguration config = new BluetoothConfiguration();
        config.context = getApplicationContext();
        config.bluetoothServiceClass = BluetoothLeService.class; // BluetoothClassicService.class or BluetoothLeService.class
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "Your App Name";
        config.callListenersInMainThread = true;


// Bluetooth LE
        config.uuidService = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
        config.uuidCharacteristic = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");


        //config.uuidService = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
        //config.uuidCharacteristic = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");


                                                   //00002a00-0000-1000-8000-00805f9b34fb
        config.transport = BluetoothDevice.TRANSPORT_LE; // Only for dual-mode devices

        BluetoothLeService.init(config);

        //service = BluetoothLeService.getDefaultInstance();
        this.listenrs();

        service.startScan();
    }

    private void listenrs(){


        service.setOnScanCallback(new BluetoothLeService.OnBluetoothScanCallback() {
            @Override
            public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
                Log.e("devices ",device.getName());
                Log.e("device MAC ",device.getAddress().toString());

                if (device.getAddress().toString().equalsIgnoreCase("45:90:87:E2:30:64")){
                    service.connect(device);

                    service.stopScan();
                }
                //service.connect(device);

                //service.stopScan();
            }

            @Override
            public void onStartScan() {
                Log.i("arranco scan ","arranco scan");
            }

            @Override
            public void onStopScan() {
            }
        });


        service.setOnEventCallback(new BluetoothService.OnBluetoothEventCallback() {
            @Override
            public void onDataRead(byte[] bytes, int i) {

            }

            @Override
            public void onStatusChange(BluetoothStatus bluetoothStatus) {

            }

            @Override
            public void onDeviceName(String s) {

            }

            @Override
            public void onToast(String s) {

            }

            @Override
            public void onDataWrite(byte[] bytes) {

            }
        });

        service.setOnEventCallback(new BluetoothLeService.OnBluetoothEventCallback() {



            @Override
            public void onDataRead(byte[] buffer, int length) {
                Log.i("buffer data",buffer.toString());
            }

            @Override
            public void onStatusChange(BluetoothStatus status) {
                Log.e("status",status.toString());
            }

            @Override
            public void onDeviceName(String deviceName) {
            }

            @Override
            public void onToast(String message) {
            }

            @Override
            public void onDataWrite(byte[] buffer) {
                Log.e("data write","data write");
            }



        });
    }


}
