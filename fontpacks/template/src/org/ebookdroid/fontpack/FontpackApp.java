package org.ebookdroid.fontpack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.emdev.BaseDroidApp;
import org.emdev.fonts.AssetsFontProvider;
import org.emdev.fonts.ExtStorageFontProvider;
import org.emdev.fonts.IFontProvider;
import org.emdev.fonts.SystemFontProvider;
import org.emdev.fonts.data.FontFamilyType;
import org.emdev.fonts.data.FontPack;
import org.emdev.fonts.data.FontStyle;
import org.emdev.fonts.typeface.TypefaceEx;

import android.content.pm.PackageManager;

public class FontpackApp extends BaseDroidApp {

    public static SystemFontProvider sfm;
    public static AssetsFontProvider afm;
    public static ExtStorageFontProvider esfm;

    @Override
    public void onCreate() {
        super.onCreate();

        sfm = new SystemFontProvider();
        sfm.init();

        afm = new AssetsFontProvider();
        afm.init();

        esfm = new ExtStorageFontProvider();
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
        final PackageManager pm = context.getPackageManager();
        // deletePackage(String packageName, IPackageDeleteObserver observer, int flags);
        try {
            final Class<?> ipdoClass = Class.forName("android.content.pm.IPackageDeleteObserver");
            final Method deleteMethod = pm.getClass().getMethod("deletePackage", String.class, ipdoClass, int.class);
            deleteMethod.invoke(pm, BaseDroidApp.APP_PACKAGE, null, Integer.valueOf(0));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
