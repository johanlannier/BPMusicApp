package fr.lannier.iem.bpmusicapp;

public class BPMSingleton {
    private static final BPMSingleton ourInstance = new BPMSingleton();

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
