package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

import javax.annotation.Nonnull;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.HOUSE_DETAILS;

public class PreCheckUtils {
    /**
     * Checks if object has any "survey" value e.g. in tags (keys and values)
     * e.g. source=survey or survey:date=2022-01-01
     */
    public static boolean hasSurveyValue(@Nonnull OsmPrimitive primitive) {
        return primitive.getKeys().toString().contains("survey");
    }

    /**
     * Checks if new building value is a simplification of existing value.
     * E.g. detached->house returns true
     */
    public static boolean isBuildingValueSimplification(@Nonnull OsmPrimitive current, @Nonnull OsmPrimitive newObj){
        String currentValue = current.get("building");
        String newValue = newObj.get("building");

        if (currentValue == null){
            return false;
        }
        if (newValue == null){
            return true;
        }

        if (newValue.equals("house") && HOUSE_DETAILS.contains(currentValue)){
            return true;
        }

        return false;
    }
}
