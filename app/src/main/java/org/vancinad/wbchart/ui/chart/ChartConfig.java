package org.vancinad.wbchart.ui.chart;

import android.content.Context;
import android.graphics.Color;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class ChartConfig implements Serializable {
    static final String _CONFIG_FILE_NAME = "config.json";
    private static final String _LOG_TAG = "ChartConfig";

    File mConfigFile;

    // default values
    public int mMargin = 100;
    public int mTickHeight = 12;
    public int mTickWidth = 12;
    public int mXScaleIncrement = 1;
    public int mYScaleIncrement = 100;
    public int mChartBackgroundColor = Color.BLUE;
    public int mChartLinesColor = Color.WHITE;
    public String mMostRecentTailNumber = null;

    ChartConfig(Context context) throws IOException {
        mConfigFile = new File(context.getFilesDir(), _CONFIG_FILE_NAME);
//        String configString =
//                "{chart: {" +
//                        "margin: 100," +
//                        "tickHeight: 12," +
//                        "tickWidth: 12," +
//                        "xScaleIncrement: 1," +
//                        "yScaleIncrement: 100" +
//                        "}" +
//                        "}";

        if (mConfigFile.exists())
            read();
    }

    /***
     * read instance data from file
     *
     * @throws IOException
     */
    private void read() throws IOException {
        Log.d(_LOG_TAG, "Reading " + mConfigFile.getName());
        JsonReader jr = new JsonReader(new FileReader(mConfigFile));
        read(jr);
    }

    private void read(JsonReader jr) throws IOException {
        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            Log.d(_LOG_TAG, "Read "+name);
            switch (name) {
                case "chart":
                    readChartObject(jr);
                    break;
                case "tailNumber":
                    mMostRecentTailNumber = jr.nextString();
                    break;
            }
        }
    }

    private void readChartObject(JsonReader jr) throws IOException {
        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            Log.d(_LOG_TAG, "Read "+name);
            switch (name) {
                case "margin":
                    mMargin = jr.nextInt();
                    break;
                case "tickHeight":
                    mTickHeight = jr.nextInt();
                    break;
                case "tickWidth":
                    mTickWidth = jr.nextInt();
                    break;
                case "xScaleIncrement":
                    mXScaleIncrement = jr.nextInt();
                    break;
                case "yScaleIncrement":
                    mYScaleIncrement = jr.nextInt();
                    break;
                case "chartBackgroundColor":
                    mChartBackgroundColor = jr.nextInt();
                    break;
                case "chartLinesColor":
                    mChartLinesColor = jr.nextInt();
                    break;
            }
        }
        jr.endObject();
    }

    public Exception write() {
        Log.d(_LOG_TAG, "Writing " + mConfigFile.getName());

        Exception retEx = null;

        try {
            JsonWriter jw = new JsonWriter(new FileWriter(mConfigFile));
            jw.beginObject();
            writeChartObject(jw);
            jw.name("tailNumber").value(mMostRecentTailNumber);
            jw.endObject();
            jw.close();
        } catch (IOException e) {
            retEx = e;
            e.printStackTrace();
        }

        return retEx;
    }

    private void writeChartObject(JsonWriter jw) throws IOException {
        jw.name("chart");
        jw.beginObject();
        jw.name("margin").value(mMargin);
        jw.name("tickHeight").value(mTickHeight);
        jw.name("tickWidth").value(mTickWidth);
        jw.name("xScaleIncrement").value(mXScaleIncrement);
        jw.name("yScaleIncrement").value(mYScaleIncrement);
        jw.name("chartBackgroundColor").value(mChartBackgroundColor);
        jw.name("chartLinesColor").value(mChartLinesColor);
        jw.endObject();
    }
}
