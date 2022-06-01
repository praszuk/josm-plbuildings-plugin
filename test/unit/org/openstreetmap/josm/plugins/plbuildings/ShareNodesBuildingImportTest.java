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

public class ShareNodesBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingShareNodesSimpleJoinToBuildingLikeSemiDetached(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/share_nodes/import_building.osm"), "");
            }
        };

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        BuildingsAction.performBuildingImport(ds);

        assertNotNull(ds);
        assertEquals(ds.getWays().stream().filter(Way::isClosed).count(), 2);

        Way building = (Way) ds.getWays().toArray()[0];

        // Two shared nodes between 2 buildings. Skipping first node because closed ways always duplicates 1 node
        assertEquals(building.getNodes().stream().skip(1).filter((node -> node.isReferredByWays(2))).count(), 2);
    }
    @Test
    public void testImportBuildingAllSameShareNodesActionShouldBeCanceled(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
            }
        };

        DataSet ds = importOsmFile(new File("test/data/share_nodes/building_base.osm"), "");
        assertNotNull(ds);
        assertEquals(ds.getWays().size(), 1);

        BuildingsAction.performBuildingImport(ds);

        assertNotNull(ds);
        assertEquals(ds.getWays().size(), 1);
    }
}
