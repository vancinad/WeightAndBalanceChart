package org.vancinad.wbchart.ui.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import org.json.JSONObject;
import org.vancinad.aircraft.Aircraft;
import org.vancinad.aircraft.CGEnvelope;
import org.vancinad.aircraft.Vertex;

import java.util.Iterator;
import java.util.Locale;

public class WBChart {
    //set initial values to make sure these get set when we load CG envelopes
    double minGW = Double.MAX_VALUE, maxGW = Double.MIN_VALUE;
    double minCG = Double.MAX_VALUE, maxCG = Double.MIN_VALUE;

    int mMargin; // = 50;
    int mTickHeight; // = margin / 4;
    int mTickWidth; // = margin / 4;
    int mXScaleIncrement; // = 1;
    int mYScaleIncrement; // = 100;
    int mChartBackgroundColor = Color.BLUE;
    int mChartLinesColor = Color.WHITE;

    Canvas mCanvas;
    Rect mChartRect = null;
    Aircraft mAircraft;


    public WBChart(String config, Aircraft aircraft) {
        try {
            setConfig(config); // TODO: Read json config from file
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAircraft = aircraft;
        setChartRange();

    }

    private void setConfig(String config) throws Exception {

        JSONObject o = new JSONObject(config); // TODO: Is there a way to hydrate Java object from JSONObject?
        JSONObject oChart = (JSONObject) o.get("chart");

        mMargin = oChart.getInt("margin");
        mTickHeight = oChart.getInt("tickHeight");
        mTickWidth = oChart.getInt("tickWidth");
        mXScaleIncrement = oChart.getInt("xScaleIncrement");
        mYScaleIncrement = oChart.getInt("yScaleIncrement");
        mChartBackgroundColor = Color.BLUE;
        mChartLinesColor = Color.WHITE;

    }

    /***
     *  Find the upper and lower chart values for weight and CG
     */
    private void setChartRange() {
        for (CGEnvelope e : mAircraft.getEnvelopes()) {
            if (e.getMaxCG() > maxCG)
                maxCG = scaleMax(e.getMaxCG(), mXScaleIncrement);
            if (e.getMaxWeight() > maxGW)
                maxGW = scaleMax(e.getMaxWeight(), mYScaleIncrement);
            if (e.getMinWeight() < minGW)
                minGW = e.getMinWeight();
            if (e.getMaxCG() > maxCG)
                maxCG = scaleMax(e.getMaxCG(), mXScaleIncrement);
            if (e.getMinCG() < minCG)
                minCG = scaleMin(e.getMinCG(), mXScaleIncrement);
        }
    }

    static int scaleMax(double v, int increment)
    {
        return ((int) (v / increment + 1)) * increment;
    }

    static int scaleMin(double v, int increment)
    {
        return ((int) (v / increment - 0.2)) * increment;
    }

    Point toXY(double cgVal, double weightVal, Rect r)
    {
        double x = r.left + ((cgVal-minCG) / (maxCG-minCG) * (r.right - r.left));
        double y = r.bottom - ((weightVal-minGW) / (maxGW - minGW) * (r.bottom - r.top));
        return new Point((int)x,(int)y);
    }

    public void redraw() {
        Log.d("redraw()", String.format("canvas=%b", mCanvas));
        if(mCanvas != null) {
            draw(mCanvas);
        }
    }

    public void draw(Canvas myCanvas) {
        this.mCanvas = myCanvas;
        Paint myPaint = new Paint();

        mChartRect = new Rect();
        mChartRect.left = mMargin * 2;
        mChartRect.right = myCanvas.getWidth() - mMargin;
        mChartRect.top = mMargin;
        mChartRect.bottom = myCanvas.getHeight() - mMargin;

        myCanvas.drawColor(mChartBackgroundColor);
        myPaint.setColor(mChartLinesColor);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setTextSize(mMargin / 3);
        myCanvas.drawRect(mChartRect, myPaint);

        myPaint.setStyle(Paint.Style.FILL);
        int textY = mChartRect.bottom+ mTickHeight - myPaint.getFontMetricsInt().top;
        int xScaleWidth = (int)(this.maxCG - this.minCG);
        Rect boundingRect = new Rect();
        int i = 0;
        do
        {
            //int tickExtraHeight = (x % 2 > 0) ? 0 : tickHeight;
            String label = String.valueOf(this.minCG + i);
            myPaint.getTextBounds(label, 0, label.length(), boundingRect);
            int tickX = (int) (mChartRect.left + ((float)i/xScaleWidth) * (mChartRect.right - mChartRect.left));
            myCanvas.drawLine(tickX, mChartRect.bottom, tickX, mChartRect.bottom + mTickHeight, myPaint);
            myCanvas.drawText(label, tickX - boundingRect.right / 2, textY, myPaint);
            //always draw rightmost tick mark
            if (i == xScaleWidth)
                break;
            else {
                i += mXScaleIncrement;
                if (i > xScaleWidth)
                    i = xScaleWidth;
            }
        } while (true);

        int yScaleWidth = (int)(this.maxGW - this.minGW);

        i = 0;
        do
        {
            String label = String.valueOf((int)(this.minGW + i));
            myPaint.getTextBounds(label, 0, label.length(), boundingRect);
            int tickY = (int) (mChartRect.bottom - ((float)i/yScaleWidth) * (mChartRect.bottom - mChartRect.top));
            int textX = mChartRect.left - mTickWidth - (int) myPaint.measureText(label,0,label.length());
            textY = tickY - boundingRect.top / 2;
            myCanvas.drawLine(mChartRect.left, tickY, mChartRect.left - mTickWidth, tickY, myPaint);
            myCanvas.drawText(label, textX, textY, myPaint);
            //always draw rightmost tick mark
            if (i == yScaleWidth)
                break;
            else {
                i += mYScaleIncrement;
                if (i > yScaleWidth)
                    i = yScaleWidth;
            }
        } while (true);

        Path path = new Path();
        for (CGEnvelope e : mAircraft.getEnvelopes())
        {
            path.reset();
            Iterator<Vertex> it = e.verticesIterator();
            while (it.hasNext())
            {
                Vertex v = it.next();
                double cgVal = v.arm;
                double weightVal = v.weight;
//                    float x = r.left + ((cgVal-minCG) / (maxCG-minCG) * (r.right - r.left));
//                    float y = r.bottom - ((weightVal-minGW) / (maxGW - minGW) * (r.bottom - r.top));
                Point pt = toXY(cgVal, weightVal, mChartRect);
                if (path.isEmpty())
                    path.moveTo(pt.x, pt.y);
                else
                    path.lineTo(pt.x, pt.y);
            }
            path.close();
            myPaint.setColor(e.getColor());
            myCanvas.drawPath(path, myPaint);
        }

        plotCG();

    } // draw()

    void plotCG() {
        double plotWeight = mAircraft.getGrossWeight();
        double plotCG = mAircraft.getCG();
        Log.d("plotCG", String.format("Given: weight=%f, cg=%f, canvas=%b", plotWeight, plotCG, mCanvas));
        // do nothing if no canvas
        if (mCanvas != null) {
            // adjust weight and cg to something we can plot
            if (plotWeight < minGW) plotWeight = minGW;
            else if (plotWeight > maxGW) plotWeight = maxGW;
            if (plotCG < minCG) plotCG = minCG;
            else if (plotCG > maxCG) plotCG = maxCG;

            Point plotPt = toXY(plotCG, plotWeight, mChartRect);
            Paint paint = new Paint();
            paint.setColor(Color.RED); // dot color
            final int radius = mTickHeight; // use tick height as dot radius
            Log.d("plotCG", String.format("Drawing weight=%f, cg=%f, x=%d, y=%d", plotWeight, plotCG, plotPt.x, plotPt.y));
            mCanvas.drawCircle(plotPt.x, plotPt.y, radius, paint);

            //draw plot label
            //position toward center of the chart relative to plot point
            String plotLabel = String.format(Locale.getDefault(), "(%.1f, %.2f)", mAircraft.getGrossWeight(), mAircraft.getCG());
            final int o = (int) (radius * 1.5); // label offset in pixels
            Rect labelBounds = new Rect();
            paint.setTextSize(mMargin / 3);

            paint.getTextBounds(plotLabel, 0, plotLabel.length(), labelBounds); // measure the label text

            // determine x and y (bottom-left) for the plot label
            float labelX = (plotPt.x < mChartRect.centerX()) ?  // if pt is left of center...
                plotPt.x + o : // ...put label to right
                plotPt.x - labelBounds.width() - o; // else put label to left
            float labelY = (plotPt.y < mChartRect.centerY()) ? // if pt is above center...
                plotPt.y + labelBounds.height() + o : // put label below
                plotPt.y - o; // else put label above

            int bgMargin = 20;
            RectF labelBackground = new RectF(labelBounds.left, labelBounds.top - bgMargin, labelBounds.right + bgMargin, labelBounds.bottom);
            labelBackground.offsetTo((int)labelX, (int)labelY - labelBounds.height() - bgMargin/2);
            paint.setColor(Color.DKGRAY);
            mCanvas.drawRoundRect(labelBackground, radius, radius, paint);

            paint.setColor(mChartLinesColor);
            mCanvas.drawText(plotLabel, labelX + bgMargin/2, labelY - bgMargin/2, paint);
        }
    }

} // class WBChart

