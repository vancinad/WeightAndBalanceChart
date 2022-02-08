package org.vancinad.wbchart.aircraft;

import android.graphics.Color;

import java.util.ArrayList;

public class Cessna172N extends AircraftType {
    public Cessna172N() {
        super("3A12-172N");
    }

    public ArrayList<Station> loadStations() {

        ArrayList<Station> retStations;

        if (mStations == null) {
            retStations = new ArrayList<>();
            retStations.add(new Station(46.0, "Fuel"));
            retStations.add(new Station(37.0, "Front"));
            retStations.add(new Station(73.0, "Rear"));
            retStations.add(new Station(95.0, "Baggage 1"));
//            retStations.add(new Station(108.0, "Baggage 2 (front)"));
            retStations.add(new Station(123.0, "Baggage 2 (middle)"));
//            retStations.add(new Station(142.0, "Baggage 2 (rear)"));
        }
        else
            retStations = this.mStations;

        return retStations;
    }

    public ArrayList<CGEnvelope> loadEnvelopes()
    {
        ArrayList<CGEnvelope> retEnvelopes;
        if (mEnvelopes == null)
        {
            CGEnvelope utilityCategory = new CGEnvelope("Utility", Color.GREEN);
            utilityCategory
                    .addPoint(35.0f,1500)
                    .addPoint(35.0f,1950)
                    .addPoint(35.5f,2000)
                    .addPoint(40.5f,2000)
                    .addPoint(40.5f,1500);

            CGEnvelope normalCategory = new CGEnvelope("Normal", Color.CYAN);
            normalCategory
                    .addPoint(35.0f,1500)
                    .addPoint(35.0f,1950)
                    .addPoint(38.5f,2300)
                    .addPoint(47.3f,2300)
                    .addPoint(47.3f,1500);
            retEnvelopes = new ArrayList<>();
            retEnvelopes.add(normalCategory);
            retEnvelopes.add(utilityCategory);
        }
        else
            retEnvelopes = mEnvelopes;

        return retEnvelopes;
    }


}
