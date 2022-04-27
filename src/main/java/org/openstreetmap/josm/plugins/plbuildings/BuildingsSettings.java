package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;

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


}
