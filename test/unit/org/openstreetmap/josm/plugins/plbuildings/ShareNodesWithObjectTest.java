package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class ShareNodesWithObjectTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingShareTwoNodesWithBuilding(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_building.osm"), "");
        assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().toArray()[0]; // doesn't matter which one
        assertEquals(
            building.getNodes().stream()
                .skip(1)
                .filter(node -> node.isReferredByWays(2))
                .count(),
        2);
    }

    @Test
    public void testImportBuildingNotShareNodesWithNotBuildingWay(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_waterway.osm"), "");
        assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];
        assertEquals(
            building.getNodes().stream()
                .skip(1)
                .filter(node -> node.isReferredByWays(2))
                .count(),
            0);
    }

    @Test
    public void testImportBuildingNotShareNodesWithBarrier(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_barrier.osm"), "");
        assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];
        assertEquals(
                building.getNodes().stream()
                        .skip(1)
                        .filter(node -> node.isReferredByWays(2))
                        .count(),
                0);
    }

    @Test
    public void testImportBuildingNotShareNodeWithNodeObject(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/node_shop.osm"), "");
        assertNotNull(ds);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.processDownloadedData();

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];

        // -1 is for avoid duplicate of nodes from closed way and +1 is for looking for node object
        assertEquals(ds.getNodes().size(), (building.getNodesCount() - 1) + 1);
    }
}
