package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class PreCheckUtils {
    /**
     * Checks if object has any "survey" value e.g. in tags (keys and values)
     * e.g. source=survey or survey:date=2022-01-01
     */
    public static boolean hasSurveyValue(OsmPrimitive primitive) {
        if (primitive == null){
            return false;
        }

        return primitive.getKeys().toString().contains("survey");
    }
}
