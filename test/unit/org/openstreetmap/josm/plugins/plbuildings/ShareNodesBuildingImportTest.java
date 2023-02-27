package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.*;

public class ShareNodesBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingShareTwoNodesWithOneBuilding(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 2);

        Way building = (Way) ds.getWays().toArray()[0];

        // Two shared nodes between 2 buildings. Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 2);
    }
    @Test
    public void testImportBuildingAllSameShareNodesActionShouldBeCanceled(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        assertNotNull(ds);
        assertEquals(ds.getWays().size(), 1);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertNotNull(ds);
        assertEquals(ds.getWays().size(), 1);
    }

    @Test
    public void testImportBuildingShareThreeNodesWithTwoAdjacentBuildings(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/two_adjacent_sides_merged_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 3);

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Three shared nodes between 3 buildings (2 nodes – 2 referred ways, 1 nodes – 3 referred ways).
        // Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 3);
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(3))).count(), 1);
    }
    @Test
    public void testImportBuildingShareFourNodesWithTwoOppositeBuildings(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/two_opposite_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 3);

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Four shared nodes between 3 buildings (4 nodes – 2 referred ways).
        // Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 4);
    }

    @Test
    public void testImportBuildingShareFourNodesWithThreeAdjacentNWEBuildings(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/three_adjacent_nwe_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setDataSourceProfile(testProfile);
        manager.processDownloadedData();

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 4);

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Four shared nodes between 4 buildings (2 nodes – 2 referred ways, 2 nodes – 3 referred ways)).
        // Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(3))).count(), 2);
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 4);
    }
}
