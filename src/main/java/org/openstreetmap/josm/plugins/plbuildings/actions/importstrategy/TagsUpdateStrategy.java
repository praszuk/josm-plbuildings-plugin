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

public class TagsUpdateStrategy extends ImportStrategy {
    public TagsUpdateStrategy(
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
                ImportStatus.CANCELED, tr("Cannot perform building update tags without selected building.")
            );
            return null;
        }
        // Pre-check/modify import data section

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

        if (isBuildingValueSimplification(selectedBuilding, importedBuilding)) {
            String oldValue = selectedBuilding.get("building");
            String newValue = importedBuilding.get("building");

            importedBuilding.put("building", selectedBuilding.get("building"));
            Logging.info("Avoiding building details simplification ({0} -\\> {1})", oldValue, newValue);
        }

        if (isBuildingLevelsWithRoofEquals(selectedBuilding, importedBuilding)) {
            String oldValue = selectedBuilding.get("building:levels");
            String newValue = importedBuilding.get("building:levels");

            importedBuilding.put("building:levels", selectedBuilding.get("building:levels"));
            Logging.info(
                "Avoiding breaking building:levels caused by roof levels ({0} -\\> {1})",
                oldValue,
                newValue
            );
        }

        // general import section
        UpdateBuildingTagsCommand updateBuildingTagsCommand = new UpdateBuildingTagsCommand(
            currentDataSet,
            () -> selectedBuilding,
            importedBuilding
        );
        boolean isSuccess = updateBuildingTagsCommand.executeCommand();
        if (!isSuccess) {
            Logging.debug("Update tags building failed!");
            manager.setStatus(ImportStatus.IMPORT_ERROR, updateBuildingTagsCommand.getErrorReason());
            return null;
        }
        UndoRedoHandler.getInstance().add(updateBuildingTagsCommand, false);
        importStats.addImportWithReplaceCounter(1);
        Logging.debug("Updated building {0} with new data", selectedBuilding.getId());

        return selectedBuilding;
    }
}
