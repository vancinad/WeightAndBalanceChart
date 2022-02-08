package org.vancinad.wbchart.ui.chart;

import android.content.Context;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.vancinad.wbchart.R;
import org.vancinad.wbchart.aircraft.Aircraft;
import org.vancinad.wbchart.aircraft.Station;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class ChartFragment extends Fragment {

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
        Log.d("ChartFragment", "Begin onCreateView()");
        layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_chart, container, false);
        chartViewModel = new ViewModelProvider(this.getActivity()).get(ChartViewModel.class);
        aircraft = Aircraft.createNew("3A12-172N", "N734BG", 1436.2, 39.26);
        /* TODO: Handle negative case */ assert aircraft != null && aircraft.isApproved();
        //test_setAircraftStationWeights(); //TODO: Remove after testing complete
        chartViewModel.setAircraft(aircraft, (chartViewModel.numberOfStations() == 0)); // if chartViewModel is uninitialized, load stations from aircraft's data
/*
        if (chartViewModel.numberOfStations() == 0) {
            // Model not initialized. Load stations and weights from aircraft
            chartViewModel.setAircraft(aircraft, true);
        } else {
            // Model has data. Associate new aircraft and load aircraft from model
            chartViewModel.setAircraft(aircraft, false);
        }
*/
        wbChart = new WBChart(getChartConfig(), aircraft);

        Log.d("ChartFragment", "End onCreateView()");
        return layout;
    }

    @Override
    public void onResume() {
        Log.d("ChartFragment", "Begin onResume()");
        super.onResume();
        setUI();
        addChart();
        Log.d("ChartFragment", "End onResume()");
    }

    private void test_setAircraftStationWeights() {
        Log.d("ChartFragment", "Begin test_setAircraftStationWeights()");
        double[] weights = {240.0, 380, 100, 50, 20};
        for (int i=0; i<weights.length; i++)
            aircraft.setStationWeight(i, weights[i]);
        Log.d("ChartFragment", "End test_setAircraftStationWeights()");
    }

    /***
     * Create text and edit fields for each aircraft station.
     * Vertical barrier separates texts on left from edits on right.
     * Texts align baseline to baseline with associated edit field.
     *
     */
    private void setUI() {
        Log.d("ChartFragment", "Begin setUI()");
        uiPairs = new ArrayList<>(chartViewModel.numberOfStations());
        Context context = layout.getContext();
        int layoutId = layout.getId();

        Barrier fieldBarrier = new Barrier(context);
        int fieldBarrierId = View.generateViewId();
        fieldBarrier.setId(fieldBarrierId);
        fieldBarrier.setDpMargin(12);
        fieldBarrier.setType(Barrier.RIGHT);
        layout.addView(fieldBarrier);

        uiPairs.clear();
        ArrayList<Station> stations = aircraft.getStations();
        int numStations = aircraft.mAircraftType.numberOfStations();
        EditText editText = null;
        TextView textView;
        ConstraintLayout.LayoutParams lp;
        for (int i=0; i < numStations; i++) {
            int editId = View.generateViewId();
            editText = new EditText(context);
            editText.setId(editId);
            editText.setEms(5);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.startToEnd = fieldBarrierId;
            if (i == 0)
                lp.topToTop = layoutId;
            else
                lp.topToBottom = uiPairs.get(i-1).editField.getId();
            layout.addView(editText, lp);

            textView = new TextView(context);
            textView.setId(View.generateViewId());
            String text = String.format("%s (%.1f)", stations.get(i).getName(), stations.get(i).getArm());
            textView.setText(text);
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
        Barrier imageBarrier = (Barrier) layout.getViewById(R.id.imageView_barrier);
        lp = (ConstraintLayout.LayoutParams) imageBarrier.getLayoutParams();
        lp.topToBottom = editText.getId(); // constrain image barrier to last edit field
        imageBarrier.setLayoutParams(lp);

        addWatchers();
        Log.d("ChartFragment", "End setUI()");
    }

    /***
     * addWatchers
     *
     * Set up all observers and listeners
     */
    private void addWatchers() {
        Log.d("ChartFragment", "Begin addWatchers()");
        int numStations = aircraft.mAircraftType.numberOfStations();
        for (int i=0; i<numStations; i++) {
            Log.d("ChartFragment", String.format("addWatchers(): station index=%d", i));
            EditText editText = uiPairs.get(i).editField;

            // Create Observer to watch the chartViewModel data
            chartViewModel.getStationWeight(i).observe(getViewLifecycleOwner(), new Observer<Double>() {
                @Override
                public void onChanged(Double aDouble) {
                    // Notification: Model Data Changed
                    String editTextString = editText.getText().toString();
                    Log.d("Observer", "onChanged() fired");
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
                    wbChart.redraw(); //TODO: figure out sequence problem here. PlotCG not updating correctly.
                }
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
                    View v = ChartFragment.this.layout.findFocus();
                    Log.d("afterTextChanged", String.format("Editable='%s', v=%s",
                            editable.toString(),
                            (v==null) ? "null" : ((EditText)v).getText() ) );
                    if(v != null) {
                        int i = 0;
                        for (StationUIPair p : uiPairs) {
                            if (p.editField.equals(v)) {
                                Log.d("afterTextChanged",
                                        String.format("Found corresponding view at i=%d", i));
                                break;
                            }
                            i++;
                        }
                        double d;
                        try {
                            d = Double.parseDouble(editable.toString());
                        } catch (NumberFormatException e) {
                            d = 0;
                        }
                        chartViewModel.setStationWeight(i, d);
                    }
                }
            });
        }
        Log.d("ChartFragment", "End addWatchers()");
    }

    private void addChart()
    {
        Log.d("ChartFragment", "Begin addChart()");

        ImageView chartView = layout.findViewById(R.id.imageView_chart);

        int width = 1920;
        int height = (int) (width * 0.7);

        Bitmap chartBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas chartCanvas = new Canvas(chartBitmap);

        wbChart.draw(chartCanvas);

        chartView.setImageBitmap(chartBitmap);
        Log.d("ChartFragment", "End addChart()");
    }

    String getChartConfig()
    {
/*
        String config =
                "{envelope: {" +
                        "minGW: 1500," +
                        "maxGW: 2400," +
                        "minCG: 34," +
                        "maxCG: 48" +
                        "}," +
                        "chart: {" +
                        "margin: 100," +
                        "tickHeight: 12," +
                        "tickWidth: 12," +
                        "xScaleIncrement: 1," +
                        "yScaleIncrement: 100" +
                        "}" +
                        "}";
*/

        // config2 omits envelope
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
     * @param d
     * @param numString
     * @return
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