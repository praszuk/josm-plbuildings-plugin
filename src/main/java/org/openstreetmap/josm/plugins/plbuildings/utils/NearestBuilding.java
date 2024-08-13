package org.openstreetmap.josm.plugins.plbuildings.utils;

import java.util.Comparator;
import org.openstreetmap.josm.data.coor.LatLon;
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
}
