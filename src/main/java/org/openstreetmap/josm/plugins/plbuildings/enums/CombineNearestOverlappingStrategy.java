package org.openstreetmap.josm.plugins.plbuildings.enums;

/**
 * It is used to decide when merging buildings from multiple datasets into one building and both data sources
 * are available, but they are not overlapping enough.
 */
public enum CombineNearestOverlappingStrategy {
    ASK_USER("ask_user"),
    CANCEL("cancel"),
    MERGE_BOTH("merge_both"),
    ACCEPT_GEOMETRY_SOURCE("accept_geometry_source"),
    ACCEPT_TAGS_SOURCE("accept_tags_source");

    private final String text;

    CombineNearestOverlappingStrategy(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static CombineNearestOverlappingStrategy fromString(String text) {
        for (CombineNearestOverlappingStrategy cns : CombineNearestOverlappingStrategy.values()) {
            if (cns.text.equalsIgnoreCase(text)) {
                return cns;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
