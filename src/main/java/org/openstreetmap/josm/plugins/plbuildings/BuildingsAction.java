package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.plbuildings.command.AddSharedNodesBuildingCommand;
import org.openstreetmap.josm.plugins.plbuildings.command.ReplaceUpdateBuildingCommand;
import org.openstreetmap.josm.plugins.plbuildings.utils.UndoRedoUtils;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
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

        Collection<OsmPrimitive> selected = currentDataSet.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());

        AddSharedNodesBuildingCommand addSharedNodesBuildingCommand = new AddSharedNodesBuildingCommand(
            currentDataSet,
            importedBuilding
        );
        addSharedNodesBuildingCommand.executeCommand();
        Way newBuilding = addSharedNodesBuildingCommand.getCreatedBuilding();
        if (newBuilding == null){
            Logging.info("Duplicated building geometry. Canceled building import!");
            return;
        }
        UndoRedoHandler.getInstance().add(addSharedNodesBuildingCommand, false);

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

                UndoRedoUtils.undoUntil(UndoRedoHandler.getInstance(), addSharedNodesBuildingCommand, true);
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

                UndoRedoUtils.undoUntil(UndoRedoHandler.getInstance(), addSharedNodesBuildingCommand, true);
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