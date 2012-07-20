package org.emdev.fonts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.emdev.fonts.data.FontPack;

public abstract class AbstractFontProvider implements IFontProvider {

    protected final Map<String, FontPack> packs = new TreeMap<String, FontPack>();

    public void init() {
        final List<FontPack> load = load();
        for (final FontPack fp : load) {
            packs.put(fp.name, fp);
        }
    }

    @Override
    public Iterator<FontPack> iterator() {
        return packs.values().iterator();
    }

    @Override
    public FontPack getFontPack(final String name) {
        return packs.get(name);
    }

    protected abstract List<FontPack> load();

    protected boolean save() {
        return true;
    }

}
