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

import java.util.HashSet;
import java.util.Set;

import org.emdev.utils.android.AndroidVersion;
import org.emdev.utils.filesystem.FileExtensionFilter;

public class AppSettings implements AppPreferences {

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

    private Integer touchProcessingDelay;

    private PageAnimationType animationType;

    private Boolean splitPages;

    private Boolean pageInTitle;

    private Integer brightness;

    private Boolean brightnessInNightModeOnly;

    private Boolean keepScreenOn;

    private Set<String> autoScanDirs;

    private Boolean loadRecent;

    private Boolean useBookcase;

    private Integer djvuRenderingMode;

    private Boolean cropPages;

    private String tapProfiles;

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

    /* =============== UI settings =============== */

    public boolean isLoadRecentBook() {
        if (loadRecent == null) {
            loadRecent = LOAD_RECENT.getPreferenceValue(prefs);
        }
        return loadRecent;
    }

    public boolean getNightMode() {
        if (nightMode == null) {
            nightMode = NIGHT_MODE.getPreferenceValue(prefs);
        }
        return nightMode;
    }

    public void toggleNightMode() {
        nightMode = !nightMode;
        final Editor edit = prefs.edit();
        NIGHT_MODE.setPreferenceValue(edit, nightMode);
        edit.commit();
    }

    public boolean isBrightnessInNightModeOnly() {
        if (brightnessInNightModeOnly == null) {
            brightnessInNightModeOnly = BRIGHTNESS_NIGHT_MODE_ONLY.getPreferenceValue(prefs);
        }
        return brightnessInNightModeOnly;
    }

    public int getBrightness() {
        if (isBrightnessInNightModeOnly() && !getNightMode()) {
            return BRIGHTNESS.maxValue;
        }
        if (brightness == null) {
            brightness = BRIGHTNESS.getPreferenceValue(prefs);
        }
        return brightness;
    }

    public boolean isKeepScreenOn() {
        if (keepScreenOn == null) {
            keepScreenOn = KEEP_SCREEN_ON.getPreferenceValue(prefs);
        }
        return keepScreenOn;
    }

    public RotationType getRotation() {
        if (rotation == null) {
            rotation = ROTATION.getPreferenceValue(prefs);
        }
        return rotation;
    }

    public boolean getFullScreen() {
        if (fullScreen == null) {
            fullScreen = FULLSCREEN.getPreferenceValue(prefs);
        }
        return fullScreen;
    }

    public boolean getShowTitle() {
        if (showTitle == null) {
            showTitle = SHOW_TITLE.getPreferenceValue(prefs);
        }
        return showTitle;
    }

    public boolean getPageInTitle() {
        if (pageInTitle == null) {
            pageInTitle = SHOW_PAGE_IN_TITLE.getPreferenceValue(prefs);
        }
        return pageInTitle;
    }

    public boolean getShowAnimIcon() {
        if (showAnimIcon == null) {
            showAnimIcon = SHOW_ANIM_ICON.getPreferenceValue(prefs);
        }
        return showAnimIcon;
    }

    /* =============== Tap & Scroll settings =============== */

    public boolean getTapsEnabled() {
        if (tapsEnabled == null) {
            tapsEnabled = TAPS_ENABLED.getPreferenceValue(prefs);
        }
        return tapsEnabled;
    }

    public int getScrollHeight() {
        if (scrollHeight == null) {
            scrollHeight = SCROLL_HEIGHT.getPreferenceValue(prefs);
        }
        return scrollHeight.intValue();
    }

    public int getTouchProcessingDelay() {
        if (touchProcessingDelay == null) {
            touchProcessingDelay = TOUCH_DELAY.getPreferenceValue(prefs);
        }
        return touchProcessingDelay;
    }

    /* =============== Tap & Keyboard settings =============== */

    public String getTapProfiles() {
        if (tapProfiles == null) {
            tapProfiles = TAP_PROFILES.getPreferenceValue(prefs);
        }
        return tapProfiles;
    }

    public void updateTapProfiles(final String profiles) {
        tapProfiles = profiles;
        final Editor edit = prefs.edit();
        TAP_PROFILES.setPreferenceValue(edit, profiles);
        edit.commit();
    }

    public String getKeysBinding() {
        if (keysBinding == null) {
            keysBinding = KEY_BINDINGS.getPreferenceValue(prefs);
        }
        return keysBinding;
    }

    public void updateKeysBinding(final String json) {
        keysBinding = json;
        final Editor edit = prefs.edit();
        KEY_BINDINGS.setPreferenceValue(edit, json);
        edit.commit();
    }

    /* =============== Performance settings =============== */

    public int getPagesInMemory() {
        if (pagesInMemory == null) {
            pagesInMemory = PAGES_IN_MEMORY.getPreferenceValue(prefs);
        }
        return pagesInMemory.intValue();
    }

    public DocumentViewType getDocumentViewType() {
        if (viewType == null) {
            viewType = VIEW_TYPE.getPreferenceValue(prefs);
        }
        return viewType;
    }

    public int getDecodingThreadPriority() {
        if (decodingThreadPriority == null) {
            decodingThreadPriority = DECODE_THREAD_PRIORITY.getPreferenceValue(prefs);
        }
        return decodingThreadPriority.intValue();
    }

    public int getDrawThreadPriority() {
        if (drawThreadPriority == null) {
            drawThreadPriority = DRAW_THREAD_PRIORITY.getPreferenceValue(prefs);
        }
        return drawThreadPriority.intValue();
    }

    public boolean isHWAEnabled() {
        if (hwaEnabled == null) {
            hwaEnabled = HWA_ENABLED.getPreferenceValue(prefs);
        }
        return hwaEnabled.booleanValue();
    }

    public int getBitmapSize() {
        if (bitmapSize == null) {
            bitmapSize = 1 << BITMAP_SIZE.getPreferenceValue(prefs);
        }
        return bitmapSize.intValue();
    }

    public boolean getTextureReuseEnabled() {
        if (textureReuseEnabled == null) {
            textureReuseEnabled = REUSE_TEXTURES.getPreferenceValue(prefs);
        }
        return textureReuseEnabled;
    }

    public boolean getUseEarlyRecycling() {
        if (useEarlyRecycling == null) {
            useEarlyRecycling = EARLY_RECYCLING.getPreferenceValue(prefs);
        }
        return useEarlyRecycling;
    }

    public boolean getReloadDuringZoom() {
        if (reloadDuringZoom == null) {
            reloadDuringZoom = RELOAD_DURING_ZOOM.getPreferenceValue(prefs);
        }
        return reloadDuringZoom;
    }

    /* =============== Default rendering settings =============== */

    boolean getSplitPages() {
        if (splitPages == null) {
            splitPages = SPLIT_PAGES.getPreferenceValue(prefs);
        }
        return splitPages;
    }

    boolean getCropPages() {
        if (cropPages == null) {
            cropPages = CROP_PAGES.getPreferenceValue(prefs);
        }
        return cropPages;
    }

    public DocumentViewMode getViewMode() {
        if (viewMode == null) {
            viewMode = VIEW_MODE.getPreferenceValue(prefs);
        }
        return viewMode;
    }

    PageAlign getPageAlign() {
        if (pageAlign == null) {
            pageAlign = PAGE_ALIGN.getPreferenceValue(prefs);
        }
        return pageAlign;
    }

    PageAnimationType getAnimationType() {
        if (animationType == null) {
            animationType = ANIMATION_TYPE.getPreferenceValue(prefs);
        }
        return animationType;
    }

    /* =============== Format-specific settings =============== */

    public int getDjvuRenderingMode() {
        if (djvuRenderingMode == null) {
            djvuRenderingMode = DJVU_RENDERING_MODE.getPreferenceValue(prefs);
        }
        return djvuRenderingMode;
    }

    public boolean useCustomDpi() {
        if (useCustomDpi == null) {
            useCustomDpi = PDF_CUSTOM_DPI.getPreferenceValue(prefs);
        }
        return useCustomDpi.booleanValue();
    }

    public float getXDpi(final float def) {
        if (xDpi == null) {
            xDpi = PDF_CUSTOM_XDPI.getPreferenceValue(prefs);
        }
        if (useCustomDpi()) {
            return xDpi.floatValue();
        } else {
            return def;
        }
    }

    public float getYDpi(final float def) {
        if (yDpi == null) {
            yDpi = PDF_CUSTOM_YDPI.getPreferenceValue(prefs);
        }
        if (useCustomDpi()) {
            return yDpi.floatValue();
        } else {
            return def;
        }
    }

    public FontSize getFontSize() {
        if (fontSize == null) {
            fontSize = FB2_FONT_SIZE.getPreferenceValue(prefs);
        }
        return fontSize;
    }

    public boolean isFb2HyphenEnabled() {
        if (fb2HyphenEnabled == null) {
            fb2HyphenEnabled = FB2_HYPHEN.getPreferenceValue(prefs);
        }
        return fb2HyphenEnabled;
    }

    /* =============== Browser settings =============== */

    public boolean getUseBookcase() {
        if (useBookcase == null) {
            useBookcase = prefs.getBoolean("usebookcase", true);
        }
        return !AndroidVersion.is1x && useBookcase;
    }

    public Set<String> getAutoScanDirs() {
        if (autoScanDirs == null) {
            autoScanDirs = AUTO_SCAN_DIRS.getPreferenceValue(prefs);
        }
        return autoScanDirs;
    }

    public void setAutoScanDirs(final Set<String> dirs) {
        autoScanDirs = dirs;
        final Editor edit = prefs.edit();
        AUTO_SCAN_DIRS.setPreferenceValue(edit, dirs);
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

    /* =============== */

    void clearPseudoBookSettings() {
        final Editor editor = prefs.edit();
        editor.remove(BOOK.key);
        editor.remove(BOOK_SPLIT_PAGES.key);
        editor.remove(BOOK_CROP_PAGES.key);
        editor.remove(BOOK_PAGE_ALIGN.key);
        editor.remove(BOOK_ANIMATION_TYPE.key);
        editor.commit();
    }

    void updatePseudoBookSettings(final BookSettings bs) {
        final Editor edit = prefs.edit();
        BOOK.setPreferenceValue(edit, bs.fileName);
        BOOK_SPLIT_PAGES.setPreferenceValue(edit, bs.splitPages);
        BOOK_CROP_PAGES.setPreferenceValue(edit, bs.cropPages);
        BOOK_VIEW_MODE.setPreferenceValue(edit, bs.viewMode);
        BOOK_PAGE_ALIGN.setPreferenceValue(edit, bs.pageAlign);
        BOOK_ANIMATION_TYPE.setPreferenceValue(edit, bs.animationType);
        edit.commit();
    }

    void fillBookSettings(final BookSettings bs) {
        bs.splitPages = BOOK_SPLIT_PAGES.getPreferenceValue(prefs, getSplitPages());
        bs.cropPages = BOOK_CROP_PAGES.getPreferenceValue(prefs, getCropPages());
        bs.viewMode = BOOK_VIEW_MODE.getPreferenceValue(prefs, getViewMode());
        bs.pageAlign = BOOK_PAGE_ALIGN.getPreferenceValue(prefs, getPageAlign());
        bs.animationType = BOOK_ANIMATION_TYPE.getPreferenceValue(prefs, getAnimationType());
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
