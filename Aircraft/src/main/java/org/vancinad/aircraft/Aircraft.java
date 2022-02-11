package org.vancinad.aircraft;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

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

    public double grossWeight() {
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

    public double centerOfGravity() {
        return totalMoment() / grossWeight();
    }

    public double getStationWeight(int index) {
        return mStationWeights.get(index);
    }

    public void setStationWeight(int index, double newWeight) {
        mStationWeights.set(index, newWeight);
        Log.d("Aircraft", String.format("setStationWeight: index=%d weight=%f GW=%f CG=%f", index, newWeight, grossWeight(), centerOfGravity()));
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
        return (int) (getStations().get(index).arm * mStationWeights.get(index));
    }
}
