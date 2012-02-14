package org.ebookdroid.common.settings.types;

public enum FontSize {
    /**
     * 
     */
    TINY(0.5f),
    /**
     * 
     */
    SMALL(0.707f),
    /**
     * 
     */
    NORMAL(1.0f),
    /**
     * 
     */
    LARGE(1.414f),
    /**
     * 
     */
    HUGE(2.0f);

    public final float factor;

    private FontSize(final float factor) {
        this.factor = factor;
    }

    /**
     * Gets the by resource value.
     * 
     * @param resValue
     *            the resource value
     * @return the enum value or @null
     */
    public static FontSize getByResValue(final String resValue) {
        for (final FontSize fs : values()) {
            if (fs.name().equalsIgnoreCase(resValue)) {
                return fs;
            }
        }
        return NORMAL;
    }
}
