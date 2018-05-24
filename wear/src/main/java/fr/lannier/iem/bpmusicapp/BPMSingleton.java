package fr.lannier.iem.bpmusicapp;

import java.util.List;

import fr.lannier.iem.bpmusicapp.Models.Track;

public class BPMSingleton {
    private static final BPMSingleton ourInstance = new BPMSingleton();

    public boolean isPlaying=false;
    public String CurrentTitle="Title";
    public String CurrentArtists="Artists";
    public String CurrentId="null";
    public List<Track> listTracks;

    private int BPM;

    private boolean isBPMManual;

    public static BPMSingleton getInstance() {
        return ourInstance;
    }

    private BPMSingleton() {
        this.isBPMManual = false;
    }

    public int getBPM() {
        return BPM;
    }

    public void setBPM(int BPM) {
        this.BPM = BPM;
    }

    public boolean isBPMManual() {
        return isBPMManual;
    }

    public void setBPMManual(boolean BPMManual) {
        isBPMManual = BPMManual;
    }


}
