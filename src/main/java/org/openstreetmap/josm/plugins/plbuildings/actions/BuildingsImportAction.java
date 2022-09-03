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
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.gui.UncommonTagDialog;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsDuplicateValidator;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.annotation.Nonnull;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.DOWNLOADING;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils.hasUncommonTags;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.*;
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
     * Helper function to updating GUI status from action
     */
    private static void updateGuiStatus(@Nonnull ImportStatus status){
        if (BuildingsPlugin.buildingsToggleDialog == null)  // for tests and no-gui execution of method
            return;

        boolean autoChangeToDefault;
        switch(status) {
            case IDLE:
            case DOWNLOADING:
            case ACTION_REQUIRED:
                autoChangeToDefault = false;
                break;
            case DONE:
            case NO_DATA:
            case NO_UPDATE:
            case CANCELED:
            case CONNECTION_ERROR:
            case IMPORT_ERROR:
            default: // DONE, NO_DATA, NO_UPDATE
                autoChangeToDefault = true;
        }
        BuildingsPlugin.buildingsToggleDialog.setStatus(status, autoChangeToDefault);
    }

    /**
     * Helper function to updating GUI latest tags from action
     */
    private static void updateGuiTags(OsmPrimitive resultBuilding, boolean hasUncommonTags){
        if (BuildingsPlugin.buildingsToggleDialog == null)  // for tests and no-gui execution of method
            return;

        BuildingsPlugin.buildingsToggleDialog.updateTags(
            resultBuilding.getKeys().getOrDefault("building", ""),
            resultBuilding.getKeys().getOrDefault("building:levels", ""),
            hasUncommonTags
        );
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
        // Preparation and obtaining import data/selection section
        BuildingsImportStats.getInstance().addTotalImportActionCounter(1);
        updateGuiStatus(DOWNLOADING);
        DataSet importedBuildingsDataSet = getBuildingsAtCurrentLocation();
        if (importedBuildingsDataSet == null){
            Logging.warn("Downloading error: Cannot import building!");
            updateGuiStatus(ImportStatus.CONNECTION_ERROR);
            return;
        }
        if (importedBuildingsDataSet.isEmpty()) {
            Logging.info("Imported empty dataset.");
            updateGuiStatus(ImportStatus.NO_DATA);
            return;
        }

        List<Way> importedBuildingsCollection = importedBuildingsDataSet.getWays()
            .stream()
            .filter(way -> way.hasKey("building"))
            .collect(Collectors.toList());

        if (importedBuildingsCollection.isEmpty()){
            Logging.warn("Imported dataset with some data, but without buildings!");
            updateGuiStatus(ImportStatus.NO_DATA);
            return;
        }
        Way importedBuilding = importedBuildingsCollection.get(0); // just get first building

        Collection<OsmPrimitive> selected = currentDataSet.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());
        Way selectedBuilding = selected.size() == 1 ? (Way) selected.toArray()[0]:null;

        // Pre-check/modify import data section
        if (selectedBuilding != null){
            if (hasSurveyValue(selectedBuilding)){
                updateGuiStatus(ImportStatus.ACTION_REQUIRED);
                boolean isContinue = SurveyConfirmationDialog.show();
                if (!isContinue){
                    Logging.info("Canceled import with rejecting survey dialog confirmation.");
                    updateGuiStatus(ImportStatus.CANCELED);
                    return;
                }
            }

            if (isBuildingValueSimplification(selectedBuilding, importedBuilding)){
                String oldValue = selectedBuilding.get("building");
                String newValue = importedBuilding.get("building");

                importedBuilding.put("building", selectedBuilding.get("building"));
                Logging.info("Avoiding building details simplification ({0} -\\> {1})", oldValue, newValue);
            }

            if (isBuildingLevelsWithRoofEquals(selectedBuilding, importedBuilding)){
                String oldValue = selectedBuilding.get("building:levels");
                String newValue = importedBuilding.get("building:levels");

                importedBuilding.put("building:levels", selectedBuilding.get("building:levels"));
                Logging.info(
                    "Avoiding breaking building:levels caused by roof levels ({0} -\\> {1})",
                    oldValue,
                    newValue
                );
            }
        }
        // temp for testing
        // if (importedBuilding.hasTag("building", "house")){importedBuilding.put("building", "detached");}

        // general import section
        Way resultBuilding;
        if (BuildingsDuplicateValidator.isDuplicate(currentDataSet, importedBuilding)){
            if (selectedBuilding == null){
                Logging.info("Duplicated building geometry. Not selected any building. Canceling!");
                updateGuiStatus(ImportStatus.NO_UPDATE);
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
                    updateGuiStatus(ImportStatus.IMPORT_ERROR);
                    return;
                }
                UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
                BuildingsImportStats.getInstance().addImportWithTagsUpdateCounter(1);
                resultBuilding = selectedBuilding;
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
                    updateGuiStatus(ImportStatus.IMPORT_ERROR);
                    return;
                }
                UndoRedoHandler.getInstance().add(importedANewBuildingSequence, false);
                BuildingsImportStats.getInstance().addImportNewBuildingCounter(1);
                resultBuilding = addBuildingGeometryCommand.getResultBuilding();
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
                    updateGuiStatus(ImportStatus.IMPORT_ERROR);
                    return;
                }
                UndoRedoHandler.getInstance().add(mergedGeometryAndUpdatedTagsBuildingSequence, false);
                BuildingsImportStats.getInstance().addImportWithReplaceCounter(1);
                resultBuilding = selectedBuilding;
                Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
            }
        }
        currentDataSet.clearSelection();

        // post-check section
        boolean hasUncommonTags = false;
        if (hasUncommonTags(resultBuilding)){
            updateGuiStatus(ImportStatus.ACTION_REQUIRED);
            hasUncommonTags = true;
            UncommonTagDialog.show();
        }

        updateGuiStatus(ImportStatus.DONE);
        updateGuiTags(resultBuilding, hasUncommonTags);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        MainApplication.worker.execute(() -> performBuildingImport(getLayerManager().getEditDataSet()));
    }
}