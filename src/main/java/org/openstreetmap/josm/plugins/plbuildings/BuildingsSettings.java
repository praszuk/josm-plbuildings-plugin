package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

import java.util.List;

public class BuildingsSettings {

    public static final DoubleProperty BBOX_OFFSET = new DoubleProperty(
        "plbuildings.bbox_offset",
        0.0000005
    );

    /**
     * It is used to generate points for BuildingsOverlapDetector Smaller == better accuracy but slower. Approximation:
     * 0.00001 ~100 points ~0.02 seconds per building
     * 0.000001 ~10_000 points ~0.06 seconds per building <- RECOMMENDED
     * 0.0000001 ~1_000_000 points ~1 seconds per building
     */
    public static final DoubleProperty OVERLAP_DETECT_FREQ_DEGREE_STEP = new DoubleProperty(
        "plbuildings.overlap_detect_freq_degree_step",
        0.000001
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

    public static final BooleanProperty DATA_SOURCE_PROFILES_AUTO_REFRESH = new BooleanProperty(
        "plbuildings.data_source_profiles_auto_refresh",
        true
    );

}
