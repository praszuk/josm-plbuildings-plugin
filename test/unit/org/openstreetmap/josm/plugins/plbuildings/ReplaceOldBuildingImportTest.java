package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.AbstractPrimitive;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class ReplaceOldBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingWithReplaceWithOneBuildingIsSelected(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                DataSet importData = importOsmFile(new File("test/data/replace_building_1.osm"), "");
                assertNotNull(importData);
                Way buildingToImport = (Way) importData.getWays().toArray()[0];
                assertEquals(buildingToImport.getNodesCount() - 1, 4);

                return importOsmFile(new File("test/data/replace_building_1.osm"), "");
            }
        };

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.getNodesCount() == 5).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsAction.performBuildingImport(ds);


        assertEquals(buildingToReplace.getNodesCount() - 1, 4);
    }

    @Test
    public void testImportBuildingWithReplaceButMoreThanOneBuildingIsSelectedSoCancelImport(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/replace_building_1.osm"), "");
            }
        };

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);
        ds.setSelected(ds.getWays());
        assertTrue(ds.getAllSelected().size() > 1);

        Set<Integer> versions = ds.getWays().stream().map(AbstractPrimitive::getVersion).collect(Collectors.toSet());

        BuildingsAction.performBuildingImport(ds);

        assertTrue(ds.getWays().stream().allMatch(way -> versions.contains(way.getVersion())));
    }

    @Test
    public void testImportBuildingWithReplaceWithOneBuildingIsSelectedUndoRedo(){
        new MockUp<BuildingsAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                DataSet importData = importOsmFile(new File("test/data/replace_building_1.osm"), "");
                assertNotNull(importData);
                Way buildingToImport = (Way) importData.getWays().toArray()[0];
                assertEquals(buildingToImport.getNodesCount() - 1, 4);

                return importOsmFile(new File("test/data/replace_building_1.osm"), "");
            }
        };

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.getNodesCount() == 5).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsAction.performBuildingImport(ds);

        assertEquals(buildingToReplace.getNodesCount() - 1, 4);
        UndoRedoHandler.getInstance().undo(2);
        assertEquals(buildingToReplace.getNodesCount(), 5);
        UndoRedoHandler.getInstance().redo(2);
        assertEquals(buildingToReplace.getNodesCount() - 1, 4);
    }
}
