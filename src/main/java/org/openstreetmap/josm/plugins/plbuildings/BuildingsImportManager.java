package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction.performBuildingImport;
import static org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy.ACCEPT;
import static org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy.ASK_USER;
import static org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy.CANCEL;
import static org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus.DOWNLOADING;
import static org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus.IDLE;
import static org.openstreetmap.josm.plugins.plbuildings.gui.NotificationPopup.showNotification;
import static org.openstreetmap.josm.plugins.plbuildings.utils.NearestBuilding.getNearestBuilding;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.ImportedBuildingOneDsOptionDialog;
import org.openstreetmap.josm.plugins.plbuildings.gui.ImportedBuildingOverlappingOptionDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.NotifiableImportStatuses;
import org.openstreetmap.josm.plugins.plbuildings.utils.BuildingsOverlapDetector;
import org.openstreetmap.josm.plugins.plbuildings.utils.CloneBuilding;
import org.openstreetmap.josm.plugins.plbuildings.utils.NearestBuilding;


/**
 * Responsible for managing all import action workers, data sources, data, actions, GUI.
 */
public class BuildingsImportManager {
    private static CombineNearestOneDsStrategy oneDsConfirmationSessionStrategy = null;

    private final LatLon cursorLatLon;
    private final Way selectedBuilding;
    private final DataSet editLayer;
    private final DataSourceConfig dataSourceConfig;
    private DataSourceProfile currentProfile;
    private BuildingsImportData importedData;
    private ImportStatus status;
    private Way resultBuilding;

    private final NotifiableImportStatuses notifiableImportStatuses;

    public BuildingsImportManager(DataSet editLayer, LatLon cursorLatLon, Way selectedBuilding) {
        this.editLayer = editLayer;
        this.cursorLatLon = cursorLatLon;
        this.selectedBuilding = selectedBuilding;
        this.dataSourceConfig = new DataSourceConfig();
        this.currentProfile = dataSourceConfig.getCurrentProfile();
        this.notifiableImportStatuses = new NotifiableImportStatuses();

        this.importedData = null;
        this.resultBuilding = null;
        this.status = IDLE;
    }

    public BuildingsImportData getImportedData() {
        return importedData;
    }

    public LatLon getCursorLatLon() {
        return cursorLatLon;
    }

    public Way getSelectedBuilding() {
        return selectedBuilding;
    }

    public DataSet getEditLayer() {
        return editLayer;
    }

    public DataSourceProfile getCurrentProfile() {
        return this.currentProfile;
    }


    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setImportedData(BuildingsImportData importedData) {
        this.importedData = importedData;
    }

    public void setCurrentProfile(DataSourceProfile currentProfile) {
        this.currentProfile = currentProfile;
    }

    public void setResultBuilding(Way resultBuilding) {
        this.resultBuilding = resultBuilding;
    }

    public void setStatus(ImportStatus status, String reason) {
        this.status = status;
        updateGuiStatus();
        if (notifiableImportStatuses.isNotifiable(status)) {
            showNotification(status + ": " + reason);
        }
    }

    public void run() {
        BuildingsDownloadTask task = new BuildingsDownloadTask(this);
        setStatus(DOWNLOADING, null);
        task.execute();
    }

    public void processDownloadedData() {
        performBuildingImport(this);
    }

    private void updateGuiStatus() {
        // for tests and no-gui execution of method
        if (BuildingsPlugin.toggleDialogController == null) {
            return;
        }

        boolean autoChangeToDefault;
        switch (status) {
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
        BuildingsPlugin.toggleDialogController.setStatus(status, autoChangeToDefault);
    }

    /**
     * Helper function to updating GUI latest tags from action
     */
    public void updateGuiTags(boolean hasUncommonTags) {
        // for tests and no-gui execution of method
        if (BuildingsPlugin.toggleDialogController == null) {
            return;
        }

        String buildingText = "";
        String buildingLevelsText = "";
        if (resultBuilding != null) {
            buildingText = resultBuilding.getKeys().getOrDefault("building", "");
            buildingLevelsText = resultBuilding.getKeys().getOrDefault("building:levels", "");
        }
        BuildingsPlugin.toggleDialogController.updateTags(buildingText, buildingLevelsText, hasUncommonTags);
    }

    static CombineNearestOneDsStrategy getImportBuildingDataOneDsStrategy(String availableDataSource) {
        CombineNearestOneDsStrategy strategy = CombineNearestOneDsStrategy.fromString(
            BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.get()
        );
        if (oneDsConfirmationSessionStrategy != null) {
            return oneDsConfirmationSessionStrategy;
        }
        if (strategy == ASK_USER) {
            ImportedBuildingOneDsOptionDialog oneDsDialog = new ImportedBuildingOneDsOptionDialog(availableDataSource);
            strategy = oneDsDialog.isUserConfirmedOneDs() ? ACCEPT : CANCEL;
            if (oneDsDialog.isDoNotShowAgainThisSession()) {
                oneDsConfirmationSessionStrategy = strategy;
            }
        }
        return strategy;
    }

    static CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
        String geomDs,
        String tagsDs,
        double overlapPercentage
    ) {
        CombineNearestOverlappingStrategy strategy = CombineNearestOverlappingStrategy.fromString(
            BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAPPING_STRATEGY.get()
        );
        if (strategy == CombineNearestOverlappingStrategy.ASK_USER) {
            strategy = ImportedBuildingOverlappingOptionDialog.show(geomDs, tagsDs, overlapPercentage);
        }
        return strategy;
    }

    public static void injectSourceTags(OsmPrimitive importedBuilding, String geometrySource, String tagsSource) {
        if (!geometrySource.equals(tagsSource)) {
            importedBuilding.put("source:geometry", geometrySource);
        }
        importedBuilding.put("source:building", tagsSource);
    }

    /**
     * Create a new building based on provided parameters. It's cloned with new id/nodes.
     *
     * @param geometryBuilding – building from which only geometry will be reused
     * @param tagsBuilding     – building from which only tags will be reused
     */
    static Way combineBuildings(Way geometryBuilding, Way tagsBuilding) {
        Way newBuilding = new Way();
        newBuilding.setNodes(geometryBuilding.getNodes());
        newBuilding.setKeys(tagsBuilding.getKeys());

        return newBuilding;
    }

    /**
     * Get the nearest building object from 1-2 downloaded data sources as 1 building ready to import
     * It will use default strategies from settings for all problematic cases or ask user (GUI).
     * Cases:
     * .
     * 1. Same data source:
     * a) empty dataset -> return null
     * b) good dataset -> return nearest building
     * .
     * 2. Different data sources:
     * a) empty both data sets -> return null
     * b) empty one data set (default strategy or ask user)
     * -- user allows to use one data source -> return building based on 1 data source
     * -- user doesn't allow -> return null
     * c) both datasets with data:
     * -- buildings overlap with threshold e.g. 70% (setting) – merging both into 1 -> return combined building
     * -- buildings don’t overlap (default strategy or ask user):
     * ---- user allows to combine if not overlap - merging both into 1 -> return combined building
     * ---- user pick "tags" data source -> return based on tags data source
     * ---- user pick "geometry" data source -> return based on geometry data source
     * ---- user doesn't allow -> return null
     *
     * @param importedData – should be matched to profile parameter and contain 1 or 2 data sources with buildings data
     * @param profile      – should be matched with importData parameter
     * @param latLon       – cursor/start point location which is used to get the nearest building
     * @return building or null if it couldn't combine building or datasets empty/user decision/settings etc.
     * @throws NullPointerException if dataSourceProfile is not set
     */
    public static OsmPrimitive getNearestImportedBuilding(
        BuildingsImportData importedData,
        DataSourceProfile profile,
        LatLon latLon
    ) {
        OsmPrimitive importedBuilding;

        String importedBuildingGeometrySource = profile.getGeometry();
        String importedBuildingTagsSource = profile.getTags();

        // One data source
        if (profile.isOneDataSource()) {
            importedBuilding = getNearestBuilding(importedData.get(profile.getGeometry()), latLon);
            importedBuildingGeometrySource = profile.getGeometry();
            importedBuildingTagsSource = profile.getGeometry();
        }
        // Multiple data source
        else {
            DataSet geometryDs = importedData.get(profile.getGeometry());
            DataSet tagsDs = importedData.get(profile.getTags());

            // Both empty
            if (geometryDs.isEmpty() && tagsDs.isEmpty()) {
                importedBuilding = null;
            }
            // One empty
            else if (geometryDs.isEmpty() != tagsDs.isEmpty()) {
                String availableDsName = geometryDs.isEmpty() ? profile.getTags() : profile.getGeometry();

                if (getImportBuildingDataOneDsStrategy(availableDsName) == ACCEPT) {
                    importedBuilding = NearestBuilding.getNearestBuilding(importedData.get(availableDsName), latLon);
                    importedBuildingTagsSource = availableDsName;
                    importedBuildingGeometrySource = availableDsName;
                } else {
                    importedBuilding = null;
                }
            }
            // Both available
            else {
                Way geometryBuilding = geometryDs.getWays().iterator().next();
                Way tagsBuilding = tagsDs.getWays().iterator().next();
                double overlapPercentage = BuildingsOverlapDetector.detect(geometryBuilding, tagsBuilding);

                if (overlapPercentage
                    >= BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD.get()) {
                    importedBuilding = combineBuildings(geometryBuilding, tagsBuilding);
                } else {
                    CombineNearestOverlappingStrategy strategy = getImportBuildingOverlappingStrategy(
                        profile.getGeometry(),
                        profile.getTags(),
                        overlapPercentage
                    );
                    switch (strategy) {
                        case MERGE_BOTH:
                            importedBuilding = combineBuildings(geometryBuilding, tagsBuilding);
                            break;
                        case ACCEPT_GEOMETRY_SOURCE:
                            importedBuilding = geometryBuilding;
                            importedBuildingGeometrySource = profile.getGeometry();
                            importedBuildingTagsSource = profile.getGeometry();
                            break;
                        case ACCEPT_TAGS_SOURCE:
                            importedBuilding = tagsBuilding;
                            importedBuildingGeometrySource = profile.getTags();
                            importedBuildingTagsSource = profile.getTags();
                            break;
                        default:
                            importedBuilding = null;
                    }
                }
            }
        }
        if (importedBuilding != null) {
            injectSourceTags(importedBuilding, importedBuildingGeometrySource, importedBuildingTagsSource);
        }
        return CloneBuilding.cloneBuilding(importedBuilding);
    }
}