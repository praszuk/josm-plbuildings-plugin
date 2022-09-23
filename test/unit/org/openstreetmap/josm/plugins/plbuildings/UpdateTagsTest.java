package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.UserCancelException;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;


public class UpdateTagsTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();


    @Test
    public void testNoTagsChangeCancel(){
        DataSet importDataSet = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasTag("building", "house"))
            .toArray()[0];
        int version = buildingToReplace.getVersion();
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds, importDataSet, buildingToReplace);

        assertEquals(buildingToReplace.getVersion(), version);
    }

    @Test
    public void testNewTagsNoConflict(){
        new MockUp<UpdateBuildingTagsCommand>(){
            @Mock
            private List<Command> prepareUpdateTagsCommands(Way selectedBuilding, Way newBuilding){
                return Collections.singletonList(
                    new ChangePropertyCommand(
                        selectedBuilding.getDataSet(),
                        Collections.singletonList(selectedBuilding),
                        newBuilding.getKeys()
                    ));
            }
        };
        DataSet importDataSet = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(importDataSet);

        DataSet ds = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasTag("building", "house"))
            .toArray()[0];
        buildingToReplace.put("roof:shape", "flat");
        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds, importDataSet, buildingToReplace);

        assertEquals(ds.getWays().size(), 1);
        assertEquals(ds.getWays().stream()
            .filter(way -> way.hasTag("roof:shape", "flat"))
            .count(), 1);
    }

    @Test
    public void testNewTagsConflict(){
        new MockUp<UpdateBuildingTagsCommand>(){
            @Mock
            private List<Command> prepareUpdateTagsCommands(Way selectedBuilding, Way newBuilding){
                return Collections.singletonList(
                    new ChangePropertyCommand(
                        selectedBuilding.getDataSet(),
                        Collections.singletonList(selectedBuilding),
                        newBuilding.getKeys()
                    ));
            }
        };
        DataSet importDataSet = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(importDataSet);
        Way building = (Way) importDataSet.getWays().toArray()[0];
        building.put("building", "detached");

        DataSet ds = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasTag("building", "house"))
            .toArray()[0];

        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds, importDataSet, buildingToReplace);

        assertEquals(ds.getWays().size(), 1);
        assertEquals(ds.getWays().stream().filter(way -> way.hasTag("building", "detached")).count(), 1);
    }

    @Test
    public void testNewTagsCanceledByUser(){
        new MockUp<UpdateBuildingTagsCommand>(){
            @Mock
            private List<Command> prepareUpdateTagsCommands(
                Way selectedBuilding,
                Way newBuilding
            ) throws UserCancelException{
                throw new UserCancelException("Canceled by user");
            }
        };
        DataSet importDataSet = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(importDataSet);
        Way building = (Way) importDataSet.getWays().toArray()[0];
        building.put("building", "detached");

        DataSet ds = importOsmFile(new File("test/data/update_tags/import_building.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasTag("building", "house"))
            .toArray()[0];
        int version = buildingToReplace.getVersion();

        ds.setSelected(buildingToReplace);

        BuildingsImportAction.performBuildingImport(ds, importDataSet, buildingToReplace);

        assertEquals(ds.getWays().size(), 1);
        assertEquals(ds.getWays().stream().filter(way -> way.hasTag("building", "detached")).count(), 0);
        assertEquals(buildingToReplace.getVersion(), version);
    }
}
