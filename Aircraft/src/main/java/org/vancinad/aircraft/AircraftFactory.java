package org.vancinad.aircraft;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

public class AircraftFactory {
    static AircraftFactory mInstance = null; //
    Context mContext;
    File mAircraftDir; // directory containing aircraft definitions

    private AircraftFactory() {} // private constructor
    public static AircraftFactory getInstance() {
        if (mInstance == null)
            mInstance = new AircraftFactory();
        return mInstance;
    }

    /***
     * Start AircraftFactory and AircraftTypeFactory
     *
     * @param c Application Context
     * @return true if startup was successful, false if not
     */
    public boolean start(Context c) {
        mContext = c;
        mAircraftDir = Util.getDataDir(mContext.getString(R.string.dir_aircraft), mContext);
        boolean typeFactoryStarted = AircraftTypeFactory.getInstance().start(mContext);  // initialize factory singleton

        return (mAircraftDir != null && typeFactoryStarted);
    }

    public File[] getAircraftFiles() {
        return mAircraftDir.listFiles();
    }

    public String[] getAircraftTailNumbers () {
        File[] aircraftFiles = getAircraftFiles();
        ArrayList<String> tails = new ArrayList<>();
        for (File f : aircraftFiles) {
//            Aircraft a = new Aircraft(f);
//            if (a.isApproved())
//                tails.add(a.getTailNumber());
            tails.add(getTailNumber(f));
        }
        return (String[]) tails.toArray();
    }

    public File getFileFor(Aircraft a) {
        return new File(mAircraftDir, a.mTailNumber+".json");
    }

    public File getFileFor(String tailNumber) {
        return new File(mAircraftDir, tailNumber+".json");
    }

    String getTailNumber(File f) {
        String fileName = f.getName();
        int ji = fileName.indexOf(".json");
        return fileName.substring(0, ji);
    }
}
