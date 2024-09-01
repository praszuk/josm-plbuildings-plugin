package org.openstreetmap.josm.plugins.plbuildings.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator.isBuildingWayValid;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.testutils.JOSMTestRules;


class FullImportTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules().main().projection();

    @BeforeEach
    public void setUp() {
        ExpertToggleAction.getInstance().setExpert(true);
        BuildingsSettings.IMPORT_STATS.put(BuildingsSettings.IMPORT_STATS.getDefaultValue());
    }

    @AfterEach
    public void teardown() {
        BuildingsSettings.IMPORT_STATS.put(BuildingsSettings.IMPORT_STATS.getDefaultValue());
    }

    @Test
    void testImportWithFullReplace() {
        DataSet importData = importOsmFile(new File("test/data/import_strategy/imported_building.osm"), "");
        Assertions.assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];

        DataSet ds = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "test", null));

        Assertions.assertNotNull(ds);

        BuildingsImportStats stats = BuildingsImportStats.getInstance();
        int replaceCounter = stats.getImportWithReplaceCounter();
        int tagsUpdateCounter = stats.getImportWithTagsUpdateCounter();

        Way buildingToReplace = ds.getWays().stream().findFirst().orElseThrow();
        ds.setSelected(buildingToReplace);

        Assertions.assertNotEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
        Assertions.assertNotEquals(buildingToReplace.get("building"), buildingToImport.get("building"));

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertTrue(isBuildingWayValid(buildingToReplace));

        Assertions.assertEquals(buildingToImport.getNodesCount(), buildingToReplace.getNodesCount());
        Assertions.assertEquals(buildingToImport.get("building"), buildingToReplace.get("building"));

        Assertions.assertEquals(replaceCounter + 1, stats.getImportWithReplaceCounter());
        Assertions.assertEquals(tagsUpdateCounter + 1, stats.getImportWithTagsUpdateCounter());
    }
}
