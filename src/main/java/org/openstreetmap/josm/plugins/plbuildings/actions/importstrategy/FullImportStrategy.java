package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.commands.CommandWithErrorReason.getLatestErrorReason;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.hasSurveyValue;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.isBuildingLevelsWithRoofEquals;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.isBuildingValueSimplification;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.List;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector;
import org.openstreetmap.josm.plugins.plbuildings.utils.NearestBuilding;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

public class FullImportStrategy extends ImportStrategy {


    public FullImportStrategy(BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding) {
        super(manager, importStats, importedBuilding);
    }

    public void tryPreventImportIfDataFromSurvey() throws UserCancelException {
        if (hasSurveyValue(manager.getSelectedBuilding())) {
            manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
            boolean isContinue = SurveyConfirmationDialog.show();
            if (!isContinue) {
                throw new UserCancelException("Canceled import by rejected survey dialog confirmation");
            }
        }
    }

    public void trySimplifyBuildingValue() {
        if (isBuildingValueSimplification(manager.getSelectedBuilding(), importedBuilding)) {
            String oldValue = manager.getSelectedBuilding().get("building");
            String newValue = importedBuilding.get("building");

            importedBuilding.put("building", manager.getSelectedBuilding().get("building"));
            Logging.info("Avoiding building details simplification ({0} -\\> {1})", oldValue, newValue);
        }
    }

    public void tryPreventBreakingRoofLevels() {
        if (isBuildingLevelsWithRoofEquals(manager.getSelectedBuilding(), importedBuilding)) {
            String oldValue = manager.getSelectedBuilding().get("building:levels");
            String newValue = importedBuilding.get("building:levels");

            importedBuilding.put("building:levels", manager.getSelectedBuilding().get("building:levels"));
            Logging.info(
                "Avoiding breaking building:levels caused by roof levels ({0} -\\> {1})",
                oldValue,
                newValue
            );
        }
    }

    public double calcMaxOverlapPercentageForCloseBuildings() {
        List<OsmPrimitive> closeBuildings = NearestBuilding.getCloseBuildings(
            currentDataSet, importedBuilding.getBBox()
        );
        double overlapPercentage = 0.0;
        for (OsmPrimitive closeBuilding : closeBuildings) {
            overlapPercentage = Math.max(
                overlapPercentage, BuildingsOverlapDetector.detect(closeBuilding, importedBuilding)
            );
        }
        return overlapPercentage;
    }

    @Override
    public Way performImport() {
        // Pre-check/modify import data section
        Way selectedBuilding = manager.getSelectedBuilding();
        if (selectedBuilding != null) {
            try {
                tryPreventImportIfDataFromSurvey();
            } catch (UserCancelException e) {
                Logging.info("Canceled import with rejecting survey dialog confirmation.");
                manager.setStatus(
                    ImportStatus.CANCELED, tr("Canceled import by rejected survey dialog confirmation.")
                );
                return null;
            }
            trySimplifyBuildingValue();
            tryPreventBreakingRoofLevels();
        }

        // general import section
        Way resultBuilding;

        double overlapPercentage = calcMaxOverlapPercentageForCloseBuildings();
        if (overlapPercentage > BuildingsSettings.OVERLAP_DETECT_DUPLICATED_BUILDING_THRESHOLD.get()) {
            if (selectedBuilding == null) {
                Logging.info("Duplicated building geometry. Not selected any building. Canceling!");
                manager.setStatus(
                    ImportStatus.NO_UPDATE,
                    tr("Duplicated building geometry, but not selected any building. Canceling!")
                );
                return null;
            } else {
                Logging.info("Duplicated building geometry. Trying to update tags!");
                UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
                    currentDataSet,
                    () -> selectedBuilding,
                    importedBuilding
                );
                boolean isUpdated = updateBuildingTagsCommand.executeCommand();
                if (!isUpdated) {
                    Logging.info("Error with updating tags!");
                    manager.setStatus(ImportStatus.IMPORT_ERROR, updateBuildingTagsCommand.getErrorReason());
                    return null;
                }
                UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
                importStats.addImportWithTagsUpdateCounter(1);
                resultBuilding = selectedBuilding;
                Logging.info("Updated selected building tags (without geometry replacing)!");
            }
        } else {
            if (selectedBuilding == null) {
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

                List<Command> commands =
                    Arrays.asList(addBuildingGeometryCommand, updateBuildingTagsCommand);
                SequenceCommand importedNewBuildingSequence = new SequenceCommand(
                    tr("Imported a new building"), commands
                );
                boolean isSuccess = importedNewBuildingSequence.executeCommand();
                if (!isSuccess) {
                    Logging.debug("Import of a new building failed!");
                    manager.setStatus(ImportStatus.IMPORT_ERROR, getLatestErrorReason(commands));
                    return null;
                }
                UndoRedoHandler.getInstance().add(importedNewBuildingSequence, false);
                importStats.addImportNewBuildingCounter(1);
                resultBuilding = addBuildingGeometryCommand.getResultBuilding();
                Logging.debug("Imported building: {0}",
                    addBuildingGeometryCommand.getResultBuilding().getId());
            } else {
                Logging.info("Importing new building (with geometry replacing and tags update)!");

                AddBuildingGeometryCommand addBuildingGeometryCommand = new AddBuildingGeometryCommand(
                    currentDataSet,
                    importedBuilding
                );
                ReplaceBuildingGeometryCommand replaceBuildingGeometryCommand =
                    new ReplaceBuildingGeometryCommand(
                        currentDataSet,
                        selectedBuilding,
                        addBuildingGeometryCommand
                    );
                // Here it can be checked for detached/semi/terrace
                UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
                    currentDataSet,
                    () -> selectedBuilding,
                    importedBuilding
                );

                List<Command> commands = Arrays.asList(
                    addBuildingGeometryCommand,
                    replaceBuildingGeometryCommand,
                    updateBuildingTagsCommand
                );
                SequenceCommand mergedGeometryAndUpdatedTagsBuildingSequence = new SequenceCommand(
                    tr("Updated building tags and geometry"),
                    commands
                );
                boolean isSuccess = mergedGeometryAndUpdatedTagsBuildingSequence.executeCommand();
                if (!isSuccess) {
                    Logging.debug("Update (geometry and tags) building failed!");
                    manager.setStatus(ImportStatus.IMPORT_ERROR, getLatestErrorReason(commands));
                    return null;
                }
                UndoRedoHandler.getInstance().add(mergedGeometryAndUpdatedTagsBuildingSequence, false);
                importStats.addImportWithReplaceCounter(1);
                resultBuilding = selectedBuilding;
                Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
            }
        }
        return resultBuilding;
    }
}
