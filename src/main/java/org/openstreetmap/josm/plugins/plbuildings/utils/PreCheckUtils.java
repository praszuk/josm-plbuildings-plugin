package org.openstreetmap.josm.plugins.plbuildings.utils;

import static org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags.HOUSE_DETAILS;
import static org.openstreetmap.josm.plugins.plbuildings.data.BuildingsTags.LIVING_BUILDINGS;

import java.util.List;
import javax.annotation.Nonnull;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Logging;

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
    public static boolean isBuildingValueSimplification(@Nonnull OsmPrimitive current, @Nonnull OsmPrimitive newObj) {
        String currentValue = current.get("building");
        String newValue = newObj.get("building");

        if (currentValue == null) {
            return false;
        }
        if (newValue == null) {
            return true;
        }

        if (newValue.equals("house") && HOUSE_DETAILS.contains(currentValue)) {
            return true;
        }
        else if (newValue.equals("residential") && LIVING_BUILDINGS.contains(currentValue)) {
            return true;
        }

        else if (newValue.equals("outbuilding") && List.of("garage", "barn", "shed", "sty").contains(currentValue)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if new buildings:level is same as the current one basing on old building:levels and roof:levels
     * So above tags are required.
     * Examples:
     * – new: building:levels=2, old: building:levels=2 returns false (because it doesn't have roof levels)
     * – new: building:levels=2, old: building:levels=1, roof:levels=1 should return true
     * – new: building:levels=3, old: building:levels=1, roof:levels=1 should return false (1 level in addition)
     */
    public static boolean isBuildingLevelsWithRoofEquals(@Nonnull OsmPrimitive current,
                                                         @Nonnull OsmPrimitive newObj) {
        int newLevels;
        int oldLevels;
        int oldRoofLevels;

        try {
            newLevels = Integer.parseInt(newObj.get("building:levels"));
            oldLevels = Integer.parseInt(current.get("building:levels"));
            oldRoofLevels = Integer.parseInt(current.get("roof:levels"));
        } catch (NumberFormatException exception) {
            Logging.debug("Error with parsing levels: {0}", exception);
            return false;
        }

        if (newLevels == (oldLevels + oldRoofLevels)) {
            Logging.debug(
                "New levels are equals to old building:levels + old roof:levels ({0} = {1} + {2}.",
                newLevels,
                oldLevels,
                oldRoofLevels
            );
            return true;
        }

        return false;
    }
}
