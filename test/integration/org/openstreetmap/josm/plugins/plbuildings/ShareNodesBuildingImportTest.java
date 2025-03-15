package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator;

public class ShareNodesBuildingImportTest {
    @Test
    public void testImportBuildingShareTwoNodesWithOneBuilding() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertNotNull(ds);
        Assertions.assertEquals(2, ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count());

        Way building = (Way) ds.getWays().toArray()[0];

        // Two shared nodes between 2 buildings. Skipping first node because closed ways always duplicates 1 node
        Assertions.assertEquals(
            2,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count()
        );
    }

    @Test
    public void testImportBuildingAllSameShareNodesActionShouldBeCanceled() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        Assertions.assertNotNull(ds);
        Assertions.assertEquals(1, ds.getWays().size());

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertNotNull(ds);
        Assertions.assertEquals(1, ds.getWays().size());
    }

    @Test
    public void testImportBuildingShareThreeNodesWithTwoAdjacentBuildings() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/two_adjacent_sides_merged_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertNotNull(ds);
        Assertions.assertEquals(3, ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count());

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Three shared nodes between 3 buildings (2 nodes – 2 referred ways, 1 nodes – 3 referred ways).
        // Skipping first node because closed ways always duplicates 1 node
        Assertions.assertEquals(
            3,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count()
        );
        Assertions.assertEquals(
            1,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(3))).count()
        );
    }

    @Test
    public void testImportBuildingShareFourNodesWithTwoOppositeBuildings() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/two_opposite_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertNotNull(ds);
        Assertions.assertEquals(3, ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count());

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Four shared nodes between 3 buildings (4 nodes – 2 referred ways).
        // Skipping first node because closed ways always duplicates 1 node
        Assertions.assertEquals(
            4,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count()
        );
    }

    @Test
    public void testImportBuildingShareFourNodesWithThreeAdjacentNorthWestEastBuildings() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes/three_adjacent_nwe_building_base.osm"), "");
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertNotNull(ds);
        Assertions.assertEquals(4, ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count());

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Four shared nodes between 4 buildings (2 nodes – 2 referred ways, 2 nodes – 3 referred ways)).
        // Skipping first node because closed ways always duplicates 1 node
        Assertions.assertEquals(
            2,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(3))).count()
        );
        Assertions.assertEquals(
            4,
            building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count()
        );
    }
}
