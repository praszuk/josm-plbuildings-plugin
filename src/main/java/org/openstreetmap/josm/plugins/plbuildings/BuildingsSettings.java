package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

import java.util.List;

public class BuildingsSettings {

    public static final DoubleProperty BBOX_OFFSET = new DoubleProperty(
        "plbuildings.bbox_offset",
        0.0000005
    );
    public static final DoubleProperty SEARCH_DISTANCE = new DoubleProperty(
        "plbuildings.search_distance",
        3.0 // meters
    );

    public static final StringProperty IMPORT_STATS = new StringProperty(
        "plbuildings.import_stats",
        "e30="  // "{}" (base64) – empty JSON
    );

    public static final StringProperty DATA_SOURCE_SERVERS = new StringProperty(
        "plbuildings.data_source_servers",
            DataSourceServer.toJson(List.of(new DataSourceServer(
                "plbuildings",
                "https://josm-plbuildings-server.openstreetmap.org.pl/api/v2"
            ))).toString()
    );

    public static final StringProperty DATA_SOURCE_PROFILES = new StringProperty(
        "plbuildings.data_source_profiles",
        "[]"
    );

}
