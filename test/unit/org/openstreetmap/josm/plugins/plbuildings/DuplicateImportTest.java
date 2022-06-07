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
    public void testSimpleDuplicateCheckDuplicateAllNodesEqual(){
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

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasKey("building", "yes")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds);

        assertEquals(ds.getWays().stream().filter(way -> way.hasKey("building", "yes")).count(), 1);
    }

}
