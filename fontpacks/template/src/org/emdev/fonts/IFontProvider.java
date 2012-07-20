package org.emdev.fonts;

import org.emdev.fonts.data.FontFamilyType;
import org.emdev.fonts.data.FontPack;
import org.emdev.fonts.data.FontStyle;
import org.emdev.fonts.typeface.TypefaceEx;

public interface IFontProvider extends Iterable<FontPack> {

    FontPack getFontPack(final String name);

    TypefaceEx getTypeface(FontPack fp, FontFamilyType type, FontStyle style);

}
