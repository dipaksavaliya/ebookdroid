package org.ebookdroid.common.keysbinding;

import org.ebookdroid.R;
import org.ebookdroid.common.log.LogContext;
import org.ebookdroid.common.settings.AppSettings;
import org.ebookdroid.common.settings.SettingsManager;

import _android.util.SparseArrayEx;

import android.view.KeyEvent;

import org.emdev.ui.actions.ActionEx;
import org.emdev.utils.LengthUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KeysBindingManager {

    private static final LogContext LCTX = LogContext.ROOT.lctx("Actions");

    private static SparseArrayEx<ActionRef> actions = new SparseArrayEx<KeysBindingManager.ActionRef>();

    public static void loadFromSettings(final AppSettings newSettings) {
        actions.clear();

        boolean fromJSON = false;
        final String str = newSettings.getKeysBinding();
        if (LengthUtils.isNotEmpty(str)) {
            try {
                fromJSON(str);
                fromJSON = true;
            } catch (final Throwable ex) {
                LCTX.e("Error on tap configuration load: ", ex);
            }
        }

        if (!fromJSON) {
            addAction(R.id.actions_verticalConfigScrollUp, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_VOLUME_UP);
            addAction(R.id.actions_verticalConfigScrollDown, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN);

            persist();
        }
    }

    public static void persist() {
        try {
            final JSONObject json = toJSON();
            SettingsManager.getAppSettings().updateKeysBinding(json.toString());
        } catch (final JSONException ex) {
            LCTX.e("Unexpected error: ", ex);
        }
    }

    public static JSONObject toJSON() throws JSONException {
        final JSONObject object = new JSONObject();
        final JSONArray array = new JSONArray();
        for (final ActionRef ref : actions) {
            array.put(ref.toJSON());
        }
        object.put("actions", array);
        return object;
    }

    public static Integer getAction(final KeyEvent ev) {
        final ActionRef ref = actions.get(ev.getKeyCode());
        return ref != null && ref.enabled ? Integer.valueOf(ref.id) : null;
    }

    public static void addAction(final int id, final int... keys) {
        for (int key : keys) {
            actions.append(key, new ActionRef(key, id, true));
        }
    }

    private static void fromJSON(final String str) throws JSONException {
        final JSONObject root = new JSONObject(str);

        final JSONArray list = root.getJSONArray("actions");
        for (int pIndex = 0; pIndex < list.length(); pIndex++) {
            final JSONObject p = list.getJSONObject(pIndex);
            final ActionRef ref = ActionRef.fromJSON(p);
            actions.append(ref.code, ref);
        }
    }

    public static class ActionRef {

        public final int code;
        public final int id;
        public final String name;
        public boolean enabled;

        public ActionRef(final int code, final int id, final boolean enabled) {
            this.code = code;
            this.id = id;
            this.name = ActionEx.getActionName(id);
            this.enabled = enabled;
        }

        public static ActionRef fromJSON(final JSONObject json) throws JSONException {
            final int code = json.getInt("code");
            final String name = json.getString("name");
            final Integer id = ActionEx.getActionId(name);
            return new ActionRef(code, id, true);
        }

        public JSONObject toJSON() throws JSONException {
            final JSONObject object = new JSONObject();
            object.put("code", code);
            object.put("name", name);
            object.put("enabled", enabled);
            return object;
        }

        @Override
        public String toString() {
            return "(" + code + ", " + name + ", " + enabled + ")";
        }
    }

    public static class KeyInfo {

        public final int code;
        public final String label;

        public KeyInfo(final int code, final String label) {
            this.code = code;
            this.label = label;
        }

    }
}
