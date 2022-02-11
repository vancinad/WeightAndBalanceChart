package org.vancinad.aircraft;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 *  AircraftTypeFactory -- handles filesystem interactions and
 */
public class AircraftTypeFactory {
    static final String _LOG_TAG = "AircraftTypeFactory";

    //TODO: "Do not place Android context classes in static fields; this is a memory leak."
    static AircraftTypeFactory factoryInstance = null;
    static Context mContext = null;

    static File mTypeFilesDir = null;

    /***
     *
     * @return The AircraftTypeFactory singleton or null if mContext has not been set
     */
    public static AircraftTypeFactory getInstance() {
        return (mContext != null) ? getInstance(mContext) : null;
    }

    /***
     *
     * @param context Application context
     *
     * @return AircraftTypeFactory singleton
     */
    public static AircraftTypeFactory getInstance(Context context)
    {
        if (factoryInstance == null)
            factoryInstance = new AircraftTypeFactory();

        start(context);

        return factoryInstance;
    }

    /***
     *
     * @param context Owning context.
     */
    public static boolean start(Context context) {
        mContext = context;
        return setFileDirs(); // setup paths for required files
    }

    static boolean setFileDirs() {
        boolean dirIsValid;
        File baseDir = mContext.getFilesDir();
        File typesDir = new File(baseDir, mContext.getString(R.string.dir_types));
        typesDir.mkdirs();

        dirIsValid = isUsableDir(typesDir);

        if (dirIsValid) {
            mTypeFilesDir = typesDir;
            dirIsValid = initTypeFilesDir();
        }

        return dirIsValid;
    }

    private static boolean initTypeFilesDir() {
        // make sure types dir has at least one file in it
        File[] filesList = mTypeFilesDir.listFiles();
        boolean dirIsValid = filesList!=null && filesList.length > 0; //if files found, assume dir is OK
        if (filesList!=null && filesList.length == 0) // dir is OK but is empty
        {
            //if no files found, try creating one default type file
            Resources r = mContext.getResources();
            InputStreamReader isr = new InputStreamReader(r.openRawResource(R.raw.default_aircraft_type_file));
            File newFile = new File(mTypeFilesDir, mContext.getString(R.string.default_aircraft_type_id)+".json");
            try {
//                FileOutputStream newFOS = mContext.openFileOutput(newFile.toString(), 0);
                FileOutputStream newFOS = new FileOutputStream(newFile);
                //read all from input and write to output
                int c = isr.read();  // this would be faster if reading into byte[], but whatever...
                while (c != -1) {
                    newFOS.write(c);
                    c = isr.read();
                }
                newFOS.close();
                isr.close();
                dirIsValid = newFile.exists() && newFile.canRead();
            } catch (IOException e) {
                dirIsValid = false;
                e.printStackTrace();
            }
        }
        return dirIsValid;
    }

    private static boolean isUsableDir(File dir) {
        // make sure typesDir is readable, writable, and a directory
        boolean dirIsValid = true;

        if (!dir.canRead()) {
            Log.d(_LOG_TAG, String.format("%s is not readable", dir.toString()));
            dirIsValid = false;
        }
        if (!dir.canWrite()) {
            Log.d(_LOG_TAG, String.format("%s is not writable", dir.toString()));
            dirIsValid = false;
        }
        if (!dir.isDirectory()) {
            Log.d(_LOG_TAG, String.format("%s is not a directory", dir.toString()));
            dirIsValid = false;
        }
        return dirIsValid;
    }

    public AircraftType getType(String typeString) {
        AircraftType newType = null;

        File typeFile = getTypeFile(typeString);
        if (typeFile != null)
            newType = new AircraftType(typeFile);

        if (newType != null)
            if (!newType.isApproved())
                newType = null;

        return newType;
    }

    File getTypeFile(String typeString) {
        File typeFile = null;
        Log.d(_LOG_TAG, String.format("getTypeFile(...): typesFilesDir='%s' isDirectory=%b canRead=%b canWrite=%b",mTypeFilesDir.toString(), mTypeFilesDir.isDirectory(), mTypeFilesDir.canRead(), mTypeFilesDir.canWrite()));
        File[] typesFiles = mTypeFilesDir.listFiles();
        assert typesFiles != null;
        Log.d(_LOG_TAG, typesFiles.length + " files found");
        String name = null;
        int i;
        for (i=0; i<typesFiles.length; i++) {
            name = typesFiles[i].getName();
            if (name.equals(typeString+".json"))
                break;
        }
        if (i < typesFiles.length) {
            // corresponding type file was found
            typeFile = typesFiles[i];
        }
        return typeFile;
    }
    /***
     *  Private constructor. Use AircraftTypeFactory.getInstance(...)
     */
    private AircraftTypeFactory() {}

}
