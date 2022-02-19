package org.vancinad.aircraft;

import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Aircraft {
    public AircraftType mAircraftType;

    String mTailNumber;
    double mEmptyWeight;
    double mEmptyCG;
    ArrayList<Double> mStationWeights;
    boolean mIsApproved = false;

    public boolean isApproved() { return mIsApproved; }
    public String getTailNumber() { return mTailNumber; }
    public double getEmptyWeight() { return mEmptyWeight; }
    public double getEmptyCG() { return mEmptyCG; }


    public Aircraft(File file) throws IOException {
        mEmptyCG = Double.MIN_VALUE; // invalid value
        JsonReader jr = new JsonReader(new FileReader(file)); // may throw FileNotFound
        while (jr.hasNext()) { // may throw exceptions
            String name = jr.nextName();
            switch (name) {
                case "tailNumber":
                    mTailNumber = jr.nextString();
                    break;
                case "emptyWeight":
                    mEmptyWeight = jr.nextDouble();
                    break;
                case "emptyCG":
                    mEmptyCG = jr.nextDouble();
                    break;
                case "stationWeights":
                    readStationWeights(jr);
                    break;
                case "type":
                    mAircraftType = new AircraftType(jr);
                    break;
            }

            mIsApproved = mAircraftType.isApproved() &&
                    mTailNumber.length() > 0 &&
                    mEmptyWeight > 0 &&
                    mEmptyCG > Double.MIN_VALUE &&
                    mStationWeights.size() == mAircraftType.numberOfStations();
        }
    }

    private void readStationWeights(JsonReader jr) throws IOException {
        mStationWeights.clear();
        jr.beginArray(); // may throw IOException
        while (jr.hasNext()) {
            mStationWeights.add(jr.nextDouble());
        }
        jr.endArray();
    }

    public Aircraft(AircraftType mType, String mTailNumber, double mEmptyWeight, double mEmptyCG) {
        this.mAircraftType = mType;
        mIsApproved = mType.isApproved();
        this.mTailNumber = mTailNumber;
        this.mEmptyWeight = mEmptyWeight;
        this.mEmptyCG = mEmptyCG;
        this.mStationWeights = new ArrayList<>(mType.numberOfStations());
        for (int i=0; i<mType.numberOfStations(); i++)
            mStationWeights.add(0.0); // initialize weight values for all stations
    }

    public ArrayList<Station> getStations() {return mAircraftType.mStations;}
    public ArrayList<CGEnvelope> getEnvelopes() { return mAircraftType.mEnvelopes; }

    public double getGrossWeight() {
        double gw = mEmptyWeight;
        for (Double w: mStationWeights)
            gw += w;

        return gw;
    }

    public int totalMoment() {
        int moment = (int) (mEmptyWeight * mEmptyCG);
        for (int i = 0; i< mAircraftType.numberOfStations(); i++) {
            moment += getStationMoment(i);
        }
        return moment;
    }

    public double getCG() {
        return totalMoment() / getGrossWeight();
    }

    public double getStationWeight(int index) {
        return mStationWeights.get(index);
    }

    public void setStationWeight(int index, double newWeight) {
        mStationWeights.set(index, newWeight);
        Log.d("Aircraft", String.format("setStationWeight: index=%d weight=%f GW=%f CG=%f", index, newWeight, getGrossWeight(), getCG()));
    }

    /*
     * Set aircraft stations weights.
     */
    public void setStationWeights(@NonNull Iterator<MutableLiveData<Double>> weights) {
        int i=0;
        Log.d("Aircraft", "setStationWeights called...");
        while (weights.hasNext())
            setStationWeight(i++, weights.next().getValue());
    }

    public int getStationMoment(int index) {
        return (int) (mAircraftType.mStations.get(index).arm * mStationWeights.get(index));
    }
}
