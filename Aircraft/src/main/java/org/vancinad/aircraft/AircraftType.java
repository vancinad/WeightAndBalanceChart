package org.vancinad.aircraft;

import android.graphics.Color;
import android.util.JsonReader;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class AircraftType {
    static final String _LOG_TAG = "AircraftType";
    ArrayList<Station> mStations = null;
    ArrayList<CGEnvelope> mEnvelopes = null;
    String mTypeString = null; // file name from which type data was retrieved (minus ".json")
    String mTypeName = null; // Display name
    private boolean mIsApproved = false;

    AircraftType(File typeFile) {
        mStations = new ArrayList<>();
        mEnvelopes = new ArrayList<>();
        try {
            FileReader typeReader = new FileReader(typeFile);
            JsonReader jr = new JsonReader(typeReader);
            readType(jr); // load all instance data from .json file
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mStations != null && mEnvelopes !=null)
            if (mStations.size() > 0 && mEnvelopes.size() > 0)
                mIsApproved = true; // Type is approved!
    }

    void readType(JsonReader jr) throws IOException {
        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("name"))
                mTypeName = jr.nextString();
            else if (name.equals("typeId")) {
                mTypeString = jr.nextString();
            } else if (name.equals("stations"))
                readStations(jr);
            else if (name.equals("envelopes"))
                readEnvelopes(jr);
        }
        jr.endObject();
    }

    private void readEnvelopes(JsonReader jr) throws IOException {
        CGEnvelope envelope;

        jr.beginArray();
        while (jr.hasNext()) {
            envelope = readEnvelope(jr);
            if (envelope != null)
                mEnvelopes.add(envelope);
        }
        jr.endArray();
    }

    private CGEnvelope readEnvelope(JsonReader jr) throws IOException {
        CGEnvelope envelope = null;
        String envelopeName = null;
        int envelopeColor = Color.CYAN; //default color
        ArrayList<Vertex> vertices = null;

        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("name")) {
                envelopeName = jr.nextString();
            } else if (name.equals("color")) {
                String color = jr.nextString();
                Log.d(_LOG_TAG, "color="+color);
                envelopeColor = Color.parseColor(color);
            } else if (name.equals("vertices")) {
                vertices = readVertices(jr);
            }
        }
        jr.endObject();

        if (envelopeName != null && vertices != null) {
            envelope = new CGEnvelope(envelopeName, envelopeColor);
            for (Vertex v : vertices)
                envelope.addPoint(v.arm, v.weight);
        }
        return envelope;
    }

    private ArrayList<Vertex> readVertices(JsonReader jr) throws IOException {
        // return list of vertices or null if none were read
        ArrayList<Vertex> vertices = new ArrayList<>();

        jr.beginArray();
        while (jr.hasNext()) {
            Vertex vertex = readVertex(jr);
            if (vertex != null)
                vertices.add(vertex);
        }
        jr.endArray();

        if (vertices.size() > 0)
            return vertices;
        else
            return null;
    }

    private Vertex readVertex(JsonReader jr) throws IOException {
        // return Vertex or null
        double cg = Double.MIN_VALUE;
        double weight = Double.MIN_VALUE;

        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("cg"))
                cg = jr.nextDouble();
            if (name.equals("weight"))
                weight = jr.nextDouble();
        }
        jr.endObject();

        if (cg != Double.MIN_VALUE && cg != Double.MIN_VALUE)
            return new Vertex(weight, cg);
        else
            return null;
    }

    private void readStations(JsonReader jr) throws IOException {
        jr.beginArray();
        while (jr.hasNext()) {
            double arm = Double.MIN_VALUE;
            String stationName = null;
            jr.beginObject();
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("arm"))
                    arm = jr.nextDouble();
                else if (name.equals("name"))
                    stationName = jr.nextString();
            }
            jr.endObject();

            if (arm != Double.MIN_VALUE && stationName != null) {
                Station station = new Station(arm, stationName);
                mStations.add(station);
            }
        }
        jr.endArray();
    }

    public int numberOfStations() { return mStations.size(); }
    public boolean isApproved() {return mIsApproved;}

}

