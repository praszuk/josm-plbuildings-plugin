package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.gui.UncommonTagDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.tools.Logging;

import static org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction.performBuildingImport;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.DOWNLOADING;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.IDLE;
import static org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils.findUncommonTags;


/**
 * responsible for managing all import action workers, data sources, data, actions, GUI
 */
public class BuildingsImportManager {
    private final LatLon cursorLatLon;
    private final Way selectedBuilding;
    private final DataSourceProfile dataSourceProfile;

    private final DataSet editLayer;
    private BuildingsImportData importedData;
    private ImportStatus status;
    private Way resultBuilding;

    public BuildingsImportManager(DataSet editLayer, LatLon cursorLatLon, Way selectedBuilding) {
        this.editLayer = editLayer;
        this.cursorLatLon = cursorLatLon;
        this.selectedBuilding = selectedBuilding;
        this.dataSourceProfile = DataSourceConfig.getInstance().getCurrentProfile();

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

    public DataSourceProfile getDataSourceProfile(){
        return this.dataSourceProfile;
    }

    public void setImportedData(BuildingsImportData importedData) {
        this.importedData = importedData;
    }

    public void setResultBuilding(Way resultBuilding) {
        this.resultBuilding = resultBuilding;
    }
    public void setStatus(ImportStatus status) {
        this.status = status;
        updateGuiStatus();
    }

    public void run(){
        BuildingsDownloadTask task = new BuildingsDownloadTask(this);
        setStatus(DOWNLOADING);
        task.execute();
    }
    public void processDownloadedData() {
        performBuildingImport(this);
        if (resultBuilding != null){
            postCheck();
        }
    }
    private void postCheck(){
        boolean hasUncommonTags = false;
        TagMap uncommon = findUncommonTags(resultBuilding);
        if (!uncommon.isEmpty()){
            Logging.debug("Found uncommon tags {0}", uncommon);
            setStatus(ImportStatus.ACTION_REQUIRED);
            hasUncommonTags = true;
            UncommonTagDialog.show(
                    uncommon.getTags()
                            .toString()
                            .replace("[", "")
                            .replace("]", "")
            );
        }

        setStatus(ImportStatus.DONE);
        updateGuiTags(hasUncommonTags);
    }

    private void updateGuiStatus(){
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
    public void updateGuiTags(boolean hasUncommonTags){
        if (BuildingsPlugin.buildingsToggleDialog == null)  // for tests and no-gui execution of method
            return;

        BuildingsPlugin.buildingsToggleDialog.updateTags(
            resultBuilding.getKeys().getOrDefault("building", ""),
            resultBuilding.getKeys().getOrDefault("building:levels", ""),
            hasUncommonTags
        );
    }
}