package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.hasSurveyValue;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.isBuildingLevelsWithRoofEquals;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.isBuildingValueSimplification;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.gui.SurveyConfirmationDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector;
import org.openstreetmap.josm.plugins.plbuildings.utils.NearestBuilding;
import org.openstreetmap.josm.tools.Logging;

public abstract class ImportStrategy {
    protected final BuildingsImportManager manager;
    protected final BuildingsImportStats importStats;
    protected final DataSet currentDataSet;
    protected final Way importedBuilding;

    public ImportStrategy(BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding) {
        this.manager = manager;
        this.importStats = importStats;
        this.currentDataSet = manager.getEditLayer();
        this.importedBuilding = importedBuilding;
    }

    public abstract Way performImport() throws ImportActionCanceledException;

    public void tryPreventImportIfDataFromSurvey() throws ImportActionCanceledException {
        if (hasSurveyValue(manager.getSelectedBuilding())) {
            manager.setStatus(ImportStatus.ACTION_REQUIRED, null);
            boolean isContinue = SurveyConfirmationDialog.show();
            if (!isContinue) {
                throw new ImportActionCanceledException(
                    tr("Import cancelled due to rejection of confirmation of {} key window", "survey"),
                    ImportStatus.CANCELED
                );
            }
        }
    }

    public void trySimplifyBuildingValue() {
        String oldValue = manager.getSelectedBuilding().get("building");
        String newValue = importedBuilding.get("building");
        if (isBuildingValueSimplification(oldValue, newValue)) {
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

    public boolean isBuildingDuplicate() {
        double overlapPercentage = calcMaxOverlapPercentageForCloseBuildings();
        return overlapPercentage > BuildingsSettings.OVERLAP_DETECT_DUPLICATED_BUILDING_THRESHOLD.get();
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

}

