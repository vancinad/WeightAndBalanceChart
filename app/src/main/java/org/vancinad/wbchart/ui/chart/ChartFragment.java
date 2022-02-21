package org.vancinad.wbchart.ui.chart;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.vancinad.aircraft.Aircraft;
import org.vancinad.aircraft.AircraftTypeFactory;
import org.vancinad.aircraft.Station;
import org.vancinad.wbchart.R;

import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class ChartFragment extends Fragment {

    static final String _LOG_TAG = "ChartFragment";
    
    private ChartViewModel chartViewModel;
    ConstraintLayout layout;
    Aircraft aircraft;
    WBChart wbChart;

    ArrayList<StationUIPair> uiPairs;
    static class StationUIPair {
        TextView label;
        EditText editField;
    }

    @Override
    public void onDestroy() {
        Log.i("INFO", "Destroying ChartFragment");
        super.onDestroy();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(_LOG_TAG, "Begin onCreateView()");

        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_chart, container, false);
        chartViewModel = new ViewModelProvider(this.getActivity()).get(ChartViewModel.class);

        AircraftTypeFactory factory = AircraftTypeFactory.getInstance();
        assert factory != null; // should have been initialized when activity was started

        try {
            aircraft = Aircraft.getByTailNumber("N734BG");
        } catch (IOException e) {
            e.printStackTrace();
            final String msg = "getByTailNumber exception. Using default aircraft";
            Log.d(_LOG_TAG, msg);
            //Snackbar.make(layout.getRootView(), msg, Snackbar.LENGTH_LONG).show();
            aircraft = Aircraft.getDebugAircraft(factory.getType("3A12-172N"), "N734BG", 1436.2, 39.26);
        }
        if (aircraft == null || !aircraft.isApproved())
            Log.d(_LOG_TAG, "Unapproved aircraft, tail '%s' (this probably won't end well)");

        // put aircraft info in app menu bar
        Navigation.findNavController(container).
                getCurrentDestination().
                setLabel(String.format(Locale.getDefault(), "%s: %s, %s",
                        getString(R.string.menu_chart), aircraft.getTailNumber(), aircraft.mAircraftType.getTypeName()));

        chartViewModel.setAircraft(aircraft, (chartViewModel.numberOfStations() == 0)); // if chartViewModel is uninitialized, load stations from aircraft's data


        wbChart = new WBChart(getChartConfig(), aircraft);

        Log.d(_LOG_TAG, "End onCreateView()");
        return layout;
    }

    @Override
    public void onResume() {
        Log.d(_LOG_TAG, "Begin onResume()");
        super.onResume();
        setUI();
        addChart();
        Log.d(_LOG_TAG, "End onResume()");
    }

    public void onDestroyView() {
        Log.d(_LOG_TAG, "Begin onDestroy");
        aircraft.write();
        super.onDestroyView();
    }

    /***
     * Create text and edit fields for each aircraft station.
     * Vertical barrier separates texts on left from edits on right.
     * Texts align baseline to baseline with associated edit field.
     *
     */
    private void setUI() {
        Log.d(_LOG_TAG, "Begin setUI()");
        Context context = layout.getContext();
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        TextView aircraftText = layout.findViewById(R.id.aircraft);
        String aircraftString = String.format(Locale.getDefault(), "Empty: %.1f @ %.2f", aircraft.getEmptyWeight(), aircraft.getEmptyCG());
        aircraftText.setText(aircraftString);

        uiPairs = new ArrayList<>(chartViewModel.numberOfStations());
//        int layoutId = layout.getId();

        // create barrier between texts and entry fields
        Barrier fieldBarrier = new Barrier(context);
        int fieldBarrierId = View.generateViewId();
        fieldBarrier.setId(fieldBarrierId);
        fieldBarrier.setDpMargin(12);
        fieldBarrier.setType(Barrier.RIGHT);
        layout.addView(fieldBarrier);

        // get barrier separating chart image from fields
        Barrier imageBarrier = (Barrier) layout.getViewById(R.id.imageView_barrier);

        //uiPairs.clear();
        ArrayList<Station> stations = aircraft.getStations();
        int numStations = aircraft.mAircraftType.numberOfStations();
        EditText editText = null;
        TextView textView;
        ConstraintLayout.LayoutParams lp;
        for (int i=0; i < numStations; i++) {
            int editId = View.generateViewId();
            editText = new EditText(context);
            editText.setId(editId);
            editText.setTag(i); // save station index for future reference
            editText.setEms(5);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.startToEnd = fieldBarrierId;
            if (i == 0)
                //lp.topToTop = layoutId;
                lp.topToBottom = R.id.aircraft;
            else
                lp.topToBottom = uiPairs.get(i-1).editField.getId();
            layout.addView(editText, lp);

            if (!portrait)
                imageBarrier.addView(editText); // barrier will be right of edit fields

            textView = new TextView(context);
            textView.setId(View.generateViewId());
            textView.setText(String.format(Locale.getDefault(), "%s (@ %.1f)",
                    stations.get(i).getName(),
                    stations.get(i).getArm()));
            lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.startToStart = layout.getId();
            lp.baselineToBaseline = editId;
            layout.addView(textView, lp);
            fieldBarrier.addView(textView);

            StationUIPair uiPair = new StationUIPair();
            uiPair.editField = editText;
            uiPair.label = textView;
            uiPairs.add(uiPair);
        }

        if (portrait) {
            // barrier will be below last edit field
            lp = (ConstraintLayout.LayoutParams) imageBarrier.getLayoutParams();
            lp.topToBottom = editText.getId(); // constrain image barrier to last edit field
            imageBarrier.setLayoutParams(lp);
        }

        addWatchers();
        Log.d(_LOG_TAG, "End setUI()");
    }

    /***
     * addWatchers
     *
     * Set up all observers and listeners
     */
    private void addWatchers() {
        Log.d(_LOG_TAG, "Begin addWatchers()");
        int numStations = aircraft.mAircraftType.numberOfStations();
        for (int i=0; i<numStations; i++) {
            Log.d(_LOG_TAG, String.format("addWatchers(): station index=%d", i));
            EditText editText = uiPairs.get(i).editField;

            // Create Observer to watch the chartViewModel data
            chartViewModel.getStationWeight(i).observe(getViewLifecycleOwner(), aDouble -> {
                // Notification: Model Data Changed
                Log.d("Observer", "onChanged() fired");

                String editTextString = editText.getText().toString();
                if (different(aDouble, editTextString)) { // if field text needs updating
                    char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                    String newEditTextString = aDouble.toString();
                    /*
                        Now decide if decimal portion should be stripped.
                        Most values will be int, by doing this we don't clutter up the UI
                        with a bunch of needless ".0"s.
                     */
                    boolean editTextHasDecimal = (editTextString.indexOf(separator) != -1);
                    if (!editTextHasDecimal) {
                        // if currently displayed text has decimal portion, leave it. Otherwise...
                        double fieldVal = editTextString.isEmpty() ? 0 : Double.parseDouble(editTextString);
                        boolean fieldValIsInt = fieldVal == (int) fieldVal;
                        boolean doubleValIsInt = aDouble == aDouble.intValue();
                        if (fieldValIsInt && doubleValIsInt) {
                            // strip decimal portion from new text
                            int sepIndex = newEditTextString.indexOf(separator);
                            if (sepIndex != -1) {
                                newEditTextString = newEditTextString.substring(0, sepIndex);
                            }
                        }
                    }
                    Log.d("Observer", String.format("setText(\"%s\")", newEditTextString));
                    editText.setText(newEditTextString);
                } // end: if (field text needs updating)
                wbChart.redraw();
            });

            // create Listener to watch the edit field
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d("afterTextChanged", String.format("editable='%s'",
                            editable));
                    View v = ChartFragment.this.layout.findFocus();
                    if (v != null) {
                        int stationIndex = (Integer) v.getTag();
                        Log.d("afterTextChanged", String.format("View focus at stationIndex=%d",
                                stationIndex));
                        double editedWeight;
                        try {
                            editedWeight = Double.parseDouble(editable.toString());
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                            editedWeight = 0; // if text cannot be parsed, set model weight to 0
                        }
                        chartViewModel.setStationWeight(stationIndex, editedWeight);
                    }
                }
            });
        }
        Log.d(_LOG_TAG, "End addWatchers()");
    }

    private void addChart()
    {
        Log.d(_LOG_TAG, "Begin addChart()");

        ImageView chartView = layout.findViewById(R.id.imageView_chart);

        int width = 1920;
        int height = (int) (width * 0.7);

        Bitmap chartBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas chartCanvas = new Canvas(chartBitmap);

        wbChart.draw(chartCanvas);

        chartView.setImageBitmap(chartBitmap);
        Log.d(_LOG_TAG, "End addChart()");
    }

    String getChartConfig()
    {
        //TODO: read from config file
        final String config2 =
                "{chart: {" +
                            "margin: 100," +
                            "tickHeight: 12," +
                            "tickWidth: 12," +
                            "xScaleIncrement: 1," +
                            "yScaleIncrement: 100" +
                        "}" +
                "}";
        return config2;
    }

    /***
     * different -- Safely compare double to String containing a double. Treat empty string as equal to 0.
     * Default to "different" if NumberFormatException occurs.
     *
     * @param d numeric value
     * @param numString String representation
     * @return true if different or can't parse numString
     */
    boolean different(double d, String numString) {
        boolean dif = true;
        if (d == 0 && numString.isEmpty())
            dif = false; // treat 0 and empty string as equal
        else {
            try {
                if (d == Double.parseDouble(numString))
                    dif = false;
            } catch (NumberFormatException e) { /* default to "different" */ }
        }
        return dif;
    }

}