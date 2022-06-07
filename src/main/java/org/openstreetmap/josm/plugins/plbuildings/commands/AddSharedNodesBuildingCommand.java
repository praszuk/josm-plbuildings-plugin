package org.openstreetmap.josm.plugins.plbuildings.commands;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportBuildingDuplicateException;
import org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils;
import org.openstreetmap.josm.tools.Logging;

import java.util.*;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils.getBBox;
import static org.openstreetmap.josm.plugins.plbuildings.utils.SharedNodesUtils.isCloseNode;
import static org.openstreetmap.josm.tools.I18n.tr;


public class AddSharedNodesBuildingCommand extends Command  {
    /**
     * Add building to dataset and merge with existing nodes.
     * It could not copy all new nodes. It will create only the new which cannot be reused.
     */

    private final Way importedBuilding; // raw new building with duplicated nodes etc.
    private final DataSet dataSet;

    private List<Node> createdNodes; // only missing nodes (without existing nodes – not all of created building nodes)
    private Way createdBuilding;

    public AddSharedNodesBuildingCommand(DataSet dataSet, Way importedBuilding) {
        super(dataSet);
        this.dataSet = dataSet;
        this.importedBuilding = importedBuilding;
    }

    public Way getCreatedBuilding() {
        return createdBuilding;
    }


    @Override
    public void fillModifiedData(
            Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added
    ) {
        added.add((OsmPrimitive) createdNodes);
        added.add(createdBuilding);
    }

    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        List<OsmPrimitive> participatingPrimitives = new ArrayList<>(createdBuilding.getNodes());
        participatingPrimitives.add(createdBuilding);

        return participatingPrimitives;
    }

    @Override
    public void undoCommand() {
        dataSet.removePrimitive(createdBuilding);
        createdNodes.forEach(dataSet::removePrimitive);
    }

    @Override
    public boolean executeCommand() {
        if (createdBuilding == null){ // It's necessary for redo handling
            try {
                prepareBuilding();
            } catch (ImportBuildingDuplicateException e) {
                return false;
            }
        }

        addBuilding();

        return true;
    }

    @Override
    public String getDescriptionText() {
        return tr("Add imported building");
    }

    /**
     * Prepare building data (nodes and way) to add it to the dataset
     * It will check which nodes should be skipped from import and should be reused from already existing nodes
     * it checks for that close nodes with BBOX_OFFSET from BuildingSettings
     * then it will construct a new building object based on above checks
     * and add it missing nodes and building (with existing and new nodes) to dataset.
     */
    private void prepareBuilding() throws ImportBuildingDuplicateException {
        List<Node> newNodes = importedBuilding.getNodes().stream()
            .map(node -> new Node(node, true))
            .collect(Collectors.toList());

        // last node is wrongly duplicated after cloning operation, so remove it now
        // and add first node to the way below to get it as closed line (area)
        newNodes.remove(newNodes.size() - 1);

        Way newBuilding = new Way();
        newBuilding.cloneFrom(importedBuilding, false);

        // Check if any building's node is very close to existing node – almost/same lat lon and replace it
        BBox bbox = getBBox(newNodes, BuildingsSettings.BBOX_OFFSET.get());

        List<Node> closeNodes = dataSet.searchNodes(bbox).stream()
            .filter(n -> !n.isDeleted())
            .filter(SharedNodesUtils::isShareableNode)
            .collect(Collectors.toList());
        LinkedHashSet<Node> buildingNodes = new LinkedHashSet<>();
        LinkedHashSet<Node> nodesToAddToDataSet = new LinkedHashSet<>();

        HashMap<Way, Integer> buildingSharedNodesWithImportedBuildingCounter = new HashMap<>();
        newNodes.forEach(newNode -> {
            Node sameNode = closeNodes.stream()
                .filter(closeNode -> isCloseNode(closeNode, newNode, BuildingsSettings.BBOX_OFFSET.get()))
                .findFirst().orElse(null);

            if (sameNode != null) {
                buildingNodes.add(sameNode);
                sameNode.getParentWays().forEach(parentWay -> {
                    int counter = buildingSharedNodesWithImportedBuildingCounter.getOrDefault(parentWay, 0);
                    buildingSharedNodesWithImportedBuildingCounter.put(parentWay, counter + 1);
                });
            } else {
                buildingNodes.add(newNode);
                nodesToAddToDataSet.add(newNode);
            }
        });
        Logging.debug(
            "Buildings shared nodes with imported building counter: {0}",
            buildingSharedNodesWithImportedBuildingCounter
        );
        int maxSharedNodesByCloseBuildingsSize = buildingSharedNodesWithImportedBuildingCounter.values()
            .stream()
            .mapToInt(v->v)
            .max()
            .orElse(-1);

        // Whole building is a duplicate – contains the same nodes
        if (maxSharedNodesByCloseBuildingsSize == buildingNodes.size()) {
            throw new ImportBuildingDuplicateException();
        }
        newBuilding.setNodes(new ArrayList<>(buildingNodes));
        newBuilding.addNode(newBuilding.getNode(0));

        createdBuilding = newBuilding;
        createdNodes = new ArrayList<>(nodesToAddToDataSet);
    }

    public void addBuilding(){
        createdNodes.forEach(dataSet::addPrimitive);
        dataSet.addPrimitive(createdBuilding);
        Logging.debug("Added new building {0}", createdBuilding.getId());
    }

}
