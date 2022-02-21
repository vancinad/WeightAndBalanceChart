package org.vancinad.aircraft;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class Util {
    static boolean isUsableDir(File dir) {
        // make sure given File is readable, writable, and a directory
        final String _LOG_TAG = "isUsableDir";

        boolean dirIsValid = true;

        if (!dir.canRead()) {
            Log.d(_LOG_TAG, String.format("%s is not readable", dir));
            dirIsValid = false;
        }
        if (!dir.canWrite()) {
            Log.d(_LOG_TAG, String.format("%s is not writable", dir));
            dirIsValid = false;
        }
        if (!dir.isDirectory()) {
            Log.d(_LOG_TAG, String.format("%s is not a directory", dir));
            dirIsValid = false;
        }
        return dirIsValid;
    }

    static File getDataDir(String dirName, Context context) {
        File newDir = new File(context.getFilesDir(), dirName);
        newDir.mkdirs();
        return (Util.isUsableDir(newDir)) ? newDir : null;
    }
}
