package com.netmontools.lookatnet.utils;

import android.util.Log;

import com.netmontools.lookatnet.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Mansur on 03.08.2017.
 */

public final class LogSystem {
    private static boolean mLoggingEnabled = true;
    private static final String FILENAME = "log.dat";

    private LogSystem() {

    }

    public static void setDebugLogging(boolean enabled) {
        mLoggingEnabled = enabled;
    }

    public static int logInFile(String tag, String msg)
    {
        int result = 0;
        File file = new File(App.instance.getFilesDir(), FILENAME);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            String timeLog = new SimpleDateFormat("dd.MM.yy hh:mm:ss").format(new Date());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(timeLog+" (" + tag + ")\t" + msg + "\n");
            bw.close();
            result = 1;

            //MainActivity.veryLongString.append(timeLog+" (" + tag + ")\t" + msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void clearLog() {
        File file = new File(App.instance.getFilesDir(), FILENAME);
        try {
            if (file.exists()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
                bw.append("");
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int v(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.v(tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(tag, msg);
        }
        return result;
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.d(tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.i(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.w(tag, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        if (mLoggingEnabled) {
            result = Log.e(tag, msg, tr);
        }
        return result;
    }
}

