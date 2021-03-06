package fr.lannier.iem.bpmusicapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import fr.lannier.iem.bpmusicapp.Adapters.PlaylistsAdapter;
import fr.lannier.iem.bpmusicapp.Models.Playlist;
import fr.lannier.iem.bpmusicapp.Models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener,
        SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback
{

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String CLIENT_ID = "41451448696d446faa2a9b80b9d44064";
    private static final String REDIRECT_URI = "http://localhost:3000/log";
    private static String Playlist_id;
    private static String token="";
    private static String playlist_Id="";
    private Player mPlayer;
    private ListView lv_playlists;
    private List<Playlist> ListPlaylists;
    private List<Track> listTracks;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // The only thing that's different is we added the 5 lines below.
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private", "playlist-read-collaborative"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, 1337, request);
        lv_playlists=findViewById(R.id.lv_playlists);

        TextView editIPtv=findViewById(R.id.editIP);
        editIPtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editIP();
            }
        });

        new StartWearableActivityTask().execute();
    }

    public void editIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adresse IP du serveur :");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String tmp = sharedPref.getString("IPServer", "192.168.42.142");
        input.setText(tmp);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor prefsEdit = prefs.edit();
                prefsEdit.putString("IPServer", input.getText().toString());
                prefsEdit.apply();
                BPMApp.refreshService();
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    public void GetTracks(int bpm){
        if(playlist_Id != "") {
            Call<List<Track>> call = BPMApp.getBPMService().getTracksByBPM(token, playlist_Id, bpm);
            call.enqueue(new Callback<List<Track>>() {
                @Override
                public void onResponse(retrofit2.Call<List<Track>> call, Response<List<Track>> response) {
                    listTracks = response.body();
                    if (response.code() == 200) {
                        SendTracksToWatch(listTracks);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<Track>> call, Throwable t) {
                    Log.e("ERREUR", t.getMessage());

                }
            });
        }
    }

    public void playTrack(String trackId, int index2){
        if(index2 >= listTracks.size()){
            index2 = -1;
        }
        mPlayer.playUri(null, "spotify:track:" + trackId, 0, 0);
        index = index2 + 1;
        RefreshCurrentTrack();
    }

    public void resumePause(){
        if(mPlayer.getPlaybackState().isPlaying){
            mPlayer.pause(null);
        } else {
            mPlayer.resume(null);
        }
    }

    public void resetTracks(){
        index = 0;
    }

    public void nextTrack(){
        if(index >= listTracks.size()){
            index = 0;
        } else if(listTracks.size() > 0){
            playTrack(listTracks.get(index).getId(), index);
        }
    }

    public void previousTrack(){
        if(index > 1){
            playTrack(listTracks.get(index - 2).getId(), index - 2);
        }
    }

    public void SendTracksToWatch(List<Track> tracks){
        ArrayList<DataMap> listDataMap=new ArrayList<>();
        for(int i=0; i<tracks.size();i++){
            listDataMap.add(tracks.get(i).ToDataMap());
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create("/TRACKS");
        dataMap.getDataMap().putDataMapArrayList("Tracks", listDataMap);
        dataMap.getDataMap().putLong("Time", System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("TEST","envoi reussi Tracks");
            }
        });
    }

    public void GetPlaylists(){
        Call<List<Playlist>> call = BPMApp.getBPMService().getPlaylists(token);
        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Playlist>> call, Response<List<Playlist>> response) {
                ListPlaylists = response.body();
                if (response.code() == 200) {
                    //TO DO add playlists to listview
                    lv_playlists.setAdapter(new PlaylistsAdapter(MainActivity.this, ListPlaylists));
                    lv_playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            playlist_Id=ListPlaylists.get(position).getId();
                            Toast.makeText(MainActivity.this, "Playlist sélectionnée, choisissez la musique sur votre montre", Toast.LENGTH_SHORT).show();
                            GetTracks(80);
                            resetTracks();
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Playlist>> call, Throwable t) {
                Log.e("ERREUR", t.getMessage());

            }
        });
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        //mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("MainActivity", "Received connection message: " + s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        if (Objects.equals(playerEvent.name(), PlayerEvent.kSpPlaybackNotifyTrackChanged.name()) && !mPlayer.getPlaybackState().isPlaying) {
            nextTrack();
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.e("TEST","capability changed");
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.e("TEST","message received");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.e("TEST","data changed");

                String path = event.getDataItem().getUri().getPath();

                if(path.equals("/BPM")){
                    byte[] data=event.getDataItem().getData();
                    TextView bpm=findViewById(R.id.BPM);
                    DataMap datamap=DataMap.fromByteArray(data);
                    String bpmData=datamap.getString("BPM");
                    bpm.setText(bpmData);
                }

                if(path.equals("/BPMTracks")){
                    byte[] data=event.getDataItem().getData();
                    DataMap datamap=DataMap.fromByteArray(data);
                    int bpmData=datamap.getInt("BPMTracks");
                    GetTracks(bpmData);
                }

                if(path.equals("/PlayTrack")){
                    byte[] data=event.getDataItem().getData();
                    DataMap datamap=DataMap.fromByteArray(data);
                    String idTrack=datamap.getString("Track");
                    int indexTrack=datamap.getInt("indexTrack");
                    playTrack(idTrack, indexTrack);
                }

                if(path.equals("/NextTrack")){
                    nextTrack();
                    SendCurrentTrack();
                }

                if(path.equals("/PreviousTrack")){
                    previousTrack();
                    SendCurrentTrack();
                }

                if(path.equals("/PlayPause")){
                    resumePause();
                }
            }
        }
    }

    public void SendCurrentTrack(){
        String tmpName = "";
        String tmpArtists = "";
        if(listTracks.size() != 0) {
            if(index == 0){
                tmpName = listTracks.get(index).getName();
                tmpArtists = listTracks.get(index).getArtists();
            } else {
                tmpName = listTracks.get(index-1).getName();
                tmpArtists = listTracks.get(index-1).getArtists();
            }
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create("/CurrentTrack");
        dataMap.getDataMap().putString("Title", tmpName);
        dataMap.getDataMap().putString("Artists", tmpArtists);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        final String finalTmpName = tmpName;
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.e("TEST","envoi reussi, CurrentSong: "+ finalTmpName);
            }
        });
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());

            }

        } catch (ExecutionException exception) {
            Log.e("TEST", "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e("TEST", "Interrupt occurred: " + exception);
        }

        return results;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(
                        this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    @WorkerThread
    private void sendStartActivityMessage(String node) {

        Task<Integer> sendMessageTask =
                Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, new byte[0]);

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            Integer result = Tasks.await(sendMessageTask);

        } catch (ExecutionException exception) {
            Log.e("TEST", "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e("TEST", "Interrupt occurred: " + exception);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        // The next 19 lines of the code are what you need to copy & paste! :)
        if (requestCode == 1337) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                token=response.getAccessToken();
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                        GetPlaylists();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    public void RefreshCurrentTrack(){
        try{
            Thread.sleep(500);
            LinearLayout player=findViewById(R.id.player);
            player.setVisibility(View.VISIBLE);
            TextView currentTitle=findViewById(R.id.currentTitle);
            TextView currentArtists=findViewById(R.id.currentArtists);
            ImageView currentImg=findViewById(R.id.currentImg);
            currentArtists.setText(mPlayer.getMetadata().currentTrack.artistName);
            currentTitle.setText(mPlayer.getMetadata().currentTrack.name);
            Picasso.with(this).load(mPlayer.getMetadata().currentTrack.albumCoverWebUrl).into(currentImg);
        }catch (Exception e){

        }
    }
}
