package fr.lannier.iem.bpmusicapp.Activity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.lannier.iem.bpmusicapp.R;

import java.util.List;
import fr.lannier.iem.bpmusicapp.Models.Track;

public class AdapterListMusics extends ArrayAdapter<Track> {


    public AdapterListMusics(Context context , List<Track> objects) {
        super(context,0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapterlistmusic,parent, false);
        }

        ViewHolder viewHolder  = (ViewHolder) convertView.getTag();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.adapterlistmusic, parent, false);
            viewHolder.textViewTitre = (TextView) convertView.findViewById(R.id.textViewTitre);
            viewHolder.textViewArtiste = (TextView) convertView.findViewById(R.id.textViewArtist);
            viewHolder.textViewDuration = (TextView) convertView.findViewById(R.id.textViewDuration);
            viewHolder.imageViewIsPlaying = (ImageView) convertView.findViewById(R.id.imageViewIsPlaying);
            convertView.setTag(viewHolder);
        }

        Track music = getItem(position);
        viewHolder.textViewTitre.setText(music.getName());
        viewHolder.textViewArtiste.setText(music.getArtists());
        viewHolder.textViewDuration.setText(music.getDuration());
        /*if (music.getIsPlaying()) {
            viewHolder.imageViewIsPlaying.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.imageViewIsPlaying.setVisibility(View.INVISIBLE);
        }*/


        return convertView;
    }

    private static class ViewHolder{
        TextView textViewTitre;
        TextView textViewArtiste;
        TextView textViewDuration ;
        ImageView imageViewIsPlaying;
    }



}
