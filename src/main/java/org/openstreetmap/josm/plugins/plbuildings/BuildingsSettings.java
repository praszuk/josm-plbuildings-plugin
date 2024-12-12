package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags.DEFAULT_COMMON_BUILDING_VALUES;

import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.EnumProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.ListProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportMode;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;

public class BuildingsSettings {
    static final String SETTING_PREFIX = "plbuildings.";

    public static final DoubleProperty BBOX_OFFSET = new DoubleProperty(
        SETTING_PREFIX + "bbox_offset",
        0.0000005
    );

    /**
     * It is used to generate points for BuildingsOverlapDetector Smaller == better accuracy but slower. Approximation:
     * 0.00001 ~100 points ~0.02 seconds per building
     * 0.000001 ~10_000 points ~0.06 seconds per building <- RECOMMENDED
     * 0.0000001 ~1_000_000 points ~1 seconds per building
     */
    public static final DoubleProperty OVERLAP_DETECT_FREQ_DEGREE_STEP = new DoubleProperty(
        SETTING_PREFIX + "overlap_detect_freq_degree_step",
        0.000005
    );

    public static final DoubleProperty OVERLAP_DETECT_DUPLICATED_BUILDING_THRESHOLD = new DoubleProperty(
        SETTING_PREFIX + "overlap_detect_duplicated_building_threshold", 98.0
    );

    public static final StringProperty COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY = new StringProperty(
        SETTING_PREFIX + "combine_nearest_building_one_ds_strategy",
        CombineNearestOneDsStrategy.ASK_USER.toString()
    );

    public static final StringProperty COMBINE_NEAREST_BUILDING_OVERLAPPING_STRATEGY = new StringProperty(
        SETTING_PREFIX + "combine_nearest_building_both_ds_strategy",
        CombineNearestOverlappingStrategy.ASK_USER.toString()
    );
    /**
     * Percentage value. It is used when both datasets are available and plugin try to
     * combine buildings from both into one. It measures overlapping percentage firstly,
     * so if both buildings don't overlap above given threshold then use COMBINE_NEAREST_BUILDING_BOTH_DS_STRATEGY
     * to decide
     */
    public static final DoubleProperty COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD =
        new DoubleProperty(
            SETTING_PREFIX + "combine_nearest_building_overlap_threshold",
            60.0
        );

    public static final StringProperty IMPORT_STATS = new StringProperty(
        SETTING_PREFIX + "import_stats",
        "e30="  // "{}" (base64) – empty JSON
    );

    public static final StringProperty DATA_SOURCE_SERVERS = new StringProperty(
        SETTING_PREFIX + "data_source_servers",
        DataSourceServer.toJson(List.of(new DataSourceServer(
            "plbuildings",
            "https://plbuildings.niewnen.net/api/v2"
        ))).toString()
    );

    public static final StringProperty DATA_SOURCE_PROFILES = new StringProperty(
        SETTING_PREFIX + "data_source_profiles",
        "[]"
    );

    /** first element: unique server name, second element: unique profile name */
    public static final ListProperty CURRENT_DATA_SOURCE_PROFILE = new ListProperty(
        SETTING_PREFIX + "current_data_source_profile", new ArrayList<>()
    );

    public static final BooleanProperty DATA_SOURCE_PROFILES_AUTO_REFRESH = new BooleanProperty(
        SETTING_PREFIX + "data_source_profiles_auto_refresh",
        true
    );

    /** keeps the state if notification should appear or not */
    public static final StringProperty NOTIFICATION_STATES = new StringProperty(
        SETTING_PREFIX + "notification_states",
        "[]"
    );

    public static final BooleanProperty UNCOMMON_TAGS_CHECK = new BooleanProperty(
        SETTING_PREFIX + "uncommon_tags_check", true
    );

    public static final IntegerProperty CONNECTION_TIMEOUT = new IntegerProperty(
        SETTING_PREFIX + "connection_timeout_ms", 10 * 1000
    );

    public static final ListProperty COMMON_BUILDING_TAGS = new ListProperty(
        SETTING_PREFIX + "common_building_tags", new ArrayList<>(DEFAULT_COMMON_BUILDING_VALUES)
    );

    public static final EnumProperty<ImportMode> IMPORT_MODE = new EnumProperty<>(
        SETTING_PREFIX + "import_mode", ImportMode.class, ImportMode.FULL
    );

    public static final BooleanProperty AUTOREMOVE_SOURCE_GEOPORTAL_GOV_PL = new BooleanProperty(
        SETTING_PREFIX + "autoremove_source_geoportal_gov_pl", true
    );
}
