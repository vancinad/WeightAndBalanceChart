package org.vancinad.wbchart.ui.aircraft;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AircraftViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AircraftViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}