package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.LIVING_BUILDINGS;


public class PostCheckUtils {
    /*
    Tags extracted from
    https://github.com/openstreetmap-polska/gugik2osm/blob/main/processing/sql/data/buildings_categories_mappings.csv
    4th column and filtered

    and added "construction"
    and added some LIVING_BUILDING

    2022-07-30
    They not need to be checked by mapper
     */
    public final static Set<String> COMMON_BUILDING_VALUES = Collections.unmodifiableSet(
        Stream.of(
            Stream.of(
                "apartments",
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
            ).collect(Collectors.toSet()),
            LIVING_BUILDINGS
        ).flatMap(Set::stream).collect(Collectors.toSet())
    );

    // Some imported data can contain other tags than building, it should be checked by mapper
    public final static List<String> UNCOMMON_NO_BUILDING_TAGS = Arrays.asList(
        "amenity",
        "leisure",
        "historic",
        "tourism"
    );

    public static boolean hasUncommonTags(OsmPrimitive primitive){
        if (primitive == null || !primitive.hasTag("building")){
            return false;
        }

        if (!COMMON_BUILDING_VALUES.contains(primitive.get("building"))){
            return true;
        }

        return UNCOMMON_NO_BUILDING_TAGS.stream().anyMatch(primitive::hasTag);
    }

}
