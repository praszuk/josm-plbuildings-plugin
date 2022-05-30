package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class ShareNodesWithObjectTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    static {
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                DataSet importData = importOsmFile(
                        new File("test/data/share_nodes_with_object/import_building.osm"),
                        ""
                );
                assertNotNull(importData);

                return importOsmFile(new File("test/data/share_nodes_with_object/import_building.osm"), "");
            }
        };
    }
    @Test
    public void testImportBuildingShareTwoNodesWithBuilding(){
        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_building.osm"), "");
        assertNotNull(ds);

        BuildingsAction.performBuildingImport(ds);

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
        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_waterway.osm"), "");
        assertNotNull(ds);

        BuildingsAction.performBuildingImport(ds);

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
        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/way_barrier.osm"), "");
        assertNotNull(ds);

        BuildingsAction.performBuildingImport(ds);

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
        DataSet ds = importOsmFile(new File("test/data/share_nodes_with_object/node_shop.osm"), "");
        assertNotNull(ds);

        BuildingsAction.performBuildingImport(ds);

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasKey("building")).toArray()[0];

        // -1 is for avoid duplicate of nodes from closed way and +1 is for looking for node object
        assertEquals(ds.getNodes().size(), (building.getNodesCount() - 1) + 1);
    }
}
