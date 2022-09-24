package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

/**
 * This test class is to prevent messing with low-level building model
 * It has been created caused by bug
 * where nodes getting duplicated and there are more not unique nodes than first and last
 */
public class ShareNodesOrderTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingShareFourNodesWithTwoOppositeBuildings(){
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_order/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_order/two_opposite_buildings_base.osm"), "");
        BuildingsImportAction.performBuildingImport(ds, importDataSet, null);

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 3);

        Way building = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];

        // Four shared nodes between 3 buildings (4 nodes – 2 referred ways).
        // Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 4);
    }
}
