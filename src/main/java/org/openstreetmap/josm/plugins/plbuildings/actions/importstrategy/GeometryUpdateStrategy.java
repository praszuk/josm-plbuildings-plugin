package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.commands.CommandWithErrorReason.getLatestErrorReason;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.hasSurveyValue;
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
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector;
import org.openstreetmap.josm.plugins.plbuildings.utils.NearestBuilding;
import org.openstreetmap.josm.tools.Logging;

public class GeometryUpdateStrategy extends ImportStrategy {
    public GeometryUpdateStrategy(
        BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding
    ) {
        super(manager, importStats, importedBuilding);
    }

    @Override
    public Way performImport() {
        // TODO refactor
        // Pre-check section
        Way selectedBuilding = manager.getSelectedBuilding();
        if (selectedBuilding == null) {
            manager.setStatus(
                ImportStatus.CANCELED, tr("Cannot perform building update geometry without selected building.")
            );
            return null;
        }
        if (hasSurveyValue(selectedBuilding)) {
            manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
            boolean isContinue = SurveyConfirmationDialog.show();
            if (!isContinue) {
                Logging.info("Canceled import with rejecting survey dialog confirmation.");
                manager.setStatus(ImportStatus.CANCELED,
                    tr("Canceled import by rejected survey dialog confirmation."));
                return null;
            }
        }

        List<OsmPrimitive> closeBuildings = NearestBuilding.getCloseBuildings(
            currentDataSet, importedBuilding.getBBox()
        );
        double overlapPercentage = 0.0;
        for (OsmPrimitive closeBuilding : closeBuildings) {
            overlapPercentage = Math.max(
                overlapPercentage, BuildingsOverlapDetector.detect(closeBuilding, importedBuilding)
            );
        }

        if (overlapPercentage > BuildingsSettings.OVERLAP_DETECT_DUPLICATED_BUILDING_THRESHOLD.get()) {
            Logging.info("Duplicated building geometry. Canceling!");
            manager.setStatus(ImportStatus.NO_UPDATE, tr("Duplicated building geometry. Canceling!"));
            return null;
        }

        Logging.info("Importing new geometry!");
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

        List<Command> commands = Arrays.asList(addBuildingGeometryCommand, replaceBuildingGeometryCommand);
        SequenceCommand mergedGeometryBuildingSequence = new SequenceCommand(
            tr("Updated building geometry"),
            commands
        );
        boolean isSuccess = mergedGeometryBuildingSequence.executeCommand();
        if (!isSuccess) {
            Logging.debug("Update building geometry failed!");
            manager.setStatus(ImportStatus.IMPORT_ERROR, getLatestErrorReason(commands));
            return null;
        }
        UndoRedoHandler.getInstance().add(mergedGeometryBuildingSequence, false);
        importStats.addImportWithReplaceCounter(1);
        Logging.debug("Updated building {0} with new geometry", selectedBuilding.getId());

        return selectedBuilding;
    }
}
