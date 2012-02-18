package org.emdev.ui.fullscreen;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.common.log.LogContext;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.Window;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class FullScreenManagerNew implements IFullScreenManager {

    private static final LogContext LCTX = LogContext.ROOT.lctx("FullScreen");

    private static final String SYS_UI_CLS = "com.android.systemui.SystemUIService";
    private static final String SYS_UI_PKG = "com.android.systemui";
    private static final ComponentName SYS_UI = new ComponentName(SYS_UI_PKG, SYS_UI_CLS);

    private static final String SU_PATH1 = "/system/bin/su";
    private static final String SU_PATH2 = "/system/xbin/su";
    private static final String AM_PATH = "/system/bin/am";

    private final AtomicBoolean fullScreen = new AtomicBoolean();
    private final AtomicBoolean wasSet = new AtomicBoolean();

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#setFullScreenMode(android.view.Window, boolean)
     */
    @Override
    public void setFullScreenMode(final Window w, final boolean fullScreen) {
        if (this.fullScreen.compareAndSet(!fullScreen, fullScreen)) {
            if (fullScreen) {
                stopSystemUI();
            } else {
                startSystemUI();
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#onPause()
     */
    @Override
    public void onPause(final Window w) {
        if (wasSet.get()) {
            startSystemUI();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.FullScreenManagerOld#onResume()
     */
    @Override
    public void onResume(final Window w) {
        if (wasSet.get()) {
            stopSystemUI();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuOpened(android.view.Window)
     */
    @Override
    public void onMenuOpened(final Window w) {
        setFullScreenMode(w, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.emdev.ui.fullscreen.IFullScreenManager#onMenuClosed(android.view.Window)
     */
    @Override
    public void onMenuClosed(final Window w) {
        setFullScreenMode(w, true);
    }

    protected void startSystemUI() {
        if (isSystemUIRunning()) {
            wasSet.set(false);
            return;
        }
        exec(new String[] { AM_PATH, "startservice", "-n", SYS_UI.flattenToString() });
    }

    protected void stopSystemUI() {
        if (!isSystemUIRunning()) {
            wasSet.set(false);
            return;
        }

        final String su = getSuPath();
        if (su == null) {
            wasSet.set(false);
            return;
        }

        exec(new String[] { su, "-c", "service call activity 79 s16 " + SYS_UI_PKG });
    }

    protected boolean isSystemUIRunning() {
        final ActivityManager actvityManager = (ActivityManager) EBookDroidApp.context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> rsiList = actvityManager.getRunningServices(1000);

        for (final RunningServiceInfo rsi : rsiList) {
            LCTX.d("Service: " + rsi.service);
            if (SYS_UI.equals(rsi.service)) {
                LCTX.e("System UI service found");
                return true;
            }
        }
        return false;
    }

    protected void exec(final String... as) {
        (new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final boolean result = execImpl(as);
                    wasSet.set(result);
                } catch (final Throwable th) {
                    LCTX.e("Changing full screen mode failed: " + th.getCause());
                    wasSet.set(false);
                    fullScreen.set(!fullScreen.get());
                }
            }
        })).start();
    }

    private boolean execImpl(final String... as) {
        try {
            LCTX.d("Execute: " + Arrays.toString(as));
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

            int exitValue = process.exitValue();
            final String text = w.toString();
            LCTX.d("Result code: " + exitValue);
            LCTX.d("Output:\n" + text);

            return 0 == exitValue;

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
