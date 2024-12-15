package org.openstreetmap.josm.plugins.plbuildings.data;

import java.util.Set;

public class UnallowedTags {

    /**
     * Most common tag keys which should trigger validation error if selected object contain any.
     */
    public static final Set<String> UNALLOWED_SELECTED_OBJECT_KEYS = Set.of(
        "highway", "landuse", "natural", "boundary", "waterway", "railway", "barrier"
    );
}
