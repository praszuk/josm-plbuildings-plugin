package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.Assert.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

public class DuplicateImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testSimpleDuplicateCheckNotDuplicate(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
            }
        };

        DataSet ds = importOsmFile(
            new File("test/data/duplicate_import/simple_replace_base.osm"),
            "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasKey("building", "yes")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count(), 1);
    }

    @Test
    public void testSimpleDuplicateCheckDuplicateAllNodesEqualAndSameTags(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
            }
        };

        DataSet ds = importOsmFile(
            new File("test/data/duplicate_import/simple_duplicate_base.osm"),
            "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasKey("building", "house")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().size(), 1);
        assertEquals(ds.getWays().stream().filter(way -> way.hasKey("building", "house")).count(), 1);
    }
/*
 For the now replacing duplicated geometry with different tags is not supported

    @Test
    public void testSimpleDuplicateCheckNotDuplicateAllNodesEqualAndDifferentTags(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
            }
        };

        DataSet ds = importOsmFile(
            new File("test/data/duplicate_import/simple_duplicate_different_tags_base.osm"),
            "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasKey("building","house"))
            .toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().size(), 1);
        assertEquals(ds.getWays().stream().filter(way -> way.hasKey("building", "detached")).count(), 1);
    }
*/

    @Test
    public void testDuplicateBaseMoreNodesImportBuildingAllNodesEqual(){
        new MockUp<BuildingsImportAction>(){
            @Mock
            public DataSet getBuildingsAtCurrentLocation(){
                return importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
            }
        };

        DataSet ds = importOsmFile(
            new File("test/data/duplicate_import/duplicate_more_nodes_base.osm"),
            "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasKey("building", "yes")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().stream().filter(way -> way.hasKey("building", "house")).count(), 1);
    }
}