package com.ecg.dippo.ecg_final;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ecg.dippo.ecg_final.data.DataManager;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalAnchorPoint;
import com.scichart.charting.visuals.annotations.TextAnnotation;
import com.scichart.charting.visuals.annotations.VerticalAnchorPoint;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.data.model.DoubleRange;
import com.scichart.data.model.ISciList;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class chartEcg extends AppCompatActivity {


    IXyDataSeries<Double, Double> series0 ;
    IXyDataSeries<Double, Double> series1 ;
    private final static long TIME_INTERVAL = 20;

    private double[] sourceData;

    private int _currentIndex=0;
    private int _totalIndex=0;

    private TraceAOrB whichTrace = TraceAOrB.TraceA;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> schedule;

    private volatile boolean isRunning = true;

    Button btnError1,btnError2,btnError3,btnError4,btnError5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_ecg);



        final SciChartSurface surface = new SciChartSurface(this);
        final LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartecg2);

        btnError1=(Button)findViewById(R.id.btnAR);

        btnError2=(Button)findViewById(R.id.btnAR1);
        btnError3=(Button)findViewById(R.id.btnAR2);
        btnError4=(Button)findViewById(R.id.btnAR3);
        btnError5=(Button)findViewById(R.id.btnAR4);;





        btnError1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModalError(chartEcg.this,1);
            }
        });


        btnError2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModalError(chartEcg.this,3);
            }
        });


        btnError3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModalError(chartEcg.this,2);
            }
        });


        btnError4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModalError(chartEcg.this,4);
            }
        });


        btnError5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openModalError(chartEcg.this,5);
            }
        });





        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        sourceData = DataManager.getInstance().loadWaveformData(getApplicationContext());



        series0 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(3850).build();
        series1 = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withFifoCapacity(3850).build();

        final IAxis xBottomAxis = sciChartBuilder.newNumericAxis()
                .withVisibleRange(new DoubleRange(0d, 10d))
                .withAutoRangeMode(AutoRange.Never)
                .withAxisTitle("Time (seconds)")
                .withVisibility(1)
                .build();

        final IAxis yRightAxis = sciChartBuilder.newNumericAxis()
                .withVisibleRange(new DoubleRange(0.0d, 1.6d))
                .withAxisTitle("Voltage (mV)")
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

        chartLayout.addView(surface);
    }





    private synchronized void appendPoint(double sampleRate) {
        if (_currentIndex >= sourceData.length) {
            _currentIndex = 0;
        }

        // Get the next voltage and time, and append to the chart
        double voltage = sourceData[_currentIndex];
        double time = (_totalIndex / sampleRate) % 10;

        if (whichTrace == TraceAOrB.TraceA) {
            series0.append(time, voltage);
            series1.append(time, Double.NaN);
        } else {
            series0.append(time, Double.NaN);
            series1.append(time, voltage);
        }

        _currentIndex++;
        _totalIndex++;

        if (_totalIndex % 4000 == 0) {
            whichTrace = whichTrace == TraceAOrB.TraceA ? TraceAOrB.TraceB : TraceAOrB.TraceA;
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


    private  void openModalError(Context cont,int image){
        Dialog dialog = new Dialog(cont);

        dialog.setContentView(R.layout.modal_error);

        ImageView cuerpo=(ImageView)dialog.findViewById(R.id.cuerpo);
        ImageView cuerpo_bra_der=(ImageView)dialog.findViewById(R.id.cuerpo_bra_der);
        ImageView cuerpo_bra_izq=(ImageView)dialog.findViewById(R.id.cuerpo_bra_izq);
        ImageView cuerpo_pier_der=(ImageView)dialog.findViewById(R.id.cuerpo_pier_der);
        ImageView cuerpo_pier_izq=(ImageView)dialog.findViewById(R.id.cuerpo_pier_izq);



        Log.e("imagen",String.valueOf(image));
        if(image==1){
            cuerpo.setVisibility(View.VISIBLE);
        }

        if(image==2){
            cuerpo_bra_der.setVisibility(View.VISIBLE);
        }


        if(image==3){
            cuerpo_bra_izq.setVisibility(View.VISIBLE);
        }


        if(image==4){
            cuerpo_pier_der.setVisibility(View.VISIBLE);
        }


        if(image==5){
            cuerpo_pier_izq.setVisibility(View.VISIBLE);
        }



        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                // do something

            }
        });

        try {
            dialog.show();
        }catch (Exception e){
            Log.e("error",e.toString());
        }
    }

}
