package fr.lannier.iem.bpmusicapp.Activity.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

            }
        });

        imageViewPlay = v.findViewById(R.id.imageViewPlay);

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlay = !isPlay;
                if (isPlay) {
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
