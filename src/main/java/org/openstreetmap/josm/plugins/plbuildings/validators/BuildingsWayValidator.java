package org.openstreetmap.josm.plugins.plbuildings.validators;

import org.openstreetmap.josm.data.osm.Way;

import java.util.HashSet;

public class BuildingsWayValidator {

    /**
     * It is helper function to make sure that building data (as closed way low-level)
     * hasn't been broken by import with replacing geometry feature
     */
    public static boolean isBuildingWayValid(Way building){
        if (!building.isClosed()){
            return false;
        }
        // First node always should be same as last node
        if (building.firstNode() != building.lastNode()){
            return false;
        }

        // Only 1 node must be duplicated (first and last).
        if (new HashSet<>(building.getNodes()).size() != (building.getNodes().size() - 1)){
            return false;
        }

        return true;
    }
}
