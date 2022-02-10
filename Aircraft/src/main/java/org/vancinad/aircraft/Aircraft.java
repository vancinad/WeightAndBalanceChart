package org.vancinad.aircraft;

import android.content.Context;
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

    public static Aircraft Factory(String typeString, String tailNumber, Double emptyWeight, Double emptyCG, Context applicationContext) {
        AircraftType newAircraftType = null;
        Aircraft newAircraft = null;

        //if (typeString.equals("3A12-172N.json")) newAircraftType = new Cessna172N();
        // TODO: Need to handle other aircraft types. Anything other than 3A12-172N.json will throw an NPE
        newAircraftType = AircraftType.Factory("3A12-172N", applicationContext);
        if (newAircraftType != null)
            if (newAircraftType.isApproved()) {
                newAircraft = new Aircraft(newAircraftType, tailNumber, emptyWeight, emptyCG);
                if (newAircraft != null)
                    newAircraft.mIsApproved = true;
            }
        return newAircraft;
    }

    public boolean isApproved() { return mIsApproved; }

    private Aircraft(AircraftType mType, String mTailNumber, double mEmptyWeight, double mEmptyCG) {
        this.mAircraftType = mType;
        this.mTailNumber = mTailNumber;
        this.mEmptyWeight = mEmptyWeight;
        this.mEmptyCG = mEmptyCG;
        this.mStationWeights = new ArrayList<>(mType.numberOfStations());
        for (int i=0; i<mType.numberOfStations(); i++)
            mStationWeights.add(0.0);
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
