package fr.lannier.iem.bpmusicapp.Activity.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import fr.lannier.iem.bpmusicapp.BPMSingleton;
import fr.lannier.iem.bpmusicapp.MainActivity;
import fr.lannier.iem.bpmusicapp.R;

public class Player extends Fragment {

    private View v;

    private TextView textViewTitre;
    private TextView textViewArtist;

    private ImageView imageViewSongBefore;
    private ImageView imageViewSongAfter;
    private ImageView imageViewPlay;
    //private ImageView imageViewVolumeMoins;
    //private ImageView imageViewVolumePlus;

    private boolean isPlay;

    public Player() {
    }


    public static Player newInstance() {
        Player fragment = new Player();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        isPlay = false;

        v = inflater.inflate(R.layout.fragment_player, container, false);
        textViewTitre = v.findViewById(R.id.textViewTitre);
        textViewArtist = v.findViewById(R.id.textViewArtist);

        imageViewSongBefore = v.findViewById(R.id.imageViewSongBefore);

        imageViewSongBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviousSong();
            }
        });

        imageViewPlay = v.findViewById(R.id.imageViewPlay);

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayPause();
                BPMSingleton.getInstance().isPlaying=!BPMSingleton.getInstance().isPlaying;
                if (BPMSingleton.getInstance().isPlaying) {
                    imageViewPlay.setImageResource(R.drawable.pause);
                }
                else {
                    imageViewPlay.setImageResource(R.drawable.play);
                }

            }
        });

        imageViewSongAfter = v.findViewById(R.id.imageViewSongAfter);

        imageViewSongAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextSong();
            }
        });

        /*imageViewVolumeMoins= v.findViewById(R.id.imageViewVolumeMoins);

        imageViewVolumeMoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        imageViewVolumePlus = v.findViewById(R.id.imageViewVolumePlus);

        imageViewVolumePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/


        return v;
    }

    public void NextSong(){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/NextTrack");
        dataMap.getDataMap().putLong("Time", System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(v.getContext()).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("NEXT_SONG","envoi reussi, Next Song");
            }
        });
    }

    public void PreviousSong(){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/PreviousTrack");
        dataMap.getDataMap().putLong("Time", System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(v.getContext()).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("NEXT_SONG","envoi reussi, Previous Song");
            }
        });
    }

    public void PlayPause(){
        PutDataMapRequest dataMap = PutDataMapRequest.create("/PlayPause");
        dataMap.getDataMap().putLong("Time", System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(v.getContext()).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("NEXT_SONG","envoi reussi, Play/Pause");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void RefreshCurrentMusic(String title, String artists){
        textViewArtist.setText(artists);
        textViewTitre.setText(title);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
