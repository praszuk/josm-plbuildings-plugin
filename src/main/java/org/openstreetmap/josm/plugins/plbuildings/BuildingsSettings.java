package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

import java.util.Arrays;

public class BuildingsSettings {

    public static final DoubleProperty BBOX_OFFSET = new DoubleProperty(
        "plbuildings.bbox_offset",
        0.0000005
    );
    public static final StringProperty SERVER_URL = new StringProperty(
        "plbuildings.server_url",
        "https://josm-plbuildings-server.openstreetmap.org.pl/api/v1/buildings"
    );
    public static final DoubleProperty SEARCH_DISTANCE = new DoubleProperty(
        "plbuildings.search_distance",
        3.0 // meters
    );

    public static final ListProperty REPLACE_BUILDING_TAG_NO_CONFLICT = new ListProperty(
    "plbuildings.replace_building_tag_no_conflict",
        Arrays.asList(
            "building", "yes",
            "building", "farm_auxiliary",  // Not used anymore. It was used instead of building=outbuilding
            "source", "*"
        )
    );

    public static final StringProperty IMPORT_STATS = new StringProperty(
        "plbuildings.import_stats",
        "e30="  // "{}" (base64) – empty JSON
    );

}
