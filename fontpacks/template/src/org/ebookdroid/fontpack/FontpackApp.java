package org.ebookdroid.fontpack;

import java.io.File;

import org.emdev.BaseDroidApp;
import org.emdev.common.fonts.AssetsFontProvider;
import org.emdev.common.fonts.ExtStorageFontProvider;
import org.emdev.common.fonts.IFontProvider;
import org.emdev.common.fonts.SystemFontProvider;
import org.emdev.common.fonts.data.FontFamilyType;
import org.emdev.common.fonts.data.FontPack;
import org.emdev.common.fonts.data.FontStyle;
import org.emdev.common.fonts.typeface.TypefaceEx;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class FontpackApp extends BaseDroidApp {

    private static final String EBOOKDROID_PACKAGE = "org.ebookdroid";

    public static File EBOOKDROID_APP_STORAGE;
    public static int EBOOKDROID_VERSION;

    public static SystemFontProvider sfm;
    public static AssetsFontProvider afm;
    public static ExtStorageFontProvider esfm;

    @Override
    public void onCreate() {
        super.onCreate();

        EBOOKDROID_VERSION = 0;
        final PackageManager pm = getPackageManager();
        try {
            final PackageInfo pi = pm.getPackageInfo(EBOOKDROID_PACKAGE, 0);
            if (pi != null) {
                EBOOKDROID_VERSION = pi.versionCode;
            } else {
            }
        } catch (NameNotFoundException ex) {
        }

        EBOOKDROID_APP_STORAGE = getAppStorage(EBOOKDROID_PACKAGE);

        sfm = new SystemFontProvider();
        sfm.init();

        afm = new AssetsFontProvider();
        afm.init();

        esfm = new ExtStorageFontProvider(EBOOKDROID_APP_STORAGE);
        esfm.init();

        print(sfm);
        print(afm);
        print(esfm);
    }

    private void print(IFontProvider fm) {
        for (FontPack fp : fm) {
            for (FontFamilyType type : FontFamilyType.values()) {
                for (FontStyle style : FontStyle.values()) {
                    TypefaceEx tf = fm.getTypeface(fp, type, style);
                    System.out.println(fp.name + " " + type + " " + style + ": " + tf);
                }
            }
        }
    }

    public static void uninstall() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        System.out.println(intent);
        context.startActivity(intent);
    }
}
