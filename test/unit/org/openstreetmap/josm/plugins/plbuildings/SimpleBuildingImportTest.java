package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.*;

public class SimpleBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingNoCloseNodesJustOneBuildingInDataset(){
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertEquals(ds.getWays().size(), 1);
        Way building = (Way) ds.getWays().toArray()[0];
        assertEquals(building.getNodesCount() - 1, 4);
        assertTrue(building.hasTag("building"));

    }

    @Test
    public void testImportEmptyDataSet(){
        DataSet importDataSet = new DataSet();

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();
        assertTrue(ds.isEmpty());
    }

    @Test
    public void testImportDataSetWithMultipleBuildingsButImportOnlyOne(){
        DataSet importDataSet = importOsmFile(new File("test/data/simple_multiple_buildings.osm"), "");
        assertNotNull(importDataSet);
        assertTrue(importDataSet.getWays().size() > 1);

        DataSet ds = new DataSet();

        Way expectedBuilding = importDataSet.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(ds, latLon, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();
        assertEquals(ds.getWays().size(), 1);
    }
}
