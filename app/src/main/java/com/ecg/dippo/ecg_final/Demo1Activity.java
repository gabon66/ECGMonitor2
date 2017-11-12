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
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecg.dippo.ecg_final.data.DataManager;
import com.ihealth.communication.control.Po3Control;
import com.ihealth.communication.control.PoProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint;
import com.scichart.charting.visuals.annotations.TextAnnotation;
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.data.model.DoubleRange;
import com.scichart.data.model.ISciList;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rx.Subscription;

public class Demo1Activity extends AppCompatActivity {
    //String macAddress = "6A:D9:EF:AD:83:28";

    String macAddress = "C8:FD:19:4D:F8:41";

    //String macAddress = "42:AC:0B:83:EB:B0";

    String macPusli="34B1F7CD4B2E";


    IXyDataSeries<Double, Double> series0 ;
    IXyDataSeries<Double, Double> series1 ;
    private final static long TIME_INTERVAL = 20;

    private double[] sourceData;

    private int _currentIndex=0;
    private int _totalIndex=0;

    private chartEcg.TraceAOrB whichTrace = chartEcg.TraceAOrB.TraceA;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> schedule;

    private volatile boolean isRunning = true;


    private final Handler mHandlerGraph = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;

    private static final int HANDLER_SCAN = 101;
    private static final int HANDLER_CONNECTED = 102;
    private static final int HANDLER_DISCONNECT = 103;
    private static final int HANDLER_USER_STATUE = 104;

    private TextView lblSPO2 ,lblpR;
    private LinearLayout dataSO02 , dataPR;

    private TextView tv_return;
    private String deviceMac;
    private int clientId;
    private int callbackId;
    private Po3Control mPo3Control;
    Button btnreload;
    Button btnStart;

    EditText txtMac;
    Boolean scanResult=false;

    //public static String UUID_MYSERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String UUID_MYSERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public static String UUID_MYCHART= "0000FFE1-0000-1000-8000-00805FPB34FB";

    Button btnConectar ,btnOxi;
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

    private int packages=0;

    private int vawe;
    GraphView graph;

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


        final SciChartSurface surface = new SciChartSurface(this);
        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartecg);
        chartLayout.addView(surface);

        SciChartBuilder.init(this);


        // Obtain the SciChartBuilder instance
        final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        sourceData = DataManager.getInstance().loadWaveformData(getApplicationContext());



        series0 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(3850).build();
        series1 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(3850).build();

        final IAxis xBottomAxis = sciChartBuilder.newNumericAxis()
                //.withVisibleRange(new DoubleRange(0d, 10d))
                .withVisibility(1)
                .withAutoRangeMode(AutoRange.Never)
                //.withAxisTitle("Time (seconds)")
                .build();

        final IAxis yRightAxis = sciChartBuilder.newNumericAxis()
                .withVisibleRange(new DoubleRange(-0.5d, 1.5d))
                .withAxisTitle("Voltaje (mV)")
                .build();

        final IRenderableSeries rs1 = sciChartBuilder.newLineSeries()
                .withDataSeries(series0)
                .build();

        final IRenderableSeries rs2 = sciChartBuilder.newLineSeries()
                .withDataSeries(series1)
                .build();

        Collections.addAll(surface.getXAxes(), xBottomAxis);
        Collections.addAll(surface.getYAxes(), yRightAxis);
        Collections.addAll(surface.getRenderableSeries(), rs1, rs2);




        final ISciList<Double> xValues0 = series0.getXValues();
        final ISciList<Double> yValues0 = series0.getYValues();
        final ISciList<Double> xValues1 = series1.getXValues();
        final ISciList<Double> yValues1 = series1.getYValues();
        series0.append(xValues0, yValues0);
        series1.append(xValues1, yValues1);

        schedule = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                UpdateSuspender.using(surface, appendDataRunnable);
            }
        }, 0, TIME_INTERVAL, TimeUnit.MILLISECONDS);








        lblpR=(TextView)findViewById(R.id.txtPR);
        lblSPO2=(TextView)findViewById(R.id.txtSPO2);
        dataPR=(LinearLayout)findViewById(R.id.dataPr);
        dataSO02=(LinearLayout)findViewById(R.id.dataSPO2);


        graph = (GraphView) findViewById(R.id.graph);
        mSeries2 = new LineGraphSeries<>();
        graph.addSeries(mSeries2);


        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getLegendRenderer().setVisible(false);

        graph.addSeries(mSeries2);

        if (mBluetoothAdapter!=null){
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


            btnConectar=(Button)findViewById(R.id.button);

            btnOxi=(Button)findViewById(R.id.btnOxi);

            btnOxi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanPulsi();
                }
            });

            btnConectar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // inicio conexion.
                    //mBluetoothLeService.connect(macAddress);
                    mBluetoothLeService.connect(macAddress);
                }
            });
        }



        iHealthDevicesManager.getInstance().init(Demo1Activity.this);
        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        iHealthDevicesManager.getInstance().sdkUserInAuthor(Demo1Activity.this, "gabriel.adrian.felipe@gmail.com", "da1266ba3e6d479482203c03db61d6cf", "907210256bb54741b66e05c98b61be8f", callbackId);

        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(callbackId,
                iHealthDevicesManager.TYPE_PO3);

    }




    private void getServices(){
        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

        // recorro todos los servicios
        for (BluetoothGattService gattService : gattServices) {
            String serviceUUID = gattService.getUuid().toString();
            Log.e("onServicesDiscovered", "Service uuid "+serviceUUID);


            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            if (serviceUUID.equalsIgnoreCase(UUID_MYSERVICE)){
                for (BluetoothGattCharacteristic caracts: gattCharacteristics) {
                        String uuid = caracts.getUuid().toString();
                            Log.e("my uuidddd", String.valueOf(caracts.getProperties()));
                            Log.e("caracts ", caracts.getUuid().toString());
                            mBluetoothLeService.setCharacteristicNotification(caracts,true);
                    }
            }
        }
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


                //Log.e("data  Bluetooth",intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

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

        mTimer2 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
                mHandlerGraph.postDelayed(this, 200);
            }
        };
       // mHandlerGraph.postDelayed(mTimer2, 1000);
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

    private void pulsinit(String mac) {
        boolean req = iHealthDevicesManager.getInstance().connectDevice("gabriel.adrian.felipe@gmail.com", mac, "PO3");

        if (req) {


        }
    }



    private void scanPulsi(){
        long type = iHealthDevicesManager.DISCOVERY_PO3;
        Log.d(TAG, "Start scan pulsi ");
        iHealthDevicesManager.getInstance().startDiscovery(type);
    }


    iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactorData) {

            Log.i(TAG, "onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            msg.what = HANDLER_SCAN;
            msg.setData(bundle);
            myHandler.sendMessage(msg);

            if (macPusli.equalsIgnoreCase(mac)){
                Log.e("pulsi ","encontro pulsi");
                pulsinit(mac);
            }
        }


        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactorData) {
            Log.e(TAG, "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorid:" + errorID + " -manufactorData:" + manufactorData);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();

            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                msg.what = HANDLER_CONNECTED;
                Toast.makeText(Demo1Activity.this, "se conecto", Toast.LENGTH_SHORT).show();
                getPulsiData(mac);

            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                msg.what = HANDLER_DISCONNECT;
            }
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
          /*  Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userstatus", userStatus + "");
            Message msg = new Message();
            msg.what = HANDLER_USER_STATUE;
            msg.setData(bundle);
            myHandler.sendMessage(msg);*/
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.d(TAG, "mac:" + mac + "--type:" + deviceType + "--action:" + action + "--message:" + message);
            JSONTokener jsonTokener = new JSONTokener(message);
            switch (action) {
                case PoProfile.ACTION_OFFLINEDATA_PO:
                    try {
                        JSONObject object = (JSONObject) jsonTokener.nextValue();
                        JSONArray jsonArray = object.getJSONArray(PoProfile.OFFLINEDATA_PO);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            String dataId=jsonObject.getString(PoProfile.DATAID);
                            String dateString = jsonObject.getString(PoProfile.MEASURE_DATE_PO);
                            int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                            int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                            JSONArray jsonArray1 = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                            int[] wave = new int[jsonArray1.length()];
                            for (int j = 0; j < jsonArray1.length(); j++) {
                                wave[j] = jsonArray1.getInt(j);
                            }

                            dataPR.setVisibility(View.VISIBLE);
                            dataSO02.setVisibility(View.VISIBLE);

                            lblSPO2.setText(oxygen+" %");
                            lblpR.setText(pulseRate+" bpm");

                            Log.i(TAG,"dataId:"+dataId+ "--date:" + dateString + "--oxygen:" + oxygen + "--pulseRate:" + pulseRate
                                    + "-wave1:"
                                    + wave[0]
                                    + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_LIVEDA_PO:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                        int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                        float PI = (float) jsonObject.getDouble(PoProfile.PI_PO);
                        JSONArray jsonArray = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                        int[] wave = new int[3];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            wave[i] = jsonArray.getInt(i);
                        }

                        dataPR.setVisibility(View.VISIBLE);
                        dataSO02.setVisibility(View.VISIBLE);


                        lblSPO2.setText(oxygen+" %");
                        lblpR.setText(pulseRate+" bpm");
                        Log.i(TAG, "oxygen:" + oxygen + "--pulseRate:" + pulseRate + "--Pi:" + PI + "-wave1:" + wave[0]
                                + "-wave2:" + wave[1] + "--wave3:" + wave[2]);


                        packages++;
                        graph.setVisibility(View.VISIBLE);

                        //lblpR.setText(String.valueOf(packages));
                        graph2LastXValue += 1d;
                        mSeries2.appendData(new DataPoint(graph2LastXValue, wave[0]), true, 6000);


                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_RESULTDATA_PO:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        String dataId=jsonObject.getString(PoProfile.DATAID);
                        int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                        int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                        float PI = (float) jsonObject.getDouble(PoProfile.PI_PO);
                        JSONArray jsonArray = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                        int[] wave = new int[3];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            wave[i] = jsonArray.getInt(i);
                        }
                        dataPR.setVisibility(View.VISIBLE);
                        dataSO02.setVisibility(View.VISIBLE);

                        lblSPO2.setText(oxygen+" %");
                        lblpR.setText(pulseRate+" bpm");

                        Log.i(TAG, "dataId:"+dataId+"--oxygen:" + oxygen + "--pulseRate:" + pulseRate + "--Pi:" + PI + "-wave1:" + wave[0]
                                + "-wave2:" + wave[1] + "--wave3:" + wave[2]);


                        graph2LastXValue += 1d;
                        mSeries2.appendData(new DataPoint(graph2LastXValue, wave[0]), true, 5000);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_NO_OFFLINEDATA_PO:
                    Log.i("action no offiline","--");
                    break;
                case PoProfile.ACTION_BATTERY_PO:
                    JSONObject jsonobject;
                    try {
                        jsonobject = (JSONObject) jsonTokener.nextValue();
                        int battery = jsonobject.getInt(PoProfile.BATTERY_PO);
                        Log.d(TAG, "battery:" + battery);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScanFinish() {
            if(!scanResult){

                //Toast.makeText(Demo1Activity.this, "Error : PulsiOximetro no encontrado", Toast.LENGTH_SHORT).show();
                //finish();
            }else {

            }

            //tv_discovery.setText("discover finish");
        }

    };

    private void getPulsiData(String mac){
        if (mPo3Control!=null){
            mPo3Control.startMeasure();
        }else {
            mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(mac);
            if (mPo3Control!=null){
                Log.d(TAG, "pulsi meassure process ");
                //Toast.makeText(RecollectorActivity.this, "tengo el control", Toast.LENGTH_SHORT).show();
                mPo3Control.startMeasure();
            }else {
                Log.d(TAG, "errpr al conectar con pulsi");
                Toast.makeText(this, "Error en conexion ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_SCAN:
                    Bundle bundle_scan = msg.getData();
                    String mac_scan = bundle_scan.getString("mac");
                    String type_scan = bundle_scan.getString("type");
                    HashMap<String, String> hm_scan = new HashMap<String, String>();
                    hm_scan.put("mac", mac_scan);
                    hm_scan.put("type", type_scan);
                    break;

                case HANDLER_CONNECTED:
                    Bundle bundle_connect = msg.getData();
                    String mac_connect = bundle_connect.getString("mac");
                    String type_connect = bundle_connect.getString("type");
                    HashMap<String, String> hm_connect = new HashMap<String, String>();
                    hm_connect.put("mac", mac_connect);
                    hm_connect.put("type", type_connect);
                    Log.e(TAG, "idps:" + iHealthDevicesManager.getInstance().getDevicesIDPS(mac_connect));
                    break;

                case HANDLER_DISCONNECT:
                    Bundle bundle_disconnect = msg.getData();
                    String mac_disconnect = bundle_disconnect.getString("mac");
                    String type_disconnect = bundle_disconnect.getString("type");
                    HashMap<String, String> hm_disconnect = new HashMap<String, String>();
                    hm_disconnect.put("mac", mac_disconnect);
                    hm_disconnect.put("type", type_disconnect);
                    //list_ConnectedDevices.remove(hm_disconnect);

                    // updateViewForConnected();

                    break;
                case HANDLER_USER_STATUE:
                    Bundle bundle_status = msg.getData();
                    String username = bundle_status.getString("username");
                    String userstatus = bundle_status.getString("userstatus");
                    String str = "username:" + username + " - userstatus:" + userstatus;
                    Toast.makeText(Demo1Activity.this, str, Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }




    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;

    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }




    //  EGG CHART ------------




    private synchronized void appendPoint(double sampleRate) {
        if (_currentIndex >= sourceData.length) {
            _currentIndex = 0;
        }

        // Get the next voltage and time, and append to the chart
        double voltage = sourceData[_currentIndex];
        double time = (_totalIndex / sampleRate) % 10;

        if (whichTrace == chartEcg.TraceAOrB.TraceA) {
            series0.append(time, voltage);
            series1.append(time, Double.NaN);
        } else {
            series0.append(time, Double.NaN);
            series1.append(time, voltage);
        }

        _currentIndex++;
        _totalIndex++;

        if (_totalIndex % 4000 == 0) {
            whichTrace = whichTrace == chartEcg.TraceAOrB.TraceA ? chartEcg.TraceAOrB.TraceB : chartEcg.TraceAOrB.TraceA;
        }
    }

    private final Runnable appendDataRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            if (isRunning) {
                for (int i = 0; i < 10; i++) {
                    appendPoint(400);
                }
            }
        }
    };

    enum TraceAOrB {
        TraceA,
        TraceB
    }


}
