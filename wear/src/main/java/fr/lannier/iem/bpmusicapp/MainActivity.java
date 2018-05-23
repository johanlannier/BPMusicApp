package fr.lannier.iem.bpmusicapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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

import fr.lannier.iem.bpmusicapp.Activity.Adapter.AdapterListMusics;
import fr.lannier.iem.bpmusicapp.Activity.Fragment.Accueil;
import fr.lannier.iem.bpmusicapp.Activity.Fragment.BPMParameters;
import fr.lannier.iem.bpmusicapp.Activity.Fragment.Player;
import fr.lannier.iem.bpmusicapp.Models.Track;

public class MainActivity extends WearableActivity implements
        DataClient.OnDataChangedListener,
        SensorEventListener{

    //region variables
    private View view;
    private GridViewPager pager;
    private BPMParameters bpmParametersFragment;
    private TextView textViewRythmeRunner;
    private TextView textViewRythmeDefined;
    private SensorManager mSensorManager;
    private ListView listViewMusics;
    private AdapterListMusics adapterListMusics;
    private Sensor mHeartRateSensor;
    private int currentBPM=0;
    private List<Track> listTracks;

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

        if(textViewRythmeRunner == null) {
            textViewRythmeRunner = (TextView) findViewById(R.id.textViewRythmeRunner);
        }
        if(textViewRythmeDefined == null) {
            textViewRythmeDefined = (TextView) findViewById(R.id.textViewRythmeDefined);
        }

        textViewRythmeRunner.setText(Integer.toString(mHeartRate) + " ❤️");

        if (!BPMSingleton.getInstance().isBPMManual()){
            textViewRythmeDefined.setText(String.format(Integer.toString(mHeartRate%10*10)));
        } else {
            textViewRythmeDefined.setText(BPMSingleton.getInstance().getBPM() + "");
        }
        currentBPM=mHeartRate;
    }

    public void Refresh(final List<Track> pListMusic){
        AdapterListMusics adapter  = new AdapterListMusics(this, pListMusic);
        listViewMusics = (ListView) view.findViewById(R.id.listViewMusics);
        listViewMusics.setAdapter(adapter);
    }

    public void SendBPMToPhone(){
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        listViewMusics = (ListView) findViewById(R.id.listViewMusics);
        textViewRythmeRunner = (TextView) findViewById(R.id.textViewRythmeRunner);
        textViewRythmeDefined = (TextView) findViewById(R.id.textViewRythmeDefined);
        listTracks = new ArrayList<Track>();
        adapterListMusics =  new AdapterListMusics(this, listTracks);
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
                listTracks=new ArrayList<>();
                for(int i=0;i<listDataMap.size();i++){
                    listTracks.add(new Track(listDataMap.get(i)));
                }
                Refresh(listTracks);
                //TO DO add to listView
            }
        }
        Log.e("TEST","data changed");
    }

    private void setupViews() {
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageCount(2);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(pager);

        bpmParametersFragment = BPMParameters.newInstance();
        Accueil accueilFragment =  Accueil.newInstance();
        Player playerFragment = Player.newInstance();

        List<android.app.Fragment> pages = new ArrayList<>();
        pages.add(accueilFragment);
        pages.add(playerFragment);
        pages.add(bpmParametersFragment);
        final MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager(), pages);
        pager.setAdapter(adapter);
    }

    private class MyPagerAdapter extends FragmentGridPagerAdapter {

        private List<android.app.Fragment> mFragments;

        public MyPagerAdapter(android.app.FragmentManager fm, List<android.app.Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public android.app.Fragment getFragment(int row, int column) {
            return mFragments.get(column);
        }

    }

    public void refreshBPMDefined(){
        if(textViewRythmeDefined == null) {
            textViewRythmeDefined = (TextView) findViewById(R.id.textViewRythmeDefined);
        }
        textViewRythmeDefined.setText(BPMSingleton.getInstance().getBPM());
    }
}
