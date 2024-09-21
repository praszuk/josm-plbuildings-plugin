package org.openstreetmap.josm.plugins.plbuildings.utils;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.TagMap;

public class SharedNodesUtils {

    /**
     * Create bbox based on list of nodes and their positions.
     * It will produce bbox expanded by offset to e.g. match all very close nodes when importing
     * semidetached_house/terrace buildings
     */
    public static BBox getBbox(List<Node> nodes, double bboxOffset) {
        BBox bbox = new BBox();
        nodes.forEach(bbox::add);

        LatLon topLeft = bbox.getTopLeft();
        LatLon bottomRight = bbox.getBottomRight();
        bbox.add(new LatLon(topLeft.lat() + bboxOffset, topLeft.lon() - bboxOffset));
        bbox.add(new LatLon(bottomRight.lat() - bboxOffset, bottomRight.lon() + bboxOffset));

        return bbox;
    }

    /**
     * Check if node1 is close to node2 where max distance is maxOffset (inclusive)
     * Both (latitude and longitude) values must be close to return true.
     */
    public static boolean isCloseNode(Node node1, Node node2, double maxOffset) {
        boolean isLatOk = Math.abs(node1.lat() - node2.lat()) <= maxOffset;
        boolean isLonOk = Math.abs(node1.lon() - node2.lon()) <= maxOffset;

        return isLatOk && isLonOk;
    }

    /**
     * Check if node can be shared with importing building. Check membership of node and parent object's tags.
     *
     * @return true if node can be shared (reused) with importing building else false
     */
    public static boolean isShareableNode(Node node) {
        if (!node.isReferredByWays(1)) { // not member of any way
            return false;
        }
        return SharedNodesUtils.isNodeStickToWayWithTag(node, new AbstractMap.SimpleEntry<>("building", "*"));
    }


    /**
     * @param node which is element of ways to check theirs tags
     * @param entryRequired key-value pair as OSM tag which is checked if exists in any shared (referred) way.
     *     To check only key, use "*" or empty string as value.
     * @return true if at least 1 way has given tag (entryRequired) else false
     */
    public static boolean isNodeStickToWayWithTag(Node node,
                                                  Map.Entry<String, String> entryRequired) {
        return node.isReferredByWays(1) && node.getParentWays().stream().anyMatch(parentWay -> {
            TagMap wayTags = parentWay.getKeys();

            if (wayTags.containsKey(entryRequired.getKey())) {
                if (entryRequired.getValue().equals("*") || entryRequired.getValue().isEmpty()) {
                    return true;
                } else {
                    return wayTags.get(entryRequired.getKey()).equals(entryRequired.getValue());
                }
            }
            return false;
        });
    }
}
