package fr.lannier.iem.bpmusicapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import fr.lannier.iem.bpmusicapp.Models.Track;

public class MainActivity extends WearableActivity implements
        DataClient.OnDataChangedListener,
        SensorEventListener{

    //region variables
    private View view;

    private TextView textViewRythmeRunner;
    private TextView textViewRythmeDefined;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private int currentBPM=0;

    //endregion

    @Override
    protected void onResume() {
        super.onResume();
        StartSensor();
        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        }

    @Override
    protected void onPause() {
        super.onPause();
        StopSensor();
        Wearable.getDataClient(this).removeListener(this);
        }


    private void StartSensor(){
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void StopSensor(){
        mSensorManager.unregisterListener(this, mHeartRateSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float mHeartRateFloat = event.values[0];

        int mHeartRate = Math.round(mHeartRateFloat);

        textViewRythmeRunner.setText(Integer.toString(mHeartRate) + " ❤️");

        int interval=5;
        if(currentBPM>= mHeartRate+interval || currentBPM<=mHeartRate-interval){
            currentBPM=mHeartRate;

            PutDataMapRequest dataMap = PutDataMapRequest.create("/BPM");
            dataMap.getDataMap().putString("BPM", ""+currentBPM);
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();
            Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
            dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) {
                    Log.e("TEST","envoi reussi, BPM: "+currentBPM);
                }
            });

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewRythmeRunner = (TextView) findViewById(R.id.textViewRythmeRunner);
        textViewRythmeDefined = (TextView) findViewById(R.id.textViewRythmeDefined);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        StartSensor();



        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            String path = event.getDataItem().getUri().getPath();
            if(path.equals("/TRACKS")){
                byte[] data=event.getDataItem().getData();
                DataMap datamap=DataMap.fromByteArray(data);
                ArrayList<DataMap> listDataMap=datamap.getDataMapArrayList("Tracks");
                List<Track> listTracks=new ArrayList<>();
                for(int i=0;i<listDataMap.size();i++){
                    listTracks.add(new Track(listDataMap.get(i)));
                }
                //TO DO add to listView
            }
        }
        Log.e("TEST","data changed");
    }
}
