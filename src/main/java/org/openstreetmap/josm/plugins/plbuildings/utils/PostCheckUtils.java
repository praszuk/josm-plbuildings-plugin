package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags;


public class PostCheckUtils {

    public static boolean hasUncommonTags(OsmPrimitive primitive){
        if (primitive == null || !primitive.hasTag("building")){
            return false;
        }

        if (!BuildingsTags.COMMON_BUILDING_VALUES.contains(primitive.get("building"))){
            return true;
        }

        return BuildingsTags.UNCOMMON_NO_BUILDING_TAGS.stream().anyMatch(primitive::hasTag);
    }

}
