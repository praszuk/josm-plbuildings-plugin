package org.openstreetmap.josm.plugins.plbuildings.utils;

import java.util.Set;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags;

public class TagConflictUtils {

    /**
     * Try to remove some conflicts which can be avoided using function isTagConflictCanBeSkipped
     * which implementation of the "tag conflict decision resolver"
     * If pattern is matched, then tags (TagCollection) will be updated for specific key and old value will be
     * replaced with empty string.
     *
     * @param tags TagCollection of source and target primitives
     * @param target primitive to which tags will be pasted/merged
     * @param source primitive from which tags will be copied
     */

    public static void resolveTagConflictsDefault(TagCollection tags, OsmPrimitive target, OsmPrimitive source) {
        if (tags == null || source == null || target == null) {
            return;
        }

        Set<String> conflictKeys = tags.getKeysWithMultipleValues();

        conflictKeys.forEach(conflictKey -> {
            String currentValue = target.get(conflictKey);
            String newValue = source.get(conflictKey);
            if (isTagConflictCanBeSkipped(conflictKey, currentValue, newValue)) {
                tags.remove(new Tag(conflictKey, currentValue));
                tags.add(new Tag(conflictKey, "")); // Empty value to skip conflict dialog
            }
        });
    }

    /**
     * Implementation of the "tag conflict decision resolver"
     * It contains logic which decide if conflict can be skipped.
     *
     * @param key conflict tag key
     * @param currentValue tag value of existing (target) primitive which can be optionally replaced by newValue
     * @param newValue tag value of new (source) primitive which can replace currentValue
     * @return true if conflict can be skipped else false then it must be resolved in other way
     */
    static boolean isTagConflictCanBeSkipped(String key, String currentValue, String newValue) {
        switch (key) {
            case "building":
                if (currentValue.equals("yes") && !newValue.equals("construction")) {
                    return true;
                }
                else if (currentValue.equals("construction")) {
                    return true;
                }
                // farm_auxiliary is more like deprecated in Poland
                else if (currentValue.equals("farm_auxiliary") && newValue.equals("outbuilding")) {
                    return true;
                }
                // residential can be changed to any living building
                else if (currentValue.equals("residential") && BuildingsTags.LIVING_BUILDINGS.contains(newValue)) {
                    return true;
                }
                // details of house
                else if (currentValue.equals("house") && BuildingsTags.HOUSE_DETAILS.contains(newValue)) {
                    return true;
                }
                break;

            case "source":
            case "source:building":
            case "source:geometry":
                return !currentValue.contains("survey");

            default:
                return false;
        }
        return false;
    }

}