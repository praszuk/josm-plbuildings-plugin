package org.openstreetmap.josm.plugins.plbuildings.enums;

/**
 * It is used to decide when merging buildings from multiple datasets into one building and one data source missing
 */
public enum CombineNearestOneDsStrategy {
    ASK_USER("ask_user"),
    CANCEL("cancel"),
    ACCEPT("accept");

    private final String text;

    CombineNearestOneDsStrategy(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static CombineNearestOneDsStrategy fromString(String text) {
        for (CombineNearestOneDsStrategy cns : CombineNearestOneDsStrategy.values()) {
            if (cns.text.equalsIgnoreCase(text)) {
                return cns;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
