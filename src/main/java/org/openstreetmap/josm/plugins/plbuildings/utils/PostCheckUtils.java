package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
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
        if (!BuildingsSettings.COMMON_BUILDING_TAGS.get().contains(primitive.get("building"))) {
            uncommon.put("building", primitive.get("building"));
        }
        BuildingsTags.UNCOMMON_NO_BUILDING_TAGS.stream()
            .filter(primitive::hasTag)
            .forEach(key -> uncommon.put(key, primitive.get(key)));

        return uncommon;
    }

    public static TagMap findLifecyclePrefixBuildingTags(OsmPrimitive primitive) {
        TagMap tags = new TagMap();
        if (primitive == null) {
            return tags;
        }
        primitive.getKeys().getTags().stream()
            .filter(t -> t.getKey().contains("building") || t.getKey().contains("roof"))
            .filter(t -> BuildingsTags.COMMON_LIFECYCLE_PREFIXES.stream()
                .anyMatch(prefix -> t.getKey().startsWith(prefix)))
            .forEach(t -> tags.put(t.getKey(), t.getValue()));

        return tags;
    }
}
