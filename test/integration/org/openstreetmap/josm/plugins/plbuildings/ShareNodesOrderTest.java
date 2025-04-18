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

/**
 * This test class is to prevent messing with low-level building model
 * It has been created caused by bug
 * where nodes getting duplicated and there are more not unique nodes than first and last
 */
public class ShareNodesOrderTest {

    @Test
    public void testImportBuildingShareFourNodesWithTwoOppositeBuildings() {
        DataSet importDataSet = importOsmFile(new File("test/data/share_nodes_order/import_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/share_nodes_order/two_opposite_buildings_base.osm"), "");
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
}
