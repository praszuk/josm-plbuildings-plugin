package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
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
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/simple_building.osm"), "");
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().size(), 1);
        Way building = (Way) ds.getWays().toArray()[0];
        assertEquals(building.getNodesCount() - 1, 4);
        assertTrue(building.hasTag("building"));

    }

    @Test
    public void testImportEmptyDataSet(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return new DataSet();
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds);
        assertTrue(ds.isEmpty());
    }

    @Test
    public void testImportDataSetWithMultipleBuildingsButImportOnlyOne(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){

                DataSet data = importOsmFile(new File("test/data/simple_multiple_buildings.osm"), "");
                assert data != null;
                assertTrue(data.getWays().size() > 1);
                return data;
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportAction.performBuildingImport(ds);
        assertEquals(ds.getWays().size(), 1);
    }
}
