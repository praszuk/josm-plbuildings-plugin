package org.openstreetmap.josm.plugins.plbuildings.enums;

/**
 * It is used for decide when merging buildings from multiple datasets into one building
 * if there is a problematic case to handle.
 */
public enum CombineNearestStrategy {
    ASK_USER("ask_user"),
    CANCEL("cancel"),
    ACCEPT("accept"),
    ACCEPT_GEOMETRY("accept_geometry"),
    ACCEPT_TAGS("accept_tags");

    private final String text;

    CombineNearestStrategy(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static CombineNearestStrategy fromString(String text) {
        for (CombineNearestStrategy cns : CombineNearestStrategy.values()) {
            if (cns.text.equalsIgnoreCase(text)) {
                return cns;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
