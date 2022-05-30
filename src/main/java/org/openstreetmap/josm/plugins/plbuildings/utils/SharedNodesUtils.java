package org.openstreetmap.josm.plugins.plbuildings.utils;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.TagMap;

import java.util.Map;

public class SharedNodesUtils {


    /**
     *
     * @param node which is element of ways to check theirs tags
     * @param entryRequired key-value pair as OSM tag which is checked if exists in any shared (referred) way.
     * To check only key, use "*" or empty string as value.
     * @return true if at least 1 way has given tag (entryRequired) else false
     */
    public static boolean isNodeStickToWayWithTag(Node node, Map.Entry<String, String> entryRequired){
        return node.isReferredByWays(1) && node.getParentWays().stream().anyMatch(parentWay -> {
            TagMap wayTags = parentWay.getKeys();

            if (wayTags.containsKey(entryRequired.getKey())){
                if (entryRequired.getValue().equals("*") || entryRequired.getValue().isEmpty()){
                    return true;
                }
                else return wayTags.get(entryRequired.getKey()).equals(entryRequired.getValue());
            }
            return false;
        });
    }
}
