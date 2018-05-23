package fr.lannier.iem.bpmusicapp.Models;

import com.google.android.gms.wearable.DataMap;

public class Track {

    private String id;
    private String name;
    private int bpm;
    private String artists;
    private int duration;

    public Track() {
    }

    public Track(String id, String name, int bpm, String artists, int duration) {
        this.id = id;
        this.name = name;
        this.bpm = bpm;
        this.artists = artists;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public DataMap ToDataMap(){
        DataMap tmp=new DataMap();
        tmp.putString("id",this.id);
        tmp.putString("name",this.name);
        tmp.putLong("bpm",this.bpm);
        tmp.putString("artists",this.artists);
        tmp.putInt("duration",this.duration);
        return tmp;
    }

    public Track(DataMap data){
        this(data.getString("id"), data.getString("name"), data.getInt("bpm"),data.getString("artists"), data.getInt("duration"));
    }
}
