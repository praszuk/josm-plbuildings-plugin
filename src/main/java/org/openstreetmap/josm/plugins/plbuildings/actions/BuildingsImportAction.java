package org.openstreetmap.josm.plugins.plbuildings.actions;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsDownloader;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddSharedNodesBuildingCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceUpdateBuildingCommand;
import org.openstreetmap.josm.plugins.plbuildings.utils.UndoRedoUtils;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsDuplicateValidator;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.UserCancelException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION = tr("Buildings download action.");

    public BuildingsImportAction() {
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

    /**
     * Wrapper function copied from Utilsplugin2 ReplaceGeometryUtils.getTagConflictResolutionCommands
     *
     * @param newBuilding – building from which tags will be copied
     * @param selectedBuilding – building with which tags will be merged or updated
     * @return list of commands as updating tags
     * @throws UserCancelException if user close the window or reject possible tags conflict
     */
    static List<Command> updateTags(Way newBuilding, Way selectedBuilding) throws UserCancelException {
        Collection<OsmPrimitive> primitives = Arrays.asList(selectedBuilding, newBuilding);

        return CombinePrimitiveResolverDialog.launchIfNecessary(
            TagCollection.unionOfAllPrimitives(primitives),
            primitives,
            Collections.singleton(selectedBuilding)
        );
    }

    static void handleReplaceUpdateBuildingCommandException(ReplaceUpdateBuildingCommand cmd, Way selectedBuilding) {
        Notification note;

        if (cmd.getCancelException() instanceof IllegalArgumentException) {
            // If user cancel conflict window do nothing
            note = new Notification(tr("Canceled merging buildings."));
            note.setIcon(JOptionPane.WARNING_MESSAGE);

            Logging.debug(
                "No building (id: {0}) update, caused: Cancel conflict dialog by user",
                selectedBuilding.getId()
            );
        } else if(cmd.getCancelException() instanceof ReplaceGeometryException) {
            // If selected building cannot be merged (e.g. connected ways/relation)
            note = new Notification(tr(
            "Cannot merge buildings!" +
                " Old building may be connected with some ways/relations" +
                " or not whole area is downloaded."
            ));
            note.setIcon(JOptionPane.ERROR_MESSAGE);

            Logging.debug(
                "No building update (id: {0}), caused: Replacing Geometry from UtilPlugins2 error",
                selectedBuilding.getId()
            );
        } else if (cmd.getCancelException() instanceof DataIntegrityProblemException) {
            // If data integrity like nodes duplicated or first!=last has been somehow broken
            note = new Notification(tr(
                "Cannot merge buildings! Building has been wrongly replaced and data has been broken!"
            ));
            note.setIcon(JOptionPane.ERROR_MESSAGE);

            Logging.error(
                "No building update (id: {0}), caused: DataIntegrity with replacing error! Building: {1}",
                selectedBuilding.getId(),
                selectedBuilding
            );
        } else {
            note = new Notification(tr("Cannot merge buildings! Unknown error!"));
            Logging.error(
                "No building update (id: {0}), caused: Unknown error: {1}",
                selectedBuilding.getId(),
                cmd.getCancelException().getMessage()
            );
        }
        note.setDuration(Notification.TIME_SHORT);
        note.show();
    }
    /**
     * Flow:
     * Download building from server to virtual dataset
     * Check if it's unique (no geometry duplicate):
     * -- duplicate:
     * ---- check if 1 building is selected:
     * ------ selected -> try to update tags
     * ------ not selected -> end
     * -- not duplicate:
     * ---- check if 1 building is selected:
     * ------ selected -> try to replace geometry and update tags
     * ------ not selected -> just import new building
     */
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
        Way selectedBuilding = selected.size() == 1 ? (Way) selected.toArray()[0]:null;

        if (BuildingsDuplicateValidator.isDuplicate(currentDataSet, importedBuilding)){
            if (selectedBuilding == null){
                Logging.info("Duplicated building geometry. Not selected any building. Canceling!");
                return;
            }

            else {
                Logging.info("Duplicated building geometry. Trying to update tags!");
                try {
                    List<Command> updateTagCommands = updateTags(importedBuilding, selectedBuilding);
                    if (updateTagCommands.isEmpty()){
                        Logging.debug("Duplicated building geometry and tags! Canceling!");
                        return;
                    }
                    UndoRedoHandler.getInstance().add(new SequenceCommand(
                        tr("Updated building tags"),
                        updateTagCommands
                    ));
                    BuildingsImportStats.getInstance().addImportWithReplaceCounter(1);
                } catch (UserCancelException exception){
                    Logging.debug(
                        "No building tags (id: {0}) update, caused: Cancel conflict dialog by user",
                        selectedBuilding.getId()
                    );
                }

            }
        }
        else {
            if (selectedBuilding == null){
                Logging.info("Importing new building (without geometry replacing)!");
                UndoRedoHandler.getInstance().add(new AddSharedNodesBuildingCommand(
                    currentDataSet,
                    importedBuilding
                ));
                BuildingsImportStats.getInstance().addImportCounter(1);
            }
            else {
                Logging.info("Importing new building (with geometry replacing and tags update)!");

                AddSharedNodesBuildingCommand addSharedNodesBuildingCommand = new AddSharedNodesBuildingCommand(
                    currentDataSet,
                    importedBuilding
                );
                addSharedNodesBuildingCommand.executeCommand();

                Way newBuilding = addSharedNodesBuildingCommand.getCreatedBuilding();
                UndoRedoHandler.getInstance().add(addSharedNodesBuildingCommand, false);

                ReplaceUpdateBuildingCommand replaceUpdateBuildingCommand = new ReplaceUpdateBuildingCommand(
                    currentDataSet,
                    selectedBuilding,
                    newBuilding
                );
                boolean isReplacedUpdated = replaceUpdateBuildingCommand.executeCommand();
                if (isReplacedUpdated){
                    UndoRedoHandler.getInstance().add(replaceUpdateBuildingCommand, false);
                    BuildingsImportStats.getInstance().addImportWithReplaceCounter(1);
                    Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
                }
                else {
                    UndoRedoUtils.undoUntil(UndoRedoHandler.getInstance(), addSharedNodesBuildingCommand, true);
                    handleReplaceUpdateBuildingCommandException(replaceUpdateBuildingCommand, selectedBuilding);
                }
            }
        }
        currentDataSet.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        performBuildingImport(getLayerManager().getEditDataSet());
    }
}