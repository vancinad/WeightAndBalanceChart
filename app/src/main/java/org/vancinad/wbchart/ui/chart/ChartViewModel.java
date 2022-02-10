package org.vancinad.wbchart.ui.chart;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.vancinad.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.Iterator;

public class ChartViewModel extends ViewModel {


    Aircraft mAircraft;
    private ArrayList<MutableLiveData<Double>> weights;

    /* Constructor */
    public ChartViewModel() {
        weights = new ArrayList<>();
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
            setStationWeights();
        else
            aircraft.setStationWeights(getStationWeightsIterator());

    }
    /***
     * Intialize the model's data elements from the aircraft's stations
     */
    void setStationWeights() {
        weights.clear();
        int numStations = mAircraft.mAircraftType.numberOfStations();
        for (int i=0; i<numStations; i++) {
            MutableLiveData<Double> w = new MutableLiveData<>();
            w.setValue(mAircraft.getStationWeight(i));
            Log.d("ChartViewModel ",
                    "setStationWeights: w=" + Double.toString(w.getValue()) +
                            " index=" + Integer.toString(i) );
            weights.add(w);
        }
        this.mAircraft = mAircraft; // save aircraft reference for later
    }

    public MutableLiveData<Double> getStationWeight(int index) {
        MutableLiveData<Double> d = null;
        try {
            d = weights.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return d;
    }

    public boolean setStationWeight(int index, double w) {
        Log.d("ChartViewModel", String.format("setStationWeight: index=%d, w=%f", index, w));
        boolean successFlag = true;
        try {
            /* Update aircraft before LiveData, so it's accurate when Observer.onChanged() fires */
            mAircraft.setStationWeight(index, w);
            weights.get(index).setValue(w);
        }
        catch (IndexOutOfBoundsException e) {
            successFlag = false;
            e.printStackTrace();
        }

        return successFlag;
    }

    Iterator<MutableLiveData<Double>> getStationWeightsIterator() { return weights.iterator(); }

    public int numberOfStations() { return weights.size(); }

}