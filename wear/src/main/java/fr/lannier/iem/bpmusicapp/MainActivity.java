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
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private int currentBPMDefined=0;
    private Player playerFragment;

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
            int tmpBPM = (mHeartRate/10)*10;
            if(tmpBPM != currentBPMDefined && tmpBPM > 50){
                currentBPMDefined = tmpBPM;
                textViewRythmeDefined.setText(Integer.toString(tmpBPM) + " 🎵");
                resetTracks(tmpBPM);
            }
        } else {
            textViewRythmeDefined.setText(BPMSingleton.getInstance().getBPM() + " 🎵");
        }
        currentBPM=mHeartRate;
        SendBPMToPhone();
    }

    public void Refresh(){
        AdapterListMusics adapter  = new AdapterListMusics(this, BPMSingleton.getInstance().listTracks);
        listViewMusics = (ListView) findViewById(R.id.listViewMusics);
        listViewMusics.setAdapter(adapter);
        listViewMusics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SendSelectedTrack(BPMSingleton.getInstance().listTracks.get(position), position);
                BPMSingleton.getInstance().isPlaying=true;
                playerFragment.RefreshCurrentMusic(BPMSingleton.getInstance().listTracks.get(position).getName(),BPMSingleton.getInstance().listTracks.get(position).getArtists());
                BPMSingleton.getInstance().CurrentPosition=position;
            }
        });
    }

    public void resetTracks(int BPM){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/BPMTracks");
        dataMap.getDataMap().putInt("BPMTracks", BPM);
        dataMap.getDataMap().putLong("Time", System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("TEST","envoi reussi, BPMTracks: ");
            }
        });
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
        BPMSingleton.getInstance().listTracks = new ArrayList<Track>();
        adapterListMusics =  new AdapterListMusics(this, BPMSingleton.getInstance().listTracks);
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
                BPMSingleton.getInstance().listTracks=new ArrayList<>();
                for(int i=0;i<listDataMap.size();i++){
                    BPMSingleton.getInstance().listTracks.add(new Track(listDataMap.get(i)));
                }
                Refresh();
                //TO DO add to listView
            }

            if(path.equals("/CurrentTrack")){
                byte[] data=event.getDataItem().getData();
                DataMap datamap=DataMap.fromByteArray(data);
                BPMSingleton.getInstance().CurrentTitle=datamap.getString("Title");
                BPMSingleton.getInstance().CurrentArtists=datamap.getString("Artists");
                playerFragment.RefreshCurrentMusic(BPMSingleton.getInstance().CurrentTitle,BPMSingleton.getInstance().CurrentArtists);
            }
        }
        Log.e("TEST","data changed");
    }

    public void SendSelectedTrack(final Track track, int position){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/PlayTrack");
        dataMap.getDataMap().putString("Track", track.getId());
        dataMap.getDataMap().putInt("indexTrack", position);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("PLAYING","envoi reussi, Track ID: "+track.getId()+", Name: "+track.getName());
            }
        });
    }

    private void setupViews() {
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageCount(2);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(pager);

        bpmParametersFragment = BPMParameters.newInstance();
        Accueil accueilFragment =  Accueil.newInstance();
        playerFragment = Player.newInstance();

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
