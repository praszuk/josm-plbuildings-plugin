package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class SimpleBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingNoCloseNodesJustOneBuildingInDataset(){
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds, importDataSet, null);

        assertEquals(ds.getWays().size(), 1);
        Way building = (Way) ds.getWays().toArray()[0];
        assertEquals(building.getNodesCount() - 1, 4);
        assertTrue(building.hasTag("building"));

    }

    @Test
    public void testImportEmptyDataSet(){
        DataSet importDataSet = new DataSet();

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds, importDataSet, null);
        assertTrue(ds.isEmpty());
    }

    @Test
    public void testImportDataSetWithMultipleBuildingsButImportOnlyOne(){
        DataSet importDataSet = importOsmFile(new File("test/data/simple_multiple_buildings.osm"), "");
        assertNotNull(importDataSet);
        assertTrue(importDataSet.getWays().size() > 1);

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds, importDataSet, null);
        assertEquals(ds.getWays().size(), 1);
    }
}
