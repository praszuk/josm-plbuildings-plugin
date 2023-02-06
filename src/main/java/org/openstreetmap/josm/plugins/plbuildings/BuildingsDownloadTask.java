package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.tools.Logging;

import javax.swing.*;

import java.util.concurrent.ExecutionException;

import static org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction.getBuildingsAt;
import static org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction.updateGuiStatus;
import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.DOWNLOADING;

/**
 * responsible for running SwingWorker for doing background (downloading) task to avoid freezing GUI.
 */
public class BuildingsDownloadTask extends SwingWorker<BuildingsImportData, Object> {
    private final BuildingsImportManager buildingsImportManager;

    public BuildingsDownloadTask(BuildingsImportManager buildingsImportManager){
        this.buildingsImportManager = buildingsImportManager;
    }

    @Override
    protected BuildingsImportData doInBackground() {
        updateGuiStatus(DOWNLOADING);
        return getBuildingsAt(this.buildingsImportManager.getCursorLatLon());
    }

    @Override
    protected void done() {
        try {
            this.buildingsImportManager.setImportedData(get());
            this.buildingsImportManager.processDownloadedData();
        } catch (InterruptedException | ExecutionException e) {
            Logging.error("PlBuildings runtime error at SwingWorker BuildingImportTask: {0}", e);
        }
    }
}
