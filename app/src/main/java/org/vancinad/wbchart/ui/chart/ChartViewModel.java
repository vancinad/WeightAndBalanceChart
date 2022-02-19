package org.vancinad.wbchart.ui.chart;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.vancinad.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.Iterator;

public class ChartViewModel extends ViewModel {


    Aircraft mAircraft;
    ArrayList<MutableLiveData<Double>> mWeights;

    /* Constructor */
    public ChartViewModel() {
        mWeights = new ArrayList<>();
    }

    /***
     * Associate this model with the given aircraft.
     *
     * @param aircraft Aircraft to associate with the model
     * @param useAircraftWeights If true, load the model with aircraft's weight data. If false, load aircraft from the model.
     */
    public void setAircraft(Aircraft aircraft, boolean useAircraftWeights) {
        Log.d("ChartViewModel", String.format("setStationWeights(): Aircraft=%s, Using %s weights", aircraft.toString(), (useAircraftWeights) ? "aircraft's" : "model's"));
        mAircraft = aircraft;
        if (useAircraftWeights)
            initStationWeights();
        else
            aircraft.setStationWeights(getStationWeightsIterator());

    }
    /***
     * Intialize the model's data elements from the aircraft's stations
     */
    void initStationWeights() {
        mWeights.clear();
        int numStations = mAircraft.mAircraftType.numberOfStations();
        for (int i=0; i<numStations; i++) {
            MutableLiveData<Double> w = new MutableLiveData<>();
            w.setValue(mAircraft.getStationWeight(i));
            Log.d("ChartViewModel", String.format("initStationWeights: w=%f index=%d", w.getValue(), i ));
            mWeights.add(w);
        }
    }

    public MutableLiveData<Double> getStationWeight(int index) {
        MutableLiveData<Double> d = null;
        try {
            d = mWeights.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return d;
    }

    /***
     * Set the model and aircraft station weights for the given station index.
     * No update occurs if given weight is same as current for the station.
     *
     * @param index Index of the station to be set
     * @param w New weight for the station
     * @return true if update is performed. false if exception or if given weight is same as current for the station
     */
    public boolean setStationWeight(int index, double w) {
        Log.d("ChartViewModel", String.format("setStationWeight: index=%d, w=%f", index, w));
        boolean successFlag = false;

        try {
            MutableLiveData<Double> modelStationWeight = mWeights.get(index);
            if (w != modelStationWeight.getValue()) { //only set if given weight is different
                /* Update aircraft before LiveData, so it's accurate when Observer.onChanged fires */
                mAircraft.setStationWeight(index, w);
                modelStationWeight.setValue(w);
                successFlag = true;
            }
        }
        catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
        }

        return successFlag;
    }

    Iterator<MutableLiveData<Double>> getStationWeightsIterator() { return mWeights.iterator(); }

    public int numberOfStations() { return mWeights.size(); }

}