package org.vancinad.aircraft;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Aircraft {
    static final String _LOG_TAG = "Aircraft";

    public AircraftType mAircraftType;

    String mTailNumber = new String();
    double mEmptyWeight;
    double mEmptyCG;
    ArrayList<Double> mStationWeights = new ArrayList<>();
    boolean mIsApproved = false;

    /* uses deprecated constructor - for testing only */
    public static Aircraft getDebugAircraft(AircraftType type, String n734BG, double v, double v1) {
        return new Aircraft(type, n734BG, v, v1);
    }

    public boolean isApproved() { return mIsApproved; }
    public String getTailNumber() { return mTailNumber; }
    public double getEmptyWeight() { return mEmptyWeight; }
    public double getEmptyCG() { return mEmptyCG; }

    /***
     * Get aircraft by tail number
     *
     * @param tailNumber Aircraft tail number
     * @return Aircraft instance. Callers should call isApproved() on the returned instance to confirm airworthiness
     */
    static public Aircraft getByTailNumber(String tailNumber) throws IOException {
        Log.d(_LOG_TAG, String.format("Begin: getByTailNumber('%s')", tailNumber));
        File f = AircraftFactory.getInstance().getFileFor(tailNumber);
        Aircraft a = null;

        try {
            a = new Aircraft(f);
        } catch (Exception e) {
            Log.d(_LOG_TAG, "Caught exception in getByTailNumber");
            e.printStackTrace();
            throw e;
        }

        return a;
    }
    /***
     * Instantiate Aircraft from File
     *
     * Callers should call isApproved() on the returned instance to confirm airworthiness
     *
     * @param file Json file containing aircraft definition data
     */
    private Aircraft(File file) throws IOException {
        mEmptyCG = Double.MIN_VALUE; // invalid value
        JsonReader jr = new JsonReader(new FileReader(file)); // may throw FileNotFound
        jr.beginObject();
        while (jr.hasNext()) { // may throw exceptions
            String name = jr.nextName();
            switch (name) {
                case "tailNumber":
                    mTailNumber = jr.nextString().toUpperCase(); // Should be UC already but...
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
        }
        jr.endObject();

        mIsApproved = mAircraftType.isApproved() &&
                mTailNumber.length() > 0 &&
                mEmptyWeight > 0 &&
                mEmptyCG > Double.MIN_VALUE &&
                mStationWeights.size() == mAircraftType.numberOfStations();

    } // end: Aircraft(File)

    private void readStationWeights(JsonReader jr) throws IOException {
        mStationWeights.clear();
        jr.beginArray(); // may throw IOException
        while (jr.hasNext()) {
            mStationWeights.add(jr.nextDouble());
        }
        jr.endArray();
    }

    /* deprecated constructor -- for testing only */
    private Aircraft(AircraftType mType, String mTailNumber, double mEmptyWeight, double mEmptyCG) {
        this.mAircraftType = mType;
        mIsApproved = mType.isApproved();
        this.mTailNumber = mTailNumber.toUpperCase(); // force uppercase on tail numbers
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

    public Exception write() {
        // write aircraft data to file
        // return null if success or Exception
        Exception retEx = null;
        File file = AircraftFactory.getInstance().getFileFor(this);
        try {
            write(file);
        } catch (Exception e) {
            e.printStackTrace();
            retEx = e;
        }
        return retEx;
    }

    void write(File outputFile) throws IOException {
        JsonWriter jw = new JsonWriter(new FileWriter(outputFile));
        jw.beginObject();
        jw.name("tailNumber").value(mTailNumber);
        jw.name("emptyWeight").value(mEmptyWeight);
        jw.name("emptyCG").value(mEmptyCG);
        jw.name("type");
        mAircraftType.write(jw);
        writeStationWeights(jw);
        jw.endObject();
        jw.close();
    }

    private void writeStationWeights(JsonWriter jw) throws IOException {
        jw.name("stationWeights");
        jw.beginArray();
        for (double w : mStationWeights) {
            jw.value(w);
        }
        jw.endArray();
    }
}
