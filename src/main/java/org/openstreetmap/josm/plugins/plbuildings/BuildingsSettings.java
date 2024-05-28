package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags.DEFAULT_COMMON_BUILDING_VALUES;

import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.plbuildings.data.CombineNearestStrategy;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

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

    public static final StringProperty COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY = new StringProperty(
        "plbuildings.combine_nearest_building_one_ds_strategy",
        CombineNearestStrategy.ASK_USER.toString()
    );

    public static final StringProperty COMBINE_NEAREST_BUILDING_OVERLAP_STRATEGY = new StringProperty(
        "plbuildings.combine_nearest_building_both_ds_strategy",
        CombineNearestStrategy.ASK_USER.toString()
    );
    /**
     * Percentage value. It is used when both datasets are available and plugin try to
     * combine buildings from both into one. It measures overlapping percentage firstly,
     * so if both buildings don't overlap above given threshold then use COMBINE_NEAREST_BUILDING_BOTH_DS_STRATEGY
     * to decide
     */
    public static final DoubleProperty COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD =
        new DoubleProperty(
            "plbuildings.combine_nearest_building_overlap_threshold",
            60.0
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

    public static final StringProperty NOTIFIABLE_IMPORT_STATUSES = new StringProperty(
        "plbuildings.notifiable_import_statuses",
        "{}"
    );

    public static final BooleanProperty UNCOMMON_TAGS_CHECK = new BooleanProperty(
        "plbuildings.uncommon_tags_check", true
    );

    public static final IntegerProperty CONNECTION_TIMEOUT = new IntegerProperty(
        "plbuildings.connection_timeout_ms", 10 * 1000
    );

    public static final ListProperty COMMON_BUILDING_TAGS = new ListProperty(
        "plbuildings.common_building_tags", new ArrayList<>(DEFAULT_COMMON_BUILDING_VALUES)
    );
}
