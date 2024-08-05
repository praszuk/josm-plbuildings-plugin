package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.tools.Logging;

public class TagsUpdateStrategy extends ImportStrategy {
    public TagsUpdateStrategy(
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
                tr("Cannot perform building update tags without selected building."),
                ImportStatus.CANCELED
            );
        }
        // Pre-check/modify import data section
        tryPreventImportIfDataFromSurvey();
        trySimplifyBuildingValue();
        tryPreventBreakingRoofLevels();

        // general import section
        UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
            currentDataSet,
            () -> selectedBuilding,
            importedBuilding
        );
        boolean isSuccess = updateBuildingTagsCommand.executeCommand();
        if (!isSuccess) {
            throw new ImportActionCanceledException(
                updateBuildingTagsCommand.getErrorReason(),
                ImportStatus.IMPORT_ERROR
            );
        }
        UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
        importStats.addImportWithReplaceCounter(1);
        Logging.debug("Updated building {0} with new data", selectedBuilding.getId());

        return selectedBuilding;
    }
}
