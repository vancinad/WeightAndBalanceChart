package org.vancinad.wbchart.aircraft;

import java.util.ArrayList;
import java.util.Iterator;

public class CGEnvelope {
    ArrayList<Vertex> mVertices;
//    ArrayList<Float> pts; //an array of cg/weight pairs
    String mName;
    int mColor;
    double mMinWeight, mMaxWeight; // highest and lowest weight
    double mMinCG, mMaxCG; // highest and lowest CG arm

    public CGEnvelope(String mName, int mColor)
    {
        mVertices = new ArrayList<>();
        this.mName = mName;
        this.mColor = mColor;

        // make sure the first point added will become new max/min
        mMaxCG = Double.MIN_VALUE;
        mMinCG = Double.MAX_VALUE;
        mMaxWeight = Double.MIN_VALUE;
        mMinWeight = Double.MAX_VALUE;
    }

    public CGEnvelope addPoint(double cg, double weight)
    {
        Vertex v = new Vertex(weight, cg);
        mVertices.add(v);

        if (cg > mMaxCG) mMaxCG = cg;
        if (cg < mMinCG) mMinCG = cg;

        if (weight > mMaxWeight) mMaxWeight = weight;
        if (weight < mMinWeight) mMinWeight = weight;

        return this;
    }

    public double getMinCG() { return mMinCG; }
    public double getMinWeight() { return mMinWeight; }
    public double getMaxCG() { return mMaxCG; }
    public double getMaxWeight() { return mMaxWeight; }


    public Iterator<Vertex> verticesIterator()
    {
        return mVertices.iterator();
    }

    public int getColor() { return mColor; }
}


