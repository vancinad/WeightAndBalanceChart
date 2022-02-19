package org.vancinad.aircraft;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class AircraftType {
    static final String _LOG_TAG = "AircraftType";
    ArrayList<Station> mStations = null;
    ArrayList<CGEnvelope> mEnvelopes = null;
    String mTypeString = null; // file name from which type data was retrieved (minus ".json")
    String mTypeName = null; // Display name
    //private boolean mIsApproved = false;

    public String getTypeName() { return mTypeName; }

    AircraftType(JsonReader jr) throws IOException {
        readType(jr);
    }

    AircraftType(File typeFile) throws IOException {
        FileReader typeReader = new FileReader(typeFile);
        JsonReader jr = new JsonReader(typeReader);
        readType(jr); // load all instance data from .json file
    }

    /***
     *  Write instance data
     *
     * @param jw AircraftType data will be written to this JsonWriter
     */
    public void write(JsonWriter jw) throws IOException {
        jw.beginObject();
        jw.name("name").value(mTypeName);
        jw.name("typeId").value(mTypeString);
        writeStationsObject(jw);
        writeEnvelopesObject(jw);
        jw.endObject();
        jw.flush();
    }

    private void writeEnvelopesObject(JsonWriter jw) throws IOException {
        jw.name("envelopes");
        writeEnvelopesArray(jw);
    }

    private void writeEnvelopesArray(JsonWriter jw) throws IOException {
        jw.beginArray();
        for (CGEnvelope envelope : mEnvelopes) {
            jw.beginObject();
            jw.name("name").value(envelope.mName);
            jw.name("color").value(envelope.mColorString);
            jw.name("vertices");
            writeVerticesArray(jw, envelope);
            jw.endObject();
        }
        jw.endArray();
    }

    private void writeVerticesArray(JsonWriter jw, CGEnvelope envelope) throws IOException {
        jw.beginArray();
        Iterator<Vertex> vi = envelope.verticesIterator();
        while (vi.hasNext()) {
            Vertex v = vi.next();
            jw.beginObject();
            jw.name("cg").value(v.arm);
            jw.name("weight").value(v.weight);
            jw.endObject();
        }
        jw.endArray();
    }

    void writeStationsObject(JsonWriter jw) throws IOException {
        jw.name("stations");
        writeStationsArray(jw);
    }

    private void writeStationsArray(JsonWriter jw) throws IOException {
        jw.beginArray();
        for (Station station : mStations) {
            jw.beginObject();
            jw.name("arm").value(station.arm);
            jw.name("name").value(station.name);
            jw.endObject();
        }
        jw.endArray();
    }

    /***
     * readType -- Initialize instance data members from JSON
     * @param jr JsonReader from which data will be retrieved
     * @throws IOException JsonReader error occurred
     */
    void readType(JsonReader jr) throws IOException {
        mStations = new ArrayList<>();
        mEnvelopes = new ArrayList<>();

        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            switch (name) {
                case "name":
                    mTypeName = jr.nextString();
                    break;
                case "typeId":
                    mTypeString = jr.nextString();
                    break;
                case "stations":
                    readStations(jr);
                    break;
                case "envelopes":
                    readEnvelopes(jr);
                    break;
            }
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
        String envelopeColorString = null;
//        int envelopeColor = Color.CYAN; //default color
        ArrayList<Vertex> vertices = null;

        jr.beginObject();
        while (jr.hasNext()) {
            String name = jr.nextName();
            switch (name) {
                case "name":
                    envelopeName = jr.nextString();
                    break;
                case "color":
                    envelopeColorString = jr.nextString();
                    Log.d(_LOG_TAG, "color=" + envelopeColorString);
                    break;
                case "vertices":
                    vertices = readVertices(jr);
                    break;
            }
        }
        jr.endObject();

        if (envelopeName != null && vertices != null) {
            envelope = new CGEnvelope(envelopeName, envelopeColorString);
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

    public boolean isApproved() {
        boolean isApproved = false;

        if (mStations != null && mEnvelopes !=null)
            if (mStations.size() > 0 && mEnvelopes.size() > 0)
                isApproved = true; // Type is approved!

        return isApproved;
    }

}

