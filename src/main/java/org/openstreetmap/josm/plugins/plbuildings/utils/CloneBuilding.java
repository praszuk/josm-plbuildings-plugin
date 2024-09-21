package org.openstreetmap.josm.plugins.plbuildings.utils;

import java.util.List;
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

        List<Node> buildingNodes = ((Way) (building)).getNodes();
        buildingNodes.stream().limit(buildingNodes.size() - 1).forEach(n -> newBuilding.addNode(new Node(n.getCoor())));
        newBuilding.addNode(newBuilding.firstNode()); // first node must be same as last to close way

        building.getKeys().forEach(newBuilding::put);
        return newBuilding;
    }
}
