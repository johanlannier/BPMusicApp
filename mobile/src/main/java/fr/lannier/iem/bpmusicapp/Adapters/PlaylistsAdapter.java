package fr.lannier.iem.bpmusicapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fr.lannier.iem.bpmusicapp.Models.Playlist;
import fr.lannier.iem.bpmusicapp.R;

public class PlaylistsAdapter extends ArrayAdapter<Playlist> {
    PlaylistsViewHolder viewHolder;
    Bitmap bitmapimg;
    Context context;

    public PlaylistsAdapter(Context context, List<Playlist> playlists) {
        super(context, 0, playlists);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_item, parent, false);
        }

        viewHolder = (PlaylistsAdapter.PlaylistsViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new PlaylistsAdapter.PlaylistsViewHolder();
            viewHolder.playlistPicture = (ImageView) convertView.findViewById(R.id.playlistPicture);
            viewHolder.playlistName = (TextView) convertView.findViewById(R.id.playlistName);
            convertView.setTag(viewHolder);
        }

        Playlist PlaylistItem = getItem(position);

        viewHolder.playlistName.setText(PlaylistItem.getName());

         Picasso.with(context).load(PlaylistItem.getPicture()).into(viewHolder.playlistPicture);

        return convertView;
    }

    private class PlaylistsViewHolder {
        public ImageView playlistPicture;
        public TextView playlistName;
    }
}