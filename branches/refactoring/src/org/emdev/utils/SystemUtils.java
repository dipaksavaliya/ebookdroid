package org.emdev.utils;

import org.ebookdroid.EBookDroidApp;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

public class SystemUtils {

    private static final String SYS_UI_CLS = "com.android.systemui.SystemUIService";
    private static final String SYS_UI_PKG = "com.android.systemui";
    private static final ComponentName SYS_UI = new ComponentName(SYS_UI_PKG, SYS_UI_CLS);

    private static final String SU_PATH1 = "/system/bin/su";
    private static final String SU_PATH2 = "/system/xbin/su";
    private static final String AM_PATH = "/system/bin/am";

    private SystemUtils() {
    }

    public static boolean isSystemUIRunning() {
        final ActivityManager actvityManager = (ActivityManager) EBookDroidApp.context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> rsiList = actvityManager.getRunningServices(1000);

        for (final RunningServiceInfo rsi : rsiList) {
            if (SYS_UI.equals(rsi.service)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startSystemUI() {
        if (isSystemUIRunning()) {
            return true;
        }
        exec(new String[] { AM_PATH, "startservice", "-n", "com.android.systemui/.SystemUIService" });
        return true;
    }

    public static boolean stopSystemUI() {
        if (!isSystemUIRunning()) {
            return true;
        }

        final String su = getSuPath();
        if (su == null) {
            return false;
        } else {
            exec(new String[] { su, "-c", "service call activity 79 s16 com.android.systemui" });
        }
        return true;
    }

    public static boolean setSystemUIVisible(final boolean visible) {
        if (visible) {
            startSystemUI();
        } else {
            stopSystemUI();
        }
        return visible;
    }

    public static void toggleSystemUI() {
        if (isSystemUIRunning()) {
            stopSystemUI();
        } else {
            startSystemUI();
        }
    }

    public static void exec(final String... as) {
        (new Thread(new Runnable() {

            @Override
            public void run() {
                execImpl(as);
            }
        })).start();
    }

    private static String execImpl(final String... as) {
        try {
            final Process process = Runtime.getRuntime().exec(as);
            final InputStreamReader r = new InputStreamReader(process.getInputStream());
            final StringWriter w = new StringWriter();
            final char ac[] = new char[8192];
            int i = 0;
            do {
                i = r.read(ac);
                if (i > 0) {
                    w.write(ac, 0, i);
                }
            } while (i != -1);
            r.close();
            process.waitFor();
            return w.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getSuPath() {
        final File su1 = new File(SU_PATH1);
        if (su1.exists() && su1.isFile() && su1.canExecute()) {
            return SU_PATH1;
        }
        final File su2 = new File(SU_PATH2);
        if (su2.exists() && su2.isFile() && su2.canExecute()) {
            return SU_PATH2;
        }
        return null;
    }

}
