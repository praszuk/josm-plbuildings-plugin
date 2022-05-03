package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.plbuildings.command.ReplaceUpdateBuildingCommand;
import org.openstreetmap.josm.plugins.plbuildings.utils.UndoRedoUtils;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsAction extends JosmAction {
    static final String DESCRIPTION = tr("Buildings download action.");

    public BuildingsAction() {
        super(
                tr("Download building"),
                null,
                DESCRIPTION,
                Shortcut.registerShortcut(
                    "download:building",
                    tr("Download building"),
                    KeyEvent.VK_1,
                    Shortcut.CTRL_SHIFT
                ),
                true
        );
    }

    /**
     * Create bbox based on list of nodes and their positions.
     * It will produce bbox expanded by offset to e.g. match all very close nodes when importing
     * semidetached_house/terrace buildings
     * It works only for positive lat/lon values, because this plugin is only for Poland.
     */
    public static BBox getBBox(List<Node> nodes, double bboxOffset) {
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

    public static DataSet getBuildingsAtCurrentLocation(){
        LatLon latLonPoint = MainApplication.getMap().mapView.getLatLon(
            MainApplication.getMap().mapView.getMousePosition().getX(),
            MainApplication.getMap().mapView.getMousePosition().getY()
        );

        return BuildingsDownloader.downloadBuildings(latLonPoint, "bdot");
    }

    public static void performBuildingImport(DataSet currentDataSet) {
        DataSet importedBuildingsDataSet = getBuildingsAtCurrentLocation();
        if (importedBuildingsDataSet == null){
            Logging.warn("Connection error: Cannot import building!");
            return;
        }
        if (importedBuildingsDataSet.isEmpty()) {
            Logging.info("Imported empty dataset.");
            return;
        }

        List<Way> importedBuildingsCollection = importedBuildingsDataSet.getWays()
            .stream()
            .filter(way -> way.hasKey("building"))
            .collect(Collectors.toList());

        if (importedBuildingsCollection.isEmpty()){
            Logging.warn("Imported dataset with some data, but without buildings!");
            return;
        }
        // just get first building
        Way importedBuilding = importedBuildingsCollection.get(0);

        List<Node> newNodes = importedBuilding.getNodes().stream()
            .map(node -> new Node(node, true))
            .collect(Collectors.toList());

        // last node is wrongly duplicated after cloning operation, so remove it now
        // and add first node to the way below to get it as closed line (area)
        newNodes.remove(newNodes.size() - 1);

        Way newBuilding = new Way();
        newBuilding.cloneFrom(importedBuilding, false);
        importedBuildingsDataSet.clear();

        // Check if any building's node is very close to existing node – almost/same lat lon and replace it
        BBox bbox = getBBox(newNodes, BuildingsSettings.BBOX_OFFSET.get());

        List<Node> closeNodes = currentDataSet.searchNodes(bbox).stream()
            .filter(n -> !n.isDeleted())
            .collect(Collectors.toList());
        List<Node> buildingNodes = new ArrayList<>();
        List<Node> nodesToAddToDataSet = new ArrayList<>();

        AtomicInteger sameNodeCounter = new AtomicInteger();
        newNodes.forEach(newNode -> {
            Node sameNode = closeNodes.stream()
                .filter(closeNode -> isCloseNode(closeNode, newNode, BuildingsSettings.BBOX_OFFSET.get()))
                .findFirst().orElse(null);

            if (sameNode != null) {
                buildingNodes.add(sameNode);
                sameNodeCounter.getAndIncrement();
            } else {
                buildingNodes.add(newNode);
                nodesToAddToDataSet.add(newNode);
            }
        });

        // Whole building is a duplicate – contains the same nodes
        if (sameNodeCounter.get() == buildingNodes.size()) {
            return;
        }
        newBuilding.setNodes(buildingNodes);
        newBuilding.addNode(buildingNodes.get(0));

        List<Command> commands = new ArrayList<>();

        nodesToAddToDataSet.forEach(node -> commands.add(new AddCommand(currentDataSet, node)));
        commands.add(new AddCommand(currentDataSet, newBuilding));

        Collection<OsmPrimitive> selected = currentDataSet.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());

        SequenceCommand importBuildingSequenceCommand = new SequenceCommand(tr("Import building"), commands);
        UndoRedoHandler.getInstance().add(importBuildingSequenceCommand);
        Logging.debug("Imported new building {0}", newBuilding.getId());

        if (selected.size() == 1) {
            Way selectedBuilding = (Way) selected.toArray()[0];
            try {
                ReplaceUpdateBuildingCommand replaceUpdateBuildingCommand = new ReplaceUpdateBuildingCommand(
                    currentDataSet,
                    selectedBuilding,
                    newBuilding
                );
                UndoRedoHandler.getInstance().add(replaceUpdateBuildingCommand);
                Logging.debug("Updated building {0} with new data", selectedBuilding.getId());

            } catch (IllegalArgumentException ignored) {
                // If user cancel conflict window do nothing
                Notification note = new Notification(tr("Canceled merging buildings."));
                note.setIcon(JOptionPane.WARNING_MESSAGE);
                note.setDuration(Notification.TIME_SHORT);
                note.show();

                UndoRedoUtils.undoUntil(UndoRedoHandler.getInstance(), importBuildingSequenceCommand, true);
                Logging.debug(
                    "No building (id: {0}) update, caused: Cancel conflict dialog by user",
                    selectedBuilding.getId()
                );
            } catch (ReplaceGeometryException ignore) {
                // If selected building cannot be merged (e.g. connected ways/relation)
                Notification note = new Notification(tr(
                "Cannot merge buildings!" +
                        " Old building may be connected with some ways/relations" +
                        " or not whole area is downloaded."
                ));
                note.setIcon(JOptionPane.ERROR_MESSAGE);
                note.setDuration(Notification.TIME_SHORT);
                note.show();

                UndoRedoUtils.undoUntil(UndoRedoHandler.getInstance(), importBuildingSequenceCommand, true);
                Logging.debug(
                    "No building update (id: {0}), caused: Replacing Geometry from UtilPlugins2 error",
                    selectedBuilding.getId()
                );
            }

            currentDataSet.clearSelection();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        performBuildingImport(getLayerManager().getEditDataSet());
    }
}