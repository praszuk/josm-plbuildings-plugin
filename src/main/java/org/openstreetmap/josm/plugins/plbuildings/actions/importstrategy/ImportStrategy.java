package org.openstreetmap.josm.plugins.plbuildings.actions.importstrategy;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;

public abstract class ImportStrategy {
    protected BuildingsImportManager manager;
    protected BuildingsImportStats importStats;
    protected DataSet currentDataSet;
    protected Way importedBuilding;

    public ImportStrategy(BuildingsImportManager manager, BuildingsImportStats importStats, Way importedBuilding) {
        this.manager = manager;
        this.importStats = importStats;
        this.currentDataSet = manager.getEditLayer();
        this.importedBuilding = importedBuilding;
    }

    public abstract Way performImport();
}

