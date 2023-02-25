package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Way;


public class LatLonToWayDistance {
    /**
     * It uses naive algorithm and measure euclidean distance to each node (walls are not considered).
     * @return min distance to any way's node or Double.MAX_VALUE if there is no nodes
     */
    public static double minDistance(LatLon latLon, Way way){
        return way.getNodes()
            .stream()
            .map((node -> node.getCoor().distance(latLon)))
            .sorted()
            .findFirst()
            .orElse(Double.MAX_VALUE);
    }
}
