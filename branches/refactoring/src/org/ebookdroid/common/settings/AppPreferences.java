package org.ebookdroid.common.settings;

import org.ebookdroid.R;
import org.ebookdroid.common.settings.base.BooleanPreferenceDefinition;
import org.ebookdroid.common.settings.base.EnumPreferenceDefinition;
import org.ebookdroid.common.settings.base.FileListPreferenceDefinition;
import org.ebookdroid.common.settings.base.IntegerPreferenceDefinition;
import org.ebookdroid.common.settings.base.StringPreferenceDefinition;
import org.ebookdroid.common.settings.types.RotationType;

public interface AppPreferences {

    /* =============== UI settings =============== */

    BooleanPreferenceDefinition LOAD_RECENT = new BooleanPreferenceDefinition(R.string.pref_loadrecent_id,
            R.string.pref_loadrecent_defvalue);

    BooleanPreferenceDefinition NIGHT_MODE = new BooleanPreferenceDefinition(R.string.pref_nightmode_id,
            R.string.pref_nightmode_defvalue);

    BooleanPreferenceDefinition BRIGHTNESS_NIGHT_MODE_ONLY = new BooleanPreferenceDefinition(
            R.string.pref_brightnessnightmodeonly_id, R.string.pref_brightnessnightmodeonly_defvalue);

    IntegerPreferenceDefinition BRIGHTNESS = new IntegerPreferenceDefinition(R.string.pref_brightness_id,
            R.string.pref_brightness_defvalue, R.string.pref_brightness_minvalue, R.string.pref_brightness_maxvalue);

    BooleanPreferenceDefinition KEEP_SCREEN_ON = new BooleanPreferenceDefinition(R.string.pref_keepscreenon_id,
            R.string.pref_keepscreenon_defvalue);

    EnumPreferenceDefinition<RotationType> ROTATION = new EnumPreferenceDefinition<RotationType>(RotationType.class,
            R.string.pref_rotation_id, R.string.pref_rotation_auto);

    BooleanPreferenceDefinition FULLSCREEN = new BooleanPreferenceDefinition(R.string.pref_fullscreen_id,
            R.string.pref_fullscreen_defvalue);

    BooleanPreferenceDefinition SHOW_TITLE = new BooleanPreferenceDefinition(R.string.pref_title_id,
            R.string.pref_title_defvalue);

    BooleanPreferenceDefinition SHOW_PAGE_IN_TITLE = new BooleanPreferenceDefinition(R.string.pref_pageintitle_id,
            R.string.pref_pageintitle_defvalue);

    BooleanPreferenceDefinition SHOW_ANIM_ICON = new BooleanPreferenceDefinition(R.string.pref_showanimicon_id,
            R.string.pref_showanimicon_defvalue);

    /* =============== Tap & Scroll settings =============== */

    BooleanPreferenceDefinition TAPS_ENABLED = new BooleanPreferenceDefinition(R.string.pref_tapsenabled_id,
            R.string.pref_tapsenabled_defvalue);

    IntegerPreferenceDefinition SCROLL_HEIGHT = new IntegerPreferenceDefinition(R.string.pref_scrollheight_id,
            R.string.pref_scrollheight_defvalue, R.string.pref_scrollheight_minvalue, R.string.pref_scrollheight_maxvalue);

    IntegerPreferenceDefinition TOUCH_DELAY = new IntegerPreferenceDefinition(R.string.pref_touchdelay_id,
            R.string.pref_touchdelay_defvalue, R.string.pref_touchdelay_minvalue, R.string.pref_touchdelay_maxvalue);

    /* =============== Tap & Keys settings =============== */

    StringPreferenceDefinition TAP_PROFILES = new StringPreferenceDefinition(R.string.pref_tapprofiles_id,
            R.string.pref_tapprofiles_defvalue);

    StringPreferenceDefinition KEY_BINDINGS = new StringPreferenceDefinition(R.string.pref_keys_binding_id,
            R.string.pref_keys_binding_defvalue);

    
    /* =============== Browser settings =============== */

    FileListPreferenceDefinition AUTO_SCAN_DIRS = new FileListPreferenceDefinition(R.string.pref_brautoscandir_id,
            R.string.pref_brautoscandir_defvalue);

}
