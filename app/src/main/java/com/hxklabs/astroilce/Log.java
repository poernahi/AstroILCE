package com.hxklabs.astroilce;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    // There seems to be a limitation on the number of characters or format of the file name
    public static final String LOG_FILE_NAME = "ASTRO.TXT";
    public static final String TAG = "ASTRO_ILCE";

    protected static void log(String msg) {
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_NAME);
            logFile.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(msg);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            android.util.Log.e(TAG, "Unable to write log file", e);
        }
    }

    protected static void w(String msg) {
        log("[WARN] " + msg);
        android.util.Log.w(TAG, msg);
    }

    protected static void i(String msg) {
        log("[INFO] " + msg);
        android.util.Log.i(TAG, msg);
    }

    protected static void d(String msg) {
        log("[DEBUG] " + msg);
        android.util.Log.d(TAG, msg);
    }

    protected static void e(String msg) {
        log("[ERROR] " + msg);
        android.util.Log.e(TAG, msg);
    }

    protected static void e(String msg, Throwable t) {
        android.util.Log.e(TAG, msg, t);
        StringWriter s = new StringWriter();
        PrintWriter writer = new PrintWriter(s);
        t.printStackTrace(writer);
        log("[ERROR] " + msg + "\n" + s.toString());
        writer.close();
    }
}
