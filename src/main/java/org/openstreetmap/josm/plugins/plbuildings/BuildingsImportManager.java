package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;

import static org.openstreetmap.josm.gui.MainApplication.getLayerManager;
import static org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction.performBuildingImport;


/**
 * responsible for managing all import action workers, data sources, data, actions, GUI
 */
public class BuildingsImportManager {
    private final LatLon cursorLatLon;
    private final Way selectedBuilding;

    private BuildingsImportData importedData;

    public BuildingsImportManager(LatLon cursorLatLon, Way selectedBuilding) {
        this.cursorLatLon = cursorLatLon;
        this.selectedBuilding = selectedBuilding;
        this.importedData = null;
    }


    public void setImportedData(BuildingsImportData importedData) {
        this.importedData = importedData;
    }

    public LatLon getCursorLatLon() {
        return cursorLatLon;
    }

    public void run(){
        BuildingsDownloadTask task = new BuildingsDownloadTask(this);
        task.execute();
    }
    public void processDownloadedData() {
        performBuildingImport(
                getLayerManager().getEditDataSet(),
                importedData.get("bdot"), // TODO temporary
                selectedBuilding
        );
    }
}