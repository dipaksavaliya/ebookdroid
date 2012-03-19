package org.ebookdroid.common.settings;

import org.ebookdroid.common.settings.books.BookSettings;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.DocumentViewType;
import org.ebookdroid.common.settings.types.FontSize;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.common.settings.types.RotationType;
import org.ebookdroid.common.settings.types.ToastPosition;
import org.ebookdroid.core.curl.PageAnimationType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Set;

import org.emdev.utils.android.AndroidVersion;
import org.emdev.utils.filesystem.FileExtensionFilter;

public class AppSettings implements AppPreferences {

    private static boolean firstLoad = true;

    final SharedPreferences prefs;

    public final boolean loadRecent;

    public final boolean nightMode;

    public final boolean brightnessInNightModeOnly;

    public final int brightness;

    public final boolean keepScreenOn;

    public final RotationType rotation;

    public final boolean fullScreen;

    public final boolean showTitle;

    public final boolean pageInTitle;

    public final ToastPosition pageNumberToastPosition;

    public final boolean showAnimIcon;

    public final boolean tapsEnabled;

    public final int scrollHeight;

    public final int touchProcessingDelay;

    public final String tapProfiles;

    public final String keysBinding;

    public final int pagesInMemory;

    public final DocumentViewType viewType;

    public final int decodingThreadPriority;

    public final int drawThreadPriority;

    public final boolean hwaEnabled;

    public final int bitmapSize;

    public final boolean bitmapFileringEnabled;

    public final boolean textureReuseEnabled;

    public final boolean reloadDuringZoom;

    public final boolean useEarlyRecycling;

    final boolean splitPages;

    final boolean cropPages;

    public final DocumentViewMode viewMode;

    final PageAlign pageAlign;

    final PageAnimationType animationType;

    public final int djvuRenderingMode;

    public final boolean useCustomDpi;

    public final int xDpi;

    public final int yDpi;

    public final FontSize fontSize;

    public final boolean fb2HyphenEnabled;

    public final boolean useBookcase;

    public final Set<String> autoScanDirs;

    public final String searchBookQuery;

    public final FileExtensionFilter allowedFileTypes;

    AppSettings(final Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        /* =============== UI settings =============== */
        loadRecent = LOAD_RECENT.getPreferenceValue(prefs);
        nightMode = NIGHT_MODE.getPreferenceValue(prefs);
        brightnessInNightModeOnly = BRIGHTNESS_NIGHT_MODE_ONLY.getPreferenceValue(prefs);
        brightness = BRIGHTNESS.getPreferenceValue(prefs);
        keepScreenOn = KEEP_SCREEN_ON.getPreferenceValue(prefs);
        rotation = ROTATION.getPreferenceValue(prefs);
        fullScreen = FULLSCREEN.getPreferenceValue(prefs);
        showTitle = SHOW_TITLE.getPreferenceValue(prefs);
        pageInTitle = SHOW_PAGE_IN_TITLE.getPreferenceValue(prefs);
        pageNumberToastPosition = PAGE_NUMBER_TOAST_POSITION.getPreferenceValue(prefs);
        showAnimIcon = SHOW_ANIM_ICON.getPreferenceValue(prefs);
        /* =============== Tap & Scroll settings =============== */
        tapsEnabled = TAPS_ENABLED.getPreferenceValue(prefs);
        scrollHeight = SCROLL_HEIGHT.getPreferenceValue(prefs);
        touchProcessingDelay = TOUCH_DELAY.getPreferenceValue(prefs);
        /* =============== Tap & Keyboard settings =============== */
        tapProfiles = TAP_PROFILES.getPreferenceValue(prefs);
        keysBinding = KEY_BINDINGS.getPreferenceValue(prefs);
        /* =============== Performance settings =============== */
        pagesInMemory = PAGES_IN_MEMORY.getPreferenceValue(prefs);
        viewType = VIEW_TYPE.getPreferenceValue(prefs);
        decodingThreadPriority = DECODE_THREAD_PRIORITY.getPreferenceValue(prefs);
        drawThreadPriority = DRAW_THREAD_PRIORITY.getPreferenceValue(prefs);
        hwaEnabled = HWA_ENABLED.getPreferenceValue(prefs);
        bitmapSize = BITMAP_SIZE.getPreferenceValue(prefs);
        bitmapFileringEnabled = BITMAP_FILTERING.getPreferenceValue(prefs);
        textureReuseEnabled = REUSE_TEXTURES.getPreferenceValue(prefs);
        useEarlyRecycling = EARLY_RECYCLING.getPreferenceValue(prefs);
        reloadDuringZoom = RELOAD_DURING_ZOOM.getPreferenceValue(prefs);
        /* =============== Default rendering settings =============== */
        splitPages = SPLIT_PAGES.getPreferenceValue(prefs);
        cropPages = CROP_PAGES.getPreferenceValue(prefs);
        viewMode = VIEW_MODE.getPreferenceValue(prefs);
        pageAlign = PAGE_ALIGN.getPreferenceValue(prefs);
        animationType = ANIMATION_TYPE.getPreferenceValue(prefs);
        /* =============== Format-specific settings =============== */
        djvuRenderingMode = DJVU_RENDERING_MODE.getPreferenceValue(prefs);
        useCustomDpi = PDF_CUSTOM_DPI.getPreferenceValue(prefs);
        xDpi = PDF_CUSTOM_XDPI.getPreferenceValue(prefs);
        yDpi = PDF_CUSTOM_YDPI.getPreferenceValue(prefs);
        fontSize = FB2_FONT_SIZE.getPreferenceValue(prefs);
        fb2HyphenEnabled = FB2_HYPHEN.getPreferenceValue(prefs);
        /* =============== Browser settings =============== */
        useBookcase = USE_BOOK_CASE.getPreferenceValue(prefs);
        autoScanDirs = AUTO_SCAN_DIRS.getPreferenceValue(prefs);
        searchBookQuery = SEARCH_BOOK_QUERY.getPreferenceValue(prefs);
        allowedFileTypes = FILE_TYPE_FILTER.getPreferenceValue(prefs, true);

        if (firstLoad) {
            firstLoad = false;
            boolean initialStoring = false;
            try {
                initialStoring = prefs.getString(PAGES_IN_MEMORY.key, null) == null;
            } catch (Exception ex) {
                initialStoring = true;
            }
            if (initialStoring) {
                final Editor edit = prefs.edit();

                LOAD_RECENT.setPreferenceValue(edit, loadRecent);
                NIGHT_MODE.setPreferenceValue(edit, nightMode);
                BRIGHTNESS_NIGHT_MODE_ONLY.setPreferenceValue(edit, brightnessInNightModeOnly);
                BRIGHTNESS.setPreferenceValue(edit, brightness);
                KEEP_SCREEN_ON.setPreferenceValue(edit, keepScreenOn);
                ROTATION.setPreferenceValue(edit, rotation);
                FULLSCREEN.setPreferenceValue(edit, fullScreen);
                SHOW_TITLE.setPreferenceValue(edit, showTitle);
                SHOW_PAGE_IN_TITLE.setPreferenceValue(edit, pageInTitle);
                PAGE_NUMBER_TOAST_POSITION.setPreferenceValue(edit, pageNumberToastPosition);
                SHOW_ANIM_ICON.setPreferenceValue(edit, showAnimIcon);
                /* =============== Tap & Scroll settings =============== */
                TAPS_ENABLED.setPreferenceValue(edit, tapsEnabled);
                SCROLL_HEIGHT.setPreferenceValue(edit, scrollHeight);
                TOUCH_DELAY.setPreferenceValue(edit, touchProcessingDelay);
                /* =============== Tap & Keyboard settings =============== */
                TAP_PROFILES.setPreferenceValue(edit, tapProfiles);
                KEY_BINDINGS.setPreferenceValue(edit, keysBinding);
                /* =============== Performance settings =============== */
                PAGES_IN_MEMORY.setPreferenceValue(edit, pagesInMemory);
                VIEW_TYPE.setPreferenceValue(edit, viewType);
                DECODE_THREAD_PRIORITY.setPreferenceValue(edit, decodingThreadPriority);
                DRAW_THREAD_PRIORITY.setPreferenceValue(edit, drawThreadPriority);
                HWA_ENABLED.setPreferenceValue(edit, hwaEnabled);
                BITMAP_SIZE.setPreferenceValue(edit, bitmapSize);
                BITMAP_FILTERING.setPreferenceValue(edit, bitmapFileringEnabled);
                REUSE_TEXTURES.setPreferenceValue(edit, textureReuseEnabled);
                EARLY_RECYCLING.setPreferenceValue(edit, useEarlyRecycling);
                RELOAD_DURING_ZOOM.setPreferenceValue(edit, reloadDuringZoom);
                /* =============== Default rendering settings =============== */
                SPLIT_PAGES.setPreferenceValue(edit, splitPages);
                CROP_PAGES.setPreferenceValue(edit, cropPages);
                VIEW_MODE.setPreferenceValue(edit, viewMode);
                PAGE_ALIGN.setPreferenceValue(edit, pageAlign);
                ANIMATION_TYPE.setPreferenceValue(edit, animationType);
                /* =============== Format-specific settings =============== */
                DJVU_RENDERING_MODE.setPreferenceValue(edit, djvuRenderingMode);
                PDF_CUSTOM_DPI.setPreferenceValue(edit, useCustomDpi);
                PDF_CUSTOM_XDPI.setPreferenceValue(edit, xDpi);
                PDF_CUSTOM_YDPI.setPreferenceValue(edit, yDpi);
                FB2_FONT_SIZE.setPreferenceValue(edit, fontSize);
                FB2_HYPHEN.setPreferenceValue(edit, fb2HyphenEnabled);
                /* =============== Browser settings =============== */
                USE_BOOK_CASE.setPreferenceValue(edit, useBookcase);
                AUTO_SCAN_DIRS.setPreferenceValue(edit, autoScanDirs);
                SEARCH_BOOK_QUERY.setPreferenceValue(edit, searchBookQuery);
                FILE_TYPE_FILTER.setPreferenceValue(edit, true);

                edit.commit();
            }
        }
    }

    /* =============== UI settings =============== */
    /* =============== Tap & Scroll settings =============== */
    /* =============== Tap & Keyboard settings =============== */
    /* =============== Default rendering settings =============== */
    /* =============== Format-specific settings =============== */

    public float getXDpi(final float def) {
        return useCustomDpi ? xDpi : def;
    }

    public float getYDpi(final float def) {
        return useCustomDpi ? yDpi : def;
    }

    /* =============== Browser settings =============== */

    public boolean getUseBookcase() {
        return !AndroidVersion.is1x && useBookcase;
    }

    /* =============== */

    void clearPseudoBookSettings() {
        final Editor editor = prefs.edit();
        editor.remove(BOOK.key);
        editor.remove(BOOK_SPLIT_PAGES.key);
        editor.remove(BOOK_CROP_PAGES.key);
        editor.remove(BOOK_VIEW_MODE.key);
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
        bs.splitPages = BOOK_SPLIT_PAGES.getPreferenceValue(prefs, splitPages);
        bs.cropPages = BOOK_CROP_PAGES.getPreferenceValue(prefs, cropPages);
        bs.viewMode = BOOK_VIEW_MODE.getPreferenceValue(prefs, viewMode);
        bs.pageAlign = BOOK_PAGE_ALIGN.getPreferenceValue(prefs, pageAlign);
        bs.animationType = BOOK_ANIMATION_TYPE.getPreferenceValue(prefs, animationType);
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
            if (firstTime) {
                mask = 0xFFFFFFFF;
            } else if (news != null) {
                if (olds.nightMode != news.nightMode) {
                    mask |= D_NightMode;
                }
                if (olds.rotation != news.rotation) {
                    mask |= D_Rotation;
                }
                if (olds.fullScreen != news.fullScreen) {
                    mask |= D_FullScreen;
                }
                if (olds.showTitle != news.showTitle) {
                    mask |= D_ShowTitle;
                }
                if (olds.pageInTitle != news.pageInTitle) {
                    mask |= D_PageInTitle;
                }
                if (olds.tapsEnabled != news.tapsEnabled) {
                    mask |= D_TapsEnabled;
                }
                if (olds.scrollHeight != news.scrollHeight) {
                    mask |= D_ScrollHeight;
                }
                if (olds.pagesInMemory != news.pagesInMemory) {
                    mask |= D_PagesInMemory;
                }
                if (olds.brightness != news.brightness) {
                    mask |= D_Brightness;
                }
                if (olds.brightnessInNightModeOnly != news.brightnessInNightModeOnly) {
                    mask |= D_BrightnessInNightMode;
                }
                if (olds.keepScreenOn != news.keepScreenOn) {
                    mask |= D_KeepScreenOn;
                }
                if (olds.loadRecent != news.loadRecent) {
                    mask |= D_LoadRecent;
                }
                if (olds.getUseBookcase() != news.getUseBookcase()) {
                    mask |= D_UseBookcase;
                }
                if (olds.djvuRenderingMode != news.djvuRenderingMode) {
                    mask |= D_DjvuRenderingMode;
                }
                if (!olds.autoScanDirs.equals(news.autoScanDirs)) {
                    mask |= D_AutoScanDirs;
                }
                if (!olds.allowedFileTypes.equals(news.allowedFileTypes)) {
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
