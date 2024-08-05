package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.commands.CommandWithErrorReason.getLatestErrorReason;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.List;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.commands.AddBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.ReplaceBuildingGeometryCommand;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.Logging;

public class FullImportStrategy extends ImportStrategy {
    public FullImportStrategy(BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding) {
        super(manager, importStats, importedBuilding);
    }

    private Way handleImportUpdateTagsOnly(Way selectedBuilding) throws ImportActionCanceledException {
        Logging.info("Duplicated building geometry. Trying to update tags!");
        UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
            currentDataSet,
            () -> selectedBuilding,
            importedBuilding
        );
        boolean isUpdated = updateBuildingTagsCommand.executeCommand();
        if (!isUpdated) {
            throw new ImportActionCanceledException(
                updateBuildingTagsCommand.getErrorReason(),
                ImportStatus.IMPORT_ERROR
            );
        }
        UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
        importStats.addImportWithTagsUpdateCounter(1);
        Logging.info("Updated selected building tags (without geometry replacing)!");

        return selectedBuilding;

    }

    private Way handleImportNewBuilding() throws ImportActionCanceledException {
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
            throw new ImportActionCanceledException(getLatestErrorReason(commands), ImportStatus.IMPORT_ERROR);
        }
        UndoRedoHandler.getInstance().add(importedNewBuildingSequence, false);
        importStats.addImportNewBuildingCounter(1);
        Logging.debug("Imported building: {0}", addBuildingGeometryCommand.getResultBuilding().getId());

        return addBuildingGeometryCommand.getResultBuilding();
    }

    private Way handleImportAndUpdateGeometryAndTags(Way selectedBuilding) throws ImportActionCanceledException {
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
            throw new ImportActionCanceledException(getLatestErrorReason(commands), ImportStatus.IMPORT_ERROR);
        }
        UndoRedoHandler.getInstance().add(mergedGeometryAndUpdatedTagsBuildingSequence, false);
        importStats.addImportWithReplaceCounter(1);
        Logging.debug("Updated building {0} with new data", selectedBuilding.getId());
        return selectedBuilding;
    }

    @Override
    public Way performImport() throws ImportActionCanceledException {
        // Pre-check/modify import data section
        Way selectedBuilding = manager.getSelectedBuilding();
        if (selectedBuilding != null) {
            tryPreventImportIfDataFromSurvey();
            trySimplifyBuildingValue();
            tryPreventBreakingRoofLevels();
        }

        // general import section
        if (isBuildingDuplicate()) {
            if (selectedBuilding == null) {
                throw new ImportActionCanceledException(
                    tr("Duplicated building geometry, but not selected any building. Canceling!"),
                    ImportStatus.NO_UPDATE
                );
            }
            return handleImportUpdateTagsOnly(selectedBuilding);
        }

        if (selectedBuilding == null) {
            return handleImportNewBuilding();
        } else {
            return handleImportAndUpdateGeometryAndTags(selectedBuilding);
        }
    }
}
