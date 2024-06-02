package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.utils.CloneBuilding.cloneBuilding;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class SimpleBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingNoCloseNodesJustOneBuildingInDataset() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        assertEquals(1, ds.getWays().size());
        Way building = (Way) ds.getWays().toArray()[0];
        assertEquals(4, building.getNodesCount() - 1);
        assertTrue(building.hasTag("building"));

    }

    @Test
    public void testImportEmptyDataSet() {
        DataSet importDataSet = new DataSet();

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();
        assertTrue(ds.isEmpty());
    }

    @Test
    public void testImportDataSetWithMultipleBuildingsButImportOnlyOne() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_multiple_buildings.osm"), "");
        assertNotNull(importDataSet);
        assertTrue(importDataSet.getWays().size() > 1);

        DataSet ds = new DataSet();

        Way expectedBuilding = importDataSet.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(ds, latLon, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();
        assertEquals(1, ds.getWays().size());
    }

    @Test
    public void testImportBuildingWithoutOsmMetaData() {
        DataSet rawDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        assertNotNull(rawDataSet);

        DataSet importDataSet = new DataSet();
        rawDataSet.getWays().forEach(w -> importDataSet.addPrimitiveRecursive(cloneBuilding(w)));

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        assertEquals(1, ds.getWays().size());
    }
}
