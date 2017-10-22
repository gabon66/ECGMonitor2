package com.ecg.dippo.ecg_final;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import rx.Subscription;

public class Demo1Activity extends AppCompatActivity {
    String macAddress = "C8:FD:19:4D:F8:41";
    String macAddressPulsi = "34:B1:F7:CD:4B:2E";


    public static String UUID_MYSERVICE = "00001800-0000-1000-8000-00805f9b34fb";
    public static String UUID_MYCHART= "00002a24-0000-1000-8000-00805f9b34fb";

    Button btnConectar;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
        private final static String TAG = Demo1Activity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(macAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo1);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter!=null){
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);



            btnConectar=(Button)findViewById(R.id.button);
            btnConectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // inicio conexion.
                    //mBluetoothLeService.connect(macAddress);
                    mBluetoothLeService.connect(macAddressPulsi);

                }
            });
        }
    }


    private void getServices(){
        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

        //Log.e("onServicesDiscovered", "Services count: "+gattServices.size());


        // recorro todos los servicios
        for (BluetoothGattService gattService : gattServices) {
            String serviceUUID = gattService.getUuid().toString();
          //  Log.e("onServicesDiscovered", "Service uuid "+serviceUUID);


            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
                // recorros todas las charasteristics
                for (BluetoothGattCharacteristic caracts: gattCharacteristics) {
                    String uuid = caracts.getUuid().toString();

                    if (caracts.getProperties()==BluetoothGattCharacteristic.PROPERTY_READ)
                    {
                        // SOLO LAS DE LECTURA
                        Log.e("caracts de lectura: ","Caracteristicas:" +uuid);

                        if (uuid.equalsIgnoreCase(this.UUID_MYCHART)){
                            Log.e("entro caracast","entroo");
                            //mBluetoothLeService.readCharacteristic(caracts);
                        }
                    }

                    if (caracts.getProperties()==BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                    {
                        // SOLO LAS DE LECTURA
                        Log.e("caracts de notifi: ","Caracteristicas:" +uuid);
                    }


                    if (caracts.getProperties()==BluetoothGattCharacteristic.PROPERTY_BROADCAST)
                    {
                        // SOLO LAS DE LECTURA
                        Log.e("caracts de broad: ","Caracteristicas:" +uuid);
                    }

                    if (caracts.getProperties()==BluetoothGattCharacteristic.PROPERTY_INDICATE)
                    {
                        // SOLO LAS DE LECTURA
                        Log.e("caracts de indicate: ","Caracteristicas:" +uuid);
                        mBluetoothLeService.readCharacteristic(caracts);
                    }


                }
        }

        //10-21 16:17:53.165 9220-9220/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 0000ff70-0000-1000-8000-00805f9b34fb
        //10-21 16:17:53.165 9220-9220/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 00001800-0000-1000-8000-00805f9b34fb
        //10-21 16:17:53.166 9220-9220/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 00001801-0000-1000-8000-00805f9b34fb
        //10-21 16:17:53.167 9220-9220/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 0000180a-0000-1000-8000-00805f9b34fb


        //10-21 20:21:39.137 899-899/com.ecg.dippo.ecg_final E/onServicesDiscovered: Services count: 4
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 0000ff70-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:0000ff71-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 00001800-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a00-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a01-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a02-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a03-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a04-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.138 899-899/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 00001801-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a05-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/onServicesDiscovered: Service uuid 0000180a-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a23-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a24-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a25-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a26-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a27-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a28-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a29-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a2a-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a50-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a30-0000-1000-8000-00805f9b34fb
        //10-21 20:21:39.139 899-899/com.ecg.dippo.ecg_final E/caracts:: Caracteristicas:00002a31-0000-1000-8000-00805f9b34fb



    }







    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                Toast.makeText(context, "Se conecto", Toast.LENGTH_SHORT).show();
                getServices();



            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                ///updateConnectionState(R.string.disconnected);
                Toast.makeText(context, "Se desconecto", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                mBluetoothLeService.getSupportedGattServices();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {


                Log.e("data BT",intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }



    };




    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            //final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
