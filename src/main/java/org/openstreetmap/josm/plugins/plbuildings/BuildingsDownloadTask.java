package org.openstreetmap.josm.plugins.plbuildings;

import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.openstreetmap.josm.plugins.plbuildings.io.BuildingsDownloader;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.tools.Logging;

/**
 * responsible for running SwingWorker for doing background (downloading) task to avoid freezing GUI.
 */
public class BuildingsDownloadTask extends SwingWorker<BuildingsImportData, Object> {
    private final BuildingsImportManager buildingsImportManager;

    public BuildingsDownloadTask(BuildingsImportManager buildingsImportManager) {
        this.buildingsImportManager = buildingsImportManager;
    }

    @Override
    protected BuildingsImportData doInBackground() {
        return BuildingsDownloader.downloadBuildings(buildingsImportManager);
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
