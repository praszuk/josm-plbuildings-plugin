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

public class ShareNodesWithObjectTest {
    @Test
    public void testImportBuildingShareTwoNodesWithBuilding() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_building.osm"), "");
        Assertions.assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().toArray()[0]; // doesn't matter which one
        Assertions.assertEquals(
            2,
            building.getNodes().stream()
                .skip(1)
                .filter(node -> node.isReferredByWays(2))
                .count()
        );
    }

    @Test
    public void testImportBuildingNotShareNodesWithNotBuildingWay() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_waterway.osm"), "");
        Assertions.assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];
        Assertions.assertEquals(
            0,
            building.getNodes().stream()
                .skip(1)
                .filter(node -> node.isReferredByWays(2))
                .count()
        );
    }

    @Test
    public void testImportBuildingNotShareNodesWithBarrier() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_barrier.osm"), "");
        Assertions.assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];
        Assertions.assertEquals(
            0,
            building.getNodes().stream()
                .skip(1)
                .filter(node -> node.isReferredByWays(2))
                .count()
        );
    }

    @Test
    public void testImportBuildingNotShareNodeWithNodeObject() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/node_shop.osm"), "");
        Assertions.assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];

        // -1 is for avoid duplicate of nodes from closed way and +1 is for looking for node object
        Assertions.assertEquals((building.getNodesCount() - 1) + 1, ds.getNodes().size());
    }
}
