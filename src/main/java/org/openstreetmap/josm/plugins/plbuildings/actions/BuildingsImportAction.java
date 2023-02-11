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
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsDuplicateValidator;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.*;
import static org.openstreetmap.josm.tools.I18n.tr;

public class BuildingsImportAction extends JosmAction {
    static final String DESCRIPTION = tr("Import building at cursor position or replace/update selected.");
    static final String TITLE = tr("Download building");
    public BuildingsImportAction() {
        super(
            TITLE,
            (ImageProvider) null,
            DESCRIPTION,
            Shortcut.registerShortcut(
                "download:building",
                TITLE,
                KeyEvent.VK_1,
                Shortcut.CTRL_SHIFT
            ),
            false,
            String.format("%s:buildings_import", BuildingsPlugin.info.name),
            false
        );
    }

    public static LatLon getCurrentCursorLocation(){
        try {
            return MainApplication.getMap().mapView.getLatLon(
                MainApplication.getMap().mapView.getMousePosition().getX(),
                MainApplication.getMap().mapView.getMousePosition().getY()
            );
        }
        catch (NullPointerException exception){
            return null;
        }
    }

    /**
     * @return – selected building in give dataset or null
     */
    public static Way getSelectedBuilding(DataSet ds){
        Collection<OsmPrimitive> selected = ds.getSelected()
            .stream()
            .filter(osmPrimitive -> osmPrimitive.getType() == OsmPrimitiveType.WAY)
            .collect(Collectors.toList());
        return selected.size() == 1 ? (Way) selected.toArray()[0]:null;
    }

    /**
     * Flow:
     * Validate imported dataset
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
    public static void performBuildingImport(BuildingsImportManager manager) {
        DataSet currentDataSet = manager.getEditLayer();
        DataSet importedBuildingsDataSet = manager.getImportedData().get("--");

        BuildingsImportStats.getInstance().addTotalImportActionCounter(1);

        // Imported data validation and getting building
        if (importedBuildingsDataSet == null){
            Logging.warn("Downloading error: Cannot import building!");
            manager.setStatus(ImportStatus.CONNECTION_ERROR);
            return;
        }
        if (importedBuildingsDataSet.isEmpty()) {
            Logging.info("Imported empty dataset.");
            manager.setStatus(ImportStatus.NO_DATA);
            return;
        }

        List<Way> importedBuildingsCollection = importedBuildingsDataSet.getWays()
            .stream()
            .filter(way -> way.hasKey("building"))
            .collect(Collectors.toList());

        if (importedBuildingsCollection.isEmpty()){
            Logging.warn("Imported dataset with some data, but without buildings!");
            manager.setStatus(ImportStatus.NO_DATA);
            return;
        }
        Way importedBuilding = importedBuildingsCollection.get(0); // just get first building

        // Pre-check/modify import data section
        Way selectedBuilding = manager.getSelectedBuilding();
        if (selectedBuilding != null){
            if (hasSurveyValue(selectedBuilding)){
                manager.setStatus(ImportStatus.ACTION_REQUIRED);
                boolean isContinue = SurveyConfirmationDialog.show();
                if (!isContinue){
                    Logging.info("Canceled import with rejecting survey dialog confirmation.");
                    manager.setStatus(ImportStatus.CANCELED);
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
                manager.setStatus(ImportStatus.NO_UPDATE);
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
                    manager.setStatus(ImportStatus.IMPORT_ERROR);
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
                    manager.setStatus(ImportStatus.IMPORT_ERROR);
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
                    manager.setStatus(ImportStatus.IMPORT_ERROR);
                    return;
                }
                UndoRedoHandler.getInstance().add(mergedGeometryAndUpdatedTagsBuildingSequence, false);
                BuildingsImportStats.getInstance().addImportWithReplaceCounter(1);
                resultBuilding = selectedBuilding;
                Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
            }
        }
        manager.setResultBuilding(resultBuilding);
        currentDataSet.clearSelection();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DataSet currentDataSet = getLayerManager().getEditDataSet();

        // Get selection first – it must be got before starting downloading
        // to avoid changing incorrect building in future – which is possible if user importing so fast and
        // downloading takes longer then selecting next building to update
        Way selectedBuilding = getSelectedBuilding(currentDataSet);
        LatLon cursorLatLon = getCurrentCursorLocation();

        BuildingsImportManager buildingsImportManager = new BuildingsImportManager(
            currentDataSet,
            cursorLatLon,
            selectedBuilding
        );
        if (buildingsImportManager.getDataSourceProfile() == null){
            Logging.info("BuildingsImportAction canceled! No DataSourceProfile selected!");
            return;
        }
        buildingsImportManager.run();
    }
}