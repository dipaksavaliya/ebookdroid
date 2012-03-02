package org.ebookdroid.common.settings;

import org.ebookdroid.CodecType;
import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.DocumentViewType;
import org.ebookdroid.common.settings.types.FontSize;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.common.settings.types.RotationType;
import org.ebookdroid.core.curl.PageAnimationType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.emdev.utils.MathUtils;
import org.emdev.utils.StringUtils;
import org.emdev.utils.android.AndroidVersion;
import org.emdev.utils.filesystem.FileExtensionFilter;

public class AppSettings {

    private final SharedPreferences prefs;

    private Boolean tapsEnabled;

    private DocumentViewMode viewMode;

    private Integer pagesInMemory;

    private Boolean showAnimIcon;

    private Boolean nightMode;

    private PageAlign pageAlign;

    private Boolean fullScreen;

    private RotationType rotation;

    private Boolean showTitle;

    private Integer scrollHeight;

    private PageAnimationType animationType;

    private Boolean splitPages;

    private Boolean pageInTitle;

    private Integer brightness;

    private Boolean brightnessnightmodeonly;

    private Boolean keepscreenon;

    private Set<String> autoScanDirs;

    private Boolean loadRecent;

    private Boolean useBookcase;

    private Integer djvuRenderingMode;

    private Boolean cropPages;

    private String touchProfiles;

    private String keysBinding;

    private DocumentViewType viewType;

    private Integer decodingThreadPriority;

    private Integer drawThreadPriority;

    private Boolean hwaEnabled;

    private Integer bitmapSize;

    private Boolean textureReuseEnabled;

    private Boolean reloadDuringZoom;

    private Boolean useCustomDpi;

    private Integer xDpi;

    private Integer yDpi;

    private FontSize fontSize;

    private Boolean fb2HyphenEnabled;

    private Boolean useEarlyRecycling;

    AppSettings(final Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isLoadRecentBook() {
        if (loadRecent == null) {
            loadRecent = prefs.getBoolean("loadrecent", false);
        }
        return loadRecent;
    }

    public Set<String> getAutoScanDirs() {
        if (autoScanDirs == null) {
            autoScanDirs = StringUtils.split(File.pathSeparator, prefs.getString("brautoscandir", "/sdcard"));
        }
        return autoScanDirs;
    }

    public void setAutoScanDirs(final Set<String> dirs) {
        autoScanDirs = dirs;
        final Editor edit = prefs.edit();
        edit.putString("brautoscandir", StringUtils.merge(File.pathSeparator, autoScanDirs));
        edit.commit();
    }

    public void changeAutoScanDirs(final String dir, final boolean add) {
        final Set<String> dirs = getAutoScanDirs();
        if (add && dirs.add(dir) || dirs.remove(dir)) {
            setAutoScanDirs(dirs);
        }
    }

    public FileExtensionFilter getAllowedFileTypes() {
        return getAllowedFileTypes(CodecType.getAllExtensions());
    }

    public FileExtensionFilter getAllowedFileTypes(final Set<String> fileTypes) {
        final Set<String> res = new HashSet<String>();
        for (final String ext : fileTypes) {
            if (isFileTypeAllowed(ext)) {
                res.add(ext);
            }
        }
        return new FileExtensionFilter(res);
    }

    public boolean isFileTypeAllowed(final String ext) {
        return prefs.getBoolean("brfiletype" + ext, true);
    }

    public int getBrightness() {
        if (isBrightnessInNightModeOnly() && !getNightMode()) {
            return 100;
        }
        if (brightness == null) {
            brightness = getIntValue("brightness", 100);
        }
        return brightness;
    }

    public boolean getShowAnimIcon() {
        if (showAnimIcon == null) {
            showAnimIcon = prefs.getBoolean("showanimicon", true);
        }
        return showAnimIcon;
    }

    public boolean getNightMode() {
        if (nightMode == null) {
            nightMode = prefs.getBoolean("nightmode", false);
        }
        return nightMode;
    }

    public boolean isKeepScreenOn() {
        if (keepscreenon == null) {
            keepscreenon = prefs.getBoolean("keepscreenon", true);
        }
        return keepscreenon;
    }

    public boolean isBrightnessInNightModeOnly() {
        if (brightnessnightmodeonly == null) {
            brightnessnightmodeonly = prefs.getBoolean("brightnessnightmodeonly", false);
        }
        return brightnessnightmodeonly;
    }

    public void switchNightMode() {
        nightMode = !nightMode;
        final Editor edit = prefs.edit();
        edit.putBoolean("nightmode", nightMode);
        edit.commit();
    }

    public RotationType getRotation() {
        if (rotation == null) {
            final String rotationStr = prefs.getString("rotation", RotationType.AUTOMATIC.getResValue());
            rotation = RotationType.getByResValue(rotationStr);
            if (rotation == null) {
                rotation = RotationType.AUTOMATIC;
            }
        }
        return rotation;
    }

    public boolean getFullScreen() {
        if (fullScreen == null) {
            fullScreen = prefs.getBoolean("fullscreen", false);
        }
        return fullScreen;
    }

    public boolean getShowTitle() {
        if (showTitle == null) {
            showTitle = prefs.getBoolean("title", true);
        }
        return showTitle;
    }

    public boolean getPageInTitle() {
        if (pageInTitle == null) {
            pageInTitle = prefs.getBoolean("pageintitle", true);
        }
        return pageInTitle;
    }

    public String getTouchProfiles() {
        if (touchProfiles == null) {
            touchProfiles = prefs.getString("tapprofiles", "");
        }
        return touchProfiles;
    }

    public void updateTouchProfiles(final String profiles) {
        touchProfiles = profiles;
        final Editor edit = prefs.edit();
        edit.putString("tapprofiles", touchProfiles);
        edit.commit();
    }

    public String getKeysBinding() {
        if (keysBinding == null) {
            keysBinding = prefs.getString("keys_binding", "");
        }
        return keysBinding;
    }

    public void updateKeysBinding(String json) {
        keysBinding = json;
        final Editor edit = prefs.edit();
        edit.putString("keys_binding", keysBinding);
        edit.commit();
    }

    public boolean getTapsEnabled() {
        if (tapsEnabled == null) {
            tapsEnabled = prefs.getBoolean("tapsenabled", true);
        }
        return tapsEnabled;
    }

    public int getScrollHeight() {
        if (scrollHeight == null) {
            scrollHeight = getIntValue("scrollheight", 50);
        }
        return scrollHeight.intValue();
    }

    public int getPagesInMemory() {
        if (pagesInMemory == null) {
            pagesInMemory = getIntValue("pagesinmemory", 2);
        }
        return pagesInMemory.intValue();
    }

    public DocumentViewType getDocumentViewType() {
        if (viewType == null) {
            final String typeStr = prefs.getString("docviewtype", DocumentViewType.DEFAULT.getResValue());
            viewType = DocumentViewType.getByResValue(typeStr);
            if (viewType == null) {
                viewType = DocumentViewType.DEFAULT;
            }
        }
        return viewType;
    }

    public int getDecodingThreadPriority() {
        if (decodingThreadPriority == null) {
            final int value = getIntValue("decodethread_priority", Thread.NORM_PRIORITY);
            decodingThreadPriority = MathUtils.adjust(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
        }
        return decodingThreadPriority.intValue();
    }

    public int getDrawThreadPriority() {
        if (drawThreadPriority == null) {
            final int value = getIntValue("drawthread_priority", Thread.NORM_PRIORITY);
            drawThreadPriority = MathUtils.adjust(value, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
        }
        return drawThreadPriority.intValue();
    }

    public boolean isHWAEnabled() {
        if (hwaEnabled == null) {
            hwaEnabled = prefs.getBoolean("hwa_enabled", false);
        }
        return hwaEnabled.booleanValue();
    }

    public int getBitmapSize() {
        if (bitmapSize == null) {
            final int value = getIntValue("bitmapsize", 7);
            bitmapSize = 1 << MathUtils.adjust(value, 6, 10);
        }
        return bitmapSize.intValue();
    }

    public boolean getTextureReuseEnabled() {
        if (textureReuseEnabled == null) {
            textureReuseEnabled = prefs.getBoolean("texturereuse", true);
        }
        return textureReuseEnabled;
    }

    public boolean getUseEarlyRecycling() {
        if (useEarlyRecycling == null) {
            useEarlyRecycling = prefs.getBoolean("earlyrecycling", false);
        }
        return useEarlyRecycling;
    }

    public boolean getReloadDuringZoom() {
        if (reloadDuringZoom == null) {
            reloadDuringZoom = prefs.getBoolean("reloadduringzoom", true);
        }
        return reloadDuringZoom;
    }

    public boolean getUseBookcase() {
        if (useBookcase == null) {
            useBookcase = prefs.getBoolean("usebookcase", true);
        }
        return !AndroidVersion.is1x && useBookcase;
    }

    boolean getSplitPages() {
        if (splitPages == null) {
            splitPages = prefs.getBoolean("splitpages", false);
        }
        return splitPages;
    }

    public DocumentViewMode getViewMode() {
        if (viewMode == null) {
            viewMode = DocumentViewMode.getByResValue(prefs.getString("viewmode", null));
            if (viewMode == null) {
                final boolean singlePage = prefs.getBoolean("singlepage", false);
                viewMode = singlePage ? DocumentViewMode.SINGLE_PAGE : DocumentViewMode.VERTICALL_SCROLL;
            }
        }
        return viewMode;
    }

    PageAlign getPageAlign() {
        if (pageAlign == null) {
            final String align = prefs.getString("align", PageAlign.AUTO.getResValue());
            pageAlign = PageAlign.getByResValue(align);
            if (pageAlign == null) {
                pageAlign = PageAlign.AUTO;
            }
        }
        return pageAlign;
    }

    PageAnimationType getAnimationType() {
        if (animationType == null) {
            animationType = PageAnimationType.get(prefs.getString("animationType", null));
        }
        return animationType;
    }

    public int getDjvuRenderingMode() {
        if (djvuRenderingMode == null) {
            djvuRenderingMode = getIntValue("djvu_rendering_mode", 0);
        }
        return djvuRenderingMode;
    }

    public boolean useCustomDpi() {
        if (useCustomDpi == null) {
            useCustomDpi = prefs.getBoolean("customdpi", false);
        }
        return useCustomDpi.booleanValue();
    }

    public float getXDpi(final float def) {
        if (useCustomDpi()) {
            if (xDpi == null) {
                final int value = getIntValue("xdpi", (int) def);
                xDpi = Integer.valueOf(MathUtils.adjust(value, 0, 720));
            }
            return xDpi.floatValue();
        } else {
            return def;
        }
    }

    public float getYDpi(final float def) {
        if (useCustomDpi()) {
            if (yDpi == null) {
                final int value = getIntValue("ydpi", (int) def);
                yDpi = Integer.valueOf(MathUtils.adjust(value, 0, 720));
            }
            return yDpi.floatValue();
        } else {
            return def;
        }
    }

    public FontSize getFontSize() {
        if (fontSize == null) {
            fontSize = FontSize.getByResValue(prefs.getString("fontsize", FontSize.NORMAL.toString()));
        }
        return fontSize;
    }

    public boolean isFb2HyphenEnabled() {
        if (fb2HyphenEnabled == null) {
            fb2HyphenEnabled = prefs.getBoolean("fb2hyphen", false);
        }
        return fb2HyphenEnabled;
    }

    boolean getCropPages() {
        if (cropPages == null) {
            cropPages = prefs.getBoolean("croppages", false);
        }
        return cropPages;
    }

    void clearPseudoBookSettings() {
        final Editor editor = prefs.edit();
        editor.remove("book");
        editor.remove("book_splitpages");
        editor.remove("book_singlepage");
        editor.remove("book_align");
        editor.remove("book_animationType");
        editor.remove("book_croppages");
        editor.commit();
    }

    void updatePseudoBookSettings(final BookSettings bs) {
        final Editor editor = prefs.edit();
        editor.putString("book", bs.fileName);
        editor.putBoolean("book_splitpages", bs.splitPages);
        editor.putString("book_viewmode", bs.viewMode.getResValue());
        editor.putString("book_align", bs.pageAlign.getResValue());
        editor.putString("book_animationType", bs.animationType.getResValue());
        editor.putBoolean("book_croppages", bs.cropPages);
        editor.commit();
    }

    void fillBookSettings(final BookSettings bs) {
        bs.splitPages = prefs.getBoolean("book_splitpages", getSplitPages());

        bs.viewMode = DocumentViewMode.getByResValue(prefs.getString("book_viewmode", getViewMode().getResValue()));
        if (bs.viewMode == null) {
            bs.viewMode = DocumentViewMode.VERTICALL_SCROLL;
        }

        bs.pageAlign = PageAlign.getByResValue(prefs.getString("book_align", getPageAlign().getResValue()));
        if (bs.pageAlign == null) {
            bs.pageAlign = PageAlign.AUTO;
        }
        bs.animationType = PageAnimationType.get(prefs
                .getString("book_animationType", getAnimationType().getResValue()));
        if (bs.animationType == null) {
            bs.animationType = PageAnimationType.NONE;
        }
        bs.cropPages = prefs.getBoolean("book_croppages", getCropPages());

    }

    private int getIntValue(final String key, final int defaultValue) {
        final String str = prefs.getString(key, "" + defaultValue);
        int value = defaultValue;
        try {
            value = Integer.parseInt(str);
        } catch (final NumberFormatException e) {
        }
        return value;
    }

    public static class Diff {

        private static final int D_NightMode = 0x0001 << 0;
        private static final int D_Rotation = 0x0001 << 1;
        private static final int D_FullScreen = 0x0001 << 2;
        private static final int D_ShowTitle = 0x0001 << 3;
        private static final int D_PageInTitle = 0x0001 << 4;
        private static final int D_TapsEnabled = 0x0001 << 5;
        private static final int D_ScrollHeight = 0x0001 << 7;
        private static final int D_PagesInMemory = 0x0001 << 8;
        private static final int D_Brightness = 0x0001 << 10;
        private static final int D_BrightnessInNightMode = 0x0001 << 11;
        private static final int D_KeepScreenOn = 0x0001 << 12;
        private static final int D_LoadRecent = 0x0001 << 13;
        private static final int D_UseBookcase = 0x0001 << 15;
        private static final int D_DjvuRenderingMode = 0x0001 << 16;
        private static final int D_AutoScanDirs = 0x0001 << 17;
        private static final int D_AllowedFileTypes = 0x0001 << 18;

        private int mask;
        private final boolean firstTime;

        public Diff(final AppSettings olds, final AppSettings news) {
            firstTime = olds == null;
            if (news != null) {
                if (firstTime || olds.getNightMode() != news.getNightMode()) {
                    mask |= D_NightMode;
                }
                if (firstTime || olds.getRotation() != news.getRotation()) {
                    mask |= D_Rotation;
                }
                if (firstTime || olds.getFullScreen() != news.getFullScreen()) {
                    mask |= D_FullScreen;
                }
                if (firstTime || olds.getShowTitle() != news.getShowTitle()) {
                    mask |= D_ShowTitle;
                }
                if (firstTime || olds.getPageInTitle() != news.getPageInTitle()) {
                    mask |= D_PageInTitle;
                }
                if (firstTime || olds.getTapsEnabled() != news.getTapsEnabled()) {
                    mask |= D_TapsEnabled;
                }
                if (firstTime || olds.getScrollHeight() != news.getScrollHeight()) {
                    mask |= D_ScrollHeight;
                }
                if (firstTime || olds.getPagesInMemory() != news.getPagesInMemory()) {
                    mask |= D_PagesInMemory;
                }
                if (firstTime || olds.getBrightness() != news.getBrightness()) {
                    mask |= D_Brightness;
                }
                if (firstTime || olds.isBrightnessInNightModeOnly() != news.isBrightnessInNightModeOnly()) {
                    mask |= D_BrightnessInNightMode;
                }
                if (firstTime || olds.isKeepScreenOn() != news.isKeepScreenOn()) {
                    mask |= D_KeepScreenOn;
                }
                if (firstTime || olds.isLoadRecentBook() != news.isLoadRecentBook()) {
                    mask |= D_LoadRecent;
                }
                if (firstTime || olds.getUseBookcase() != news.getUseBookcase()) {
                    mask |= D_UseBookcase;
                }
                if (firstTime || olds.getDjvuRenderingMode() != news.getDjvuRenderingMode()) {
                    mask |= D_DjvuRenderingMode;
                }
                if (firstTime || olds.getAutoScanDirs().equals(news.getAutoScanDirs())) {
                    mask |= D_AutoScanDirs;
                }
                if (firstTime || olds.getAllowedFileTypes().equals(news.getAllowedFileTypes())) {
                    mask |= D_AllowedFileTypes;
                }
            }
        }

        public boolean isFirstTime() {
            return firstTime;
        }

        public boolean isNightModeChanged() {
            return 0 != (mask & D_NightMode);
        }

        public boolean isRotationChanged() {
            return 0 != (mask & D_Rotation);
        }

        public boolean isFullScreenChanged() {
            return 0 != (mask & D_FullScreen);
        }

        public boolean isShowTitleChanged() {
            return 0 != (mask & D_ShowTitle);
        }

        public boolean isPageInTitleChanged() {
            return 0 != (mask & D_PageInTitle);
        }

        public boolean isTapsEnabledChanged() {
            return 0 != (mask & D_TapsEnabled);
        }

        public boolean isScrollHeightChanged() {
            return 0 != (mask & D_ScrollHeight);
        }

        public boolean isPagesInMemoryChanged() {
            return 0 != (mask & D_PagesInMemory);
        }

        public boolean isBrightnessChanged() {
            return 0 != (mask & D_Brightness);
        }

        public boolean isBrightnessInNightModeChanged() {
            return 0 != (mask & D_BrightnessInNightMode);
        }

        public boolean isKeepScreenOnChanged() {
            return 0 != (mask & D_KeepScreenOn);
        }

        public boolean isLoadRecentChanged() {
            return 0 != (mask & D_LoadRecent);
        }

        public boolean isUseBookcaseChanged() {
            return 0 != (mask & D_UseBookcase);
        }

        public boolean isDjvuRenderingModeChanged() {
            return 0 != (mask & D_DjvuRenderingMode);
        }

        public boolean isAutoScanDirsChanged() {
            return 0 != (mask & D_AutoScanDirs);
        }

        public boolean isAllowedFileTypesChanged() {
            return 0 != (mask & D_AllowedFileTypes);
        }
    }
}
