package org.openstreetmap.josm.plugins.plbuildings.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildingsTags {

    public static final Set<String> LIVING_BUILDINGS = Set.of(
        "house", "apartments", "detached", "semidetached_house", "terrace", "residential"
    );

    public static final Set<String> HOUSE_DETAILS = Set.of("detached", "semidetached_house", "terrace");

    /*
    Tags extracted from
    https://github.com/openstreetmap-polska/gugik2osm/blob/main/processing/sql/data/buildings_categories_mappings.csv
    4th column and filtered

    and added "construction"
    and added some LIVING_BUILDING

    2022-07-30
    They not need to be checked by mapper
     */
    public static final Set<String> DEFAULT_COMMON_BUILDING_VALUES = Collections.unmodifiableSet(
        Stream.concat(
            Set.of(
                "bungalow",
                "cabin",
                "commercial",
                "farmhouse",
                "garage",
                "hangar",
                "house",
                "industrial",
                "outbuilding",
                "retail",
                "service",
                "warehouse",
                "yes",

                "construction"
            ).stream(),
            LIVING_BUILDINGS.stream()
        ).collect(Collectors.toSet())
    );

    // Some imported data can contain other tags than building, it should be checked by mapper
    public static final List<String> UNCOMMON_NO_BUILDING_TAGS = Arrays.asList(
        "amenity",
        "leisure",
        "historic",
        "tourism"
    );

}
