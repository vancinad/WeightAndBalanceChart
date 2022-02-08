package org.vancinad.wbchart.aircraft;

import java.util.ArrayList;

public abstract class AircraftType {
    ArrayList<Station> mStations = null;
    ArrayList<CGEnvelope> mEnvelopes = null;
    String mTypeString;
    private boolean mIsApproved = false;
    double grossWeight = 0;
    double CG = 0;

    //TODO: Make constructor private and implement AircraftTypeFactory.
    AircraftType(String mTypeString) {
        this.mTypeString = mTypeString;
        mStations = loadStations();
        mEnvelopes = loadEnvelopes();
        if (mStations != null && mEnvelopes !=null)
            mIsApproved = true; // Type is approved!
    }

    //TODO: Implement AircraftTypeFactory(String aircraftType)
    /*
        Read "types directory" for all defined types.
        Find definition corresponding to aircraftType string
        Eliminate type-specific classes (e.g. Cessna172N extends AircraftType)
        Make AircraftType concrete instead of abstract
        Load
     */
    abstract ArrayList<Station> loadStations();
    abstract ArrayList<CGEnvelope> loadEnvelopes();

    public int numberOfStations() { return mStations.size(); }
    public boolean isApproved() {return mIsApproved;}
}
