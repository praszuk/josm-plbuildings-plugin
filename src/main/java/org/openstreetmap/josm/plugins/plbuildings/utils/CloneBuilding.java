package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class CloneBuilding {
    /**
     * @param building – to clone
     * @return cloned building – tags, nodes and id are new. if building is null, then returns null
     */
    public static OsmPrimitive cloneBuilding(OsmPrimitive building) {
        if (building == null) {
            return null;
        }
        Way newBuilding = new Way();
        ((Way) (building)).getNodes().forEach(n -> newBuilding.addNode(new Node(n.getCoor())));
        building.getKeys().forEach(newBuilding::put);

        return newBuilding;
    }
}
