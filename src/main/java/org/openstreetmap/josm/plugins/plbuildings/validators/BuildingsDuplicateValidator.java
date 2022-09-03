package org.openstreetmap.josm.plugins.plbuildings.validators;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils;
import org.openstreetmap.josm.tools.Logging;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils.getBBox;
import static org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils.isCloseNode;


public class BuildingsDuplicateValidator {

    private static List<Node> getCloseNodes(DataSet dataSet, Way importedBuilding){
        BBox bbox = getBBox(importedBuilding.getNodes(), BuildingsSettings.BBOX_OFFSET.get());

        return dataSet.searchNodes(bbox).stream()
            .filter(n -> !n.isDeleted())
            .filter(SharedNodesUtils::isShareableNode)
            .collect(Collectors.toList());
    }

    /**
     * It uses simple detection algorithm of checking duplication. It should be enough for most cases:
     * It counts all close nodes (bbox of given newBuilding + small offset) as possible duplicated nodes.
     * Then it compares which Way object has the most duplicated nodes with given way
     * and if number of duplicated nodes if equal to nodes of given way, than it is treated a duplicate.
     *
     * @param dataSet – will be searched for close nodes/ways to compare it with given importedBuilding
     * @param importedBuilding new building to check is duplicate it must not be in the given dataSet else always true
     *
     * @return true if the building duplicating geometry with other object in the dataset else false
     */
    public static boolean isDuplicate(DataSet dataSet, Way importedBuilding){
        List<Node> closeNodes = getCloseNodes(dataSet, importedBuilding);

        if (closeNodes.size() < importedBuilding.getNodesCount() - 1){
            return false;
        }
        
        HashMap<Way, Integer> buildingSameNodesWithImportedBuildingCounter = new HashMap<>();
        importedBuilding.getNodes()
            .stream()
            .limit(importedBuilding.getNodesCount() - 1)
            .forEach(newNode -> closeNodes.stream()
            .filter(closeNode -> isCloseNode(closeNode, newNode, BuildingsSettings.BBOX_OFFSET.get()))
            .findFirst()  // should be the closest one, but it's sufficient for most cases
            .ifPresent(sameNode -> sameNode.getParentWays().forEach(parentWay -> {
                int counter = buildingSameNodesWithImportedBuildingCounter.getOrDefault(parentWay, 0);
                buildingSameNodesWithImportedBuildingCounter.put(parentWay, counter + 1);
        })));

        Logging.debug(
            "Duplicate checker: Buildings same nodes with imported building counter: {0}",
            buildingSameNodesWithImportedBuildingCounter
        );

        // Using max func, but it should be more precisely matched, but it is easier and sufficient for most cases
        int maxSharedNodesByCloseBuildingsSize = buildingSameNodesWithImportedBuildingCounter.values()
            .stream()
            .mapToInt(v->v)
            .max()
            .orElse(-1);

        return maxSharedNodesByCloseBuildingsSize == importedBuilding.getNodesCount() - 1;
    }
}
