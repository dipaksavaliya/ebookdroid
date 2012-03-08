package org.ebookdroid.common.settings;

import static org.ebookdroid.R.string.*;

import org.ebookdroid.common.settings.base.BooleanPreferenceDefinition;
import org.ebookdroid.common.settings.base.EnumPreferenceDefinition;
import org.ebookdroid.common.settings.base.FileListPreferenceDefinition;
import org.ebookdroid.common.settings.base.IntegerPreferenceDefinition;
import org.ebookdroid.common.settings.base.StringPreferenceDefinition;
import org.ebookdroid.common.settings.types.DocumentViewMode;
import org.ebookdroid.common.settings.types.DocumentViewType;
import org.ebookdroid.common.settings.types.PageAlign;
import org.ebookdroid.common.settings.types.RotationType;
import org.ebookdroid.core.curl.PageAnimationType;

public interface AppPreferences {

    /* =============== UI settings =============== */

    BooleanPreferenceDefinition LOAD_RECENT = new BooleanPreferenceDefinition(pref_loadrecent_id,
            pref_loadrecent_defvalue);

    BooleanPreferenceDefinition NIGHT_MODE = new BooleanPreferenceDefinition(pref_nightmode_id, pref_nightmode_defvalue);

    BooleanPreferenceDefinition BRIGHTNESS_NIGHT_MODE_ONLY = new BooleanPreferenceDefinition(
            pref_brightnessnightmodeonly_id, pref_brightnessnightmodeonly_defvalue);

    IntegerPreferenceDefinition BRIGHTNESS = new IntegerPreferenceDefinition(pref_brightness_id,
            pref_brightness_defvalue, pref_brightness_minvalue, pref_brightness_maxvalue);

    BooleanPreferenceDefinition KEEP_SCREEN_ON = new BooleanPreferenceDefinition(pref_keepscreenon_id,
            pref_keepscreenon_defvalue);

    EnumPreferenceDefinition<RotationType> ROTATION = new EnumPreferenceDefinition<RotationType>(RotationType.class,
            pref_rotation_id, pref_rotation_auto);

    BooleanPreferenceDefinition FULLSCREEN = new BooleanPreferenceDefinition(pref_fullscreen_id,
            pref_fullscreen_defvalue);

    BooleanPreferenceDefinition SHOW_TITLE = new BooleanPreferenceDefinition(pref_title_id, pref_title_defvalue);

    BooleanPreferenceDefinition SHOW_PAGE_IN_TITLE = new BooleanPreferenceDefinition(pref_pageintitle_id,
            pref_pageintitle_defvalue);

    BooleanPreferenceDefinition SHOW_ANIM_ICON = new BooleanPreferenceDefinition(pref_showanimicon_id,
            pref_showanimicon_defvalue);

    /* =============== Tap & Scroll settings =============== */

    BooleanPreferenceDefinition TAPS_ENABLED = new BooleanPreferenceDefinition(pref_tapsenabled_id,
            pref_tapsenabled_defvalue);

    IntegerPreferenceDefinition SCROLL_HEIGHT = new IntegerPreferenceDefinition(pref_scrollheight_id,
            pref_scrollheight_defvalue, pref_scrollheight_minvalue, pref_scrollheight_maxvalue);

    IntegerPreferenceDefinition TOUCH_DELAY = new IntegerPreferenceDefinition(pref_touchdelay_id,
            pref_touchdelay_defvalue, pref_touchdelay_minvalue, pref_touchdelay_maxvalue);

    /* =============== Tap & Keys settings =============== */

    StringPreferenceDefinition TAP_PROFILES = new StringPreferenceDefinition(pref_tapprofiles_id,
            pref_tapprofiles_defvalue);

    StringPreferenceDefinition KEY_BINDINGS = new StringPreferenceDefinition(pref_keys_binding_id,
            pref_keys_binding_defvalue);

    /* =============== Performance settings =============== */

    IntegerPreferenceDefinition PAGES_IN_MEMORY = new IntegerPreferenceDefinition(pref_pagesinmemory_id,
            pref_pagesinmemory_defvalue, pref_pagesinmemory_minvalue, pref_pagesinmemory_maxvalue);

    EnumPreferenceDefinition<DocumentViewType> VIEW_TYPE = new EnumPreferenceDefinition<DocumentViewType>(
            DocumentViewType.class, pref_docviewtype_id, pref_docviewtype_surface);

    IntegerPreferenceDefinition DECODE_THREAD_PRIORITY = new IntegerPreferenceDefinition(pref_decodethread_priority_id,
            pref_thread_priority_normal, pref_thread_priority_lowest, pref_thread_priority_highest);

    IntegerPreferenceDefinition DRAW_THREAD_PRIORITY = new IntegerPreferenceDefinition(pref_drawthread_priority_id,
            pref_thread_priority_normal, pref_thread_priority_lowest, pref_thread_priority_highest);

    BooleanPreferenceDefinition HWA_ENABLED = new BooleanPreferenceDefinition(pref_hwa_enabled_id,
            pref_hwa_enabled_defvalue);

    IntegerPreferenceDefinition BITMAP_SIZE = new IntegerPreferenceDefinition(pref_bitmapsize_id, pref_bitmapsize_128,
            pref_bitmapsize_64, pref_bitmapsize_1024);

    BooleanPreferenceDefinition REUSE_TEXTURES = new BooleanPreferenceDefinition(pref_texturereuse_id,
            pref_texturereuse_defvalue);

    BooleanPreferenceDefinition EARLY_RECYCLING = new BooleanPreferenceDefinition(pref_earlyrecycling_id,
            pref_earlyrecycling_defvalue);

    BooleanPreferenceDefinition RELOAD_DURING_ZOOM = new BooleanPreferenceDefinition(pref_reloadduringzoom_id,
            pref_reloadduringzoom_defvalue);

    /* =============== Default rendering settings =============== */

    BooleanPreferenceDefinition SPLIT_PAGES = new BooleanPreferenceDefinition(pref_splitpages_id,
            pref_splitpages_defvalue);

    BooleanPreferenceDefinition CROP_PAGES = new BooleanPreferenceDefinition(pref_croppages_id, pref_croppages_defvalue);

    EnumPreferenceDefinition<DocumentViewMode> VIEW_MODE = new EnumPreferenceDefinition<DocumentViewMode>(
            DocumentViewMode.class, pref_viewmode_id, pref_viewmode_vertical_scroll);

    EnumPreferenceDefinition<PageAlign> PAGE_ALIGN = new EnumPreferenceDefinition<PageAlign>(PageAlign.class,
            pref_align_id, pref_align_by_width);

    EnumPreferenceDefinition<PageAnimationType> ANIMATION_TYPE = new EnumPreferenceDefinition<PageAnimationType>(
            PageAnimationType.class, pref_animation_type_id, pref_animation_type_none);

    /* =============== Browser settings =============== */

    FileListPreferenceDefinition AUTO_SCAN_DIRS = new FileListPreferenceDefinition(pref_brautoscandir_id,
            pref_brautoscandir_defvalue);

}
