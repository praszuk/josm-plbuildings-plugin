package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class SimpleBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingNoCloseNodesJustOneBuildingInDataset(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/simple_building.osm"), "");
            }
        };

        DataSet ds = new DataSet();
        BuildingsAction.performBuildingImport(ds);

        assertEquals(ds.getWays().size(), 1);
        Way building = (Way) ds.getWays().toArray()[0];
        assertEquals(building.getNodesCount() - 1, 4);
        assertTrue(building.hasTag("building"));

    }

    @Test
    public void testImportEmptyDataSet(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return new DataSet();
            }
        };

        DataSet ds = new DataSet();
        BuildingsAction.performBuildingImport(ds);
        assertTrue(ds.isEmpty());
    }
}
