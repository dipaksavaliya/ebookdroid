package org.ebookdroid;

import org.ebookdroid.common.bitmaps.BitmapManager;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.log.EmergencyHandler;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.SettingsManager;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.emdev.utils.android.AndroidVersion;

public class EBookDroidApp extends Application {

    public static final LogContext LCTX = LogContext.ROOT;

    public static Context context;

    public static String APP_VERSION;

    public static String APP_PACKAGE;

    public static File EXT_STORAGE;

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.init();

        EmergencyHandler.init(this);
        LogContext.init(this);
        SettingsManager.init(this);
        CacheManager.init(this);
    }

    protected void init() {
        context = getApplicationContext();

        final PackageManager pm = getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            APP_VERSION = pi.versionName;
            APP_PACKAGE = pi.packageName;
            EXT_STORAGE = Environment.getExternalStorageDirectory();

            LCTX.i(getString(pi.applicationInfo.labelRes) + " (" + APP_PACKAGE + ")" + " v" + APP_VERSION + "("
                    + pi.versionCode + ")");

            LCTX.i("Root             dir: " + Environment.getRootDirectory());
            LCTX.i("Data             dir: " + Environment.getDataDirectory());
            LCTX.i("External storage dir: " + Environment.getExternalStorageDirectory());
            LCTX.i("Files            dir: " + getFilesDir().getAbsolutePath());
            LCTX.i("Cache            dir: " + getCacheDir().getAbsolutePath());
            // LCTX.i("External cache   dir: " + getExternalCacheDir().getAbsolutePath());

            LCTX.i("VERSION     : " + AndroidVersion.VERSION);
            LCTX.i("BOARD       : " + Build.BOARD);
            LCTX.i("BRAND       : " + Build.BRAND);
            // LCTX.i("CPU_ABI     : " + Build.CPU_ABI);
            // LCTX.i("CPU_ABI2    : " + Build.CPU_ABI2);
            LCTX.i("DEVICE      : " + Build.DEVICE);
            LCTX.i("DISPLAY     : " + Build.DISPLAY);
            LCTX.i("FINGERPRINT : " + Build.FINGERPRINT);
            // LCTX.i("HARDWARE    : " + Build.HARDWARE);
            LCTX.i("ID          : " + Build.ID);
            // LCTX.i("MANUFACTURER: " + Build.MANUFACTURER);
            LCTX.i("MODEL       : " + Build.MODEL);
            LCTX.i("PRODUCT     : " + Build.PRODUCT);

        } catch (final NameNotFoundException e) {
            e.printStackTrace();
        }
        copyAssets();
    }
    
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("pdffonts");
        } catch (IOException e) {
            LCTX.i(e.getMessage());
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
              in = assetManager.open("pdffonts/" + filename);
              out = new FileOutputStream("/sdcard/.org.ebookdroid/" + filename);
              copyFile(in, out);
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;
            } catch(Exception e) {
                LCTX.i(e.getMessage());
            }       
        }
    }
    
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
    

    /**
     * {@inheritDoc}
     * 
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapManager.clear("on Low Memory: ");
    }
}
