package org.ebookdroid.fontpack;

import org.emdev.BaseDroidApp;
import org.emdev.fonts.AssetsFontProvider;
import org.emdev.fonts.ExtStorageFontProvider;
import org.emdev.fonts.IFontProvider;
import org.emdev.fonts.SystemFontProvider;
import org.emdev.fonts.data.FontFamilyType;
import org.emdev.fonts.data.FontPack;
import org.emdev.fonts.data.FontStyle;
import org.emdev.fonts.typeface.TypefaceEx;

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

}
