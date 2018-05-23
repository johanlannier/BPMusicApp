package fr.lannier.iem.bpmusicapp;

import java.util.List;

import fr.lannier.iem.bpmusicapp.Models.Playlist;
import fr.lannier.iem.bpmusicapp.Models.Track;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface BPMService {

    @GET("playlists")
    Call<List<Playlist>> getPlaylists(@Header("authorization") String token);

    @GET("playlists/{id}/{bpm}")
    Call<List<Track>> getTracksByBPM(@Header("authorization") String token, @Path("id") String id, @Path("bpm") int bpm);

}
