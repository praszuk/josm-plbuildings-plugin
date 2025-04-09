package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.commands.CommandWithErrorReason.getLatestErrorReasonStatus;
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
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;

public class GeometryUpdateStrategy extends ImportStrategy {
    public GeometryUpdateStrategy(
        BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding
    ) {
        super(manager, importStats, importedBuilding);
    }

    @Override
    public Way performImport() throws ImportActionCanceledException {
        // Pre-check section
        Way selectedBuilding = manager.getSelectedBuilding();
        if (selectedBuilding == null) {
            throw new ImportActionCanceledException(
                tr("Cannot update building geometry without a selected building."),
                ImportStatus.CANCELED
            );
        }
        tryPreventImportIfDataFromSurvey();

        if (isBuildingDuplicate()) {
            throw new ImportActionCanceledException(
                tr("Duplicated building geometry. Canceling!"),
                ImportStatus.NO_UPDATE
            );
        }

        Logging.info("Importing new geometry!");
        AddBuildingGeometryCommand addBuildingGeometryCommand = new AddBuildingGeometryCommand(
            currentDataSet,
            importedBuilding
        );
        ReplaceBuildingGeometryCommand replaceBuildingGeometryCommand = new ReplaceBuildingGeometryCommand(
            currentDataSet,
            selectedBuilding,
            addBuildingGeometryCommand
        );

        List<Command> commands = Arrays.asList(addBuildingGeometryCommand, replaceBuildingGeometryCommand);
        SequenceCommand mergedGeometryBuildingSequence = new SequenceCommand(
            tr("Updated building geometry"),
            commands
        );
        boolean isSuccess = mergedGeometryBuildingSequence.executeCommand();
        if (!isSuccess) {
            Pair<String, ImportStatus> errorReasonStatus = getLatestErrorReasonStatus(commands);
            throw new ImportActionCanceledException(errorReasonStatus.a, errorReasonStatus.b);
        }
        UndoRedoHandler.getInstance().add(mergedGeometryBuildingSequence, false);
        importStats.addImportWithReplaceCounter(1);
        importStats.addImportWithGeometryUpdateCounter(1);
        Logging.debug("Updated building {0} with new geometry", selectedBuilding.getId());

        return selectedBuilding;
    }
}
