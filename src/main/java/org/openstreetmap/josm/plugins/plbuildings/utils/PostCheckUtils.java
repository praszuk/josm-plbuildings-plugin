package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags;


public class PostCheckUtils {

    /**
     * Checks primitives tags for finding uncommon tags.
     *
     * @param primitive object to check
     * @return TagMap with found uncommon tags or empty TagMap
     */
    public static TagMap findUncommonTags(OsmPrimitive primitive) {
        TagMap uncommon = new TagMap();
        if (primitive == null || !primitive.hasTag("building")) {
            return uncommon;
        }
        if (!BuildingsTags.COMMON_BUILDING_VALUES.contains(primitive.get("building"))) {
            uncommon.put("building", primitive.get("building"));
        }
        BuildingsTags.UNCOMMON_NO_BUILDING_TAGS.stream()
            .filter(primitive::hasTag)
            .forEach(key -> uncommon.put(key, primitive.get(key)));

        return uncommon;
    }

}
