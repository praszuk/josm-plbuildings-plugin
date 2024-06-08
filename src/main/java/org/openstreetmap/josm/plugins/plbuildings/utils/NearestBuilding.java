package org.openstreetmap.josm.plugins.plbuildings.utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class NearestBuilding {
    /**
     * @param dataSet dataset with buildings
     * @param latLon  cursor/start location
     * @return nearest building (naive – to the nearest building node)
     */
    public static OsmPrimitive getNearestBuilding(DataSet dataSet, LatLon latLon) {
        return dataSet.getWays()
            .stream()
            .filter(w -> w.hasTag("building"))
            .min(Comparator.comparingDouble((Way w) -> LatLonToWayDistance.minDistance(latLon, w)))
            .orElse(null);
    }

    /**
     * @param currentDataSet dataset with buildings
     * @param bbox – search bbox for buildings – if at least one node is within bbox it counts
     * @return close buildings to the given bbox (not sorted)
     */
    public static List<OsmPrimitive> getCloseBuildings(DataSet currentDataSet, BBox bbox) {
        return currentDataSet.getWays()
            .stream()
            .filter(way -> way.hasTag("building"))
            .filter(way -> way.getNodes().stream().anyMatch(node -> bbox.contains(node.getCoor())))
            .collect(Collectors.toList());
    }
}
