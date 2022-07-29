package org.openstreetmap.josm.plugins.plbuildings.actions;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsDownloader;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsDuplicateValidator;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.hasSurveyValue;
import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION = tr("Import building at cursor position or replace/update selected.");
    static final String TITLE = tr("Download building");
    public BuildingsImportAction() {
        super(
            TITLE,
            null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                "download:building",
                TITLE,
                KeyEvent.VK_1,
                Shortcut.CTRL_SHIFT
            ),
            true
        );
    }

    public static DataSet getBuildingsAtCurrentLocation(){
        try {
            LatLon latLonPoint = MainApplication.getMap().mapView.getLatLon(
                MainApplication.getMap().mapView.getMousePosition().getX(),
                MainApplication.getMap().mapView.getMousePosition().getY()
            );
            return BuildingsDownloader.downloadBuildings(latLonPoint, "bdot");
        }
        catch (NullPointerException exception){
            return null;
        }
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
        BuildingsImportStats.getInstance().addTotalImportActionCounter(1);
        DataSet importedBuildingsDataSet = getBuildingsAtCurrentLocation();
        if (importedBuildingsDataSet == null){
            Logging.warn("Downloading error: Cannot import building!");
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

        if (hasSurveyValue(selectedBuilding)){
            boolean isContinue = SurveyConfirmationDialog.show();
            if (!isContinue){
                Logging.info("Canceled import with rejecting survey dialog confirmation.");
                return;
            }
        }

        // if (importedBuilding.hasTag("building", "house")){importedBuilding.put("building", "detached");}
        if (BuildingsDuplicateValidator.isDuplicate(currentDataSet, importedBuilding)){
            if (selectedBuilding == null){
                Logging.info("Duplicated building geometry. Not selected any building. Canceling!");
                return;
            }

            else {
                Logging.info("Duplicated building geometry. Trying to update tags!");
                UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
                    currentDataSet,
                    () -> selectedBuilding,
                    importedBuilding
                );
                boolean isUpdated = updateBuildingTagsCommand.executeCommand();
                if (!isUpdated){
                    Logging.info("Error with updating tags!");
                    return;
                }
                UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
                BuildingsImportStats.getInstance().addImportWithTagsUpdateCounter(1);
                Logging.info("Updated selected building tags (without geometry replacing)!");
            }
        }
        else {
            if (selectedBuilding == null){
                Logging.info("Importing new building (without geometry replacing)!");
                AddBuildingGeometryCommand addBuildingGeometryCommand = new AddBuildingGeometryCommand(
                    currentDataSet,
                    importedBuilding
                );
                // Here it can be checked for detached/semi/terrace
                UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
                    currentDataSet,
                    addBuildingGeometryCommand,
                    importedBuilding
                );

                SequenceCommand importedANewBuildingSequence = new SequenceCommand(
                    tr("Imported a new building"),
                    Arrays.asList(addBuildingGeometryCommand, updateBuildingTagsCommand)
                );
                boolean isSuccess = importedANewBuildingSequence.executeCommand();
                if(!isSuccess){
                    Logging.debug("Import of a new building failed!");
                    return;
                }
                UndoRedoHandler.getInstance().add(importedANewBuildingSequence, false);
                BuildingsImportStats.getInstance().addImportNewBuildingCounter(1);
                Logging.debug("Imported building: {0}", addBuildingGeometryCommand.getResultBuilding().getId());
            }
            else {
                Logging.info("Importing new building (with geometry replacing and tags update)!");

                AddBuildingGeometryCommand addBuildingGeometryCommand = new AddBuildingGeometryCommand(
                    currentDataSet,
                    importedBuilding
                );
                ReplaceBuildingGeometryCommand replaceBuildingGeometryCommand = new ReplaceBuildingGeometryCommand(
                    currentDataSet,
                    selectedBuilding,
                    addBuildingGeometryCommand
                );
                // Here it can be checked for detached/semi/terrace
                UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
                    currentDataSet,
                    ()->selectedBuilding,
                    importedBuilding
                );

                SequenceCommand mergedGeometryAndUpdatedTagsBuildingSequence = new SequenceCommand(
                    tr("Updated building tags and geometry"),
                    Arrays.asList(
                        addBuildingGeometryCommand,
                        replaceBuildingGeometryCommand,
                        updateBuildingTagsCommand
                    )
                );
                boolean isSuccess = mergedGeometryAndUpdatedTagsBuildingSequence.executeCommand();
                if(!isSuccess){
                    Logging.debug("Update (geometry and tags) building failed!");
                    return;
                }
                UndoRedoHandler.getInstance().add(mergedGeometryAndUpdatedTagsBuildingSequence, false);
                BuildingsImportStats.getInstance().addImportWithReplaceCounter(1);
                Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
            }
        }
        currentDataSet.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        performBuildingImport(getLayerManager().getEditDataSet());
    }
}