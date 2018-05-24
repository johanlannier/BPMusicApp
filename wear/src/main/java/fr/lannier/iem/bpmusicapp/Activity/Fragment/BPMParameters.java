package fr.lannier.iem.bpmusicapp.Activity.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fr.lannier.iem.bpmusicapp.BPMSingleton;
import fr.lannier.iem.bpmusicapp.R;

public class BPMParameters extends Fragment {

    private TextView textViewBPMDefault;
    private TextView textViewGo;

    private ImageView imageViewHand;

    private Button buttonLess;
    private Button buttonPlus;
    private View v;
    private int BPM;
    private boolean isBPMDefault;

    public BPMParameters(){

    }

    public static BPMParameters newInstance(){
        Bundle args = new Bundle();
        BPMParameters fragment = new BPMParameters();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle savedInstanceState) {
        v = pInflater.inflate(R.layout.fragment_bpmparameters, pContainer, false);

        BPM = 80;
        isBPMDefault = false;

        textViewBPMDefault = v.findViewById(R.id.textViewBPMDefault);

        imageViewHand = v.findViewById(R.id.imageViewHand);

        buttonLess = v.findViewById(R.id.buttonLess);
        buttonPlus = v.findViewById(R.id.buttonPlus);


        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BPM = BPM +1 ;
                textViewBPMDefault.setText(BPM + "");
                BPMSingleton.getInstance().setBPM(BPM);
            }
        });


        buttonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BPM = BPM -1 ;
                textViewBPMDefault.setText(BPM + "");
                BPMSingleton.getInstance().setBPM(BPM);
            }
        });


        imageViewHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBPMDefault = !isBPMDefault;
                if (isBPMDefault) {
                    BPMSingleton.getInstance().setBPMManual(false);
                    imageViewHand.setImageResource(R.drawable.handbarre);
                }
                else {
                    BPMSingleton.getInstance().setBPM(BPM);
                    BPMSingleton.getInstance().setBPMManual(true);
                    imageViewHand.setImageResource(R.drawable.hand);
                }
            }
        });

        return v;

    }
}
