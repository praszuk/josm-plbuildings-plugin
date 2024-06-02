package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator.isBuildingWayValid;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.AbstractPrimitive;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class ReplaceOldBuildingImportTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testImportBuildingWithReplaceWithOneBuildingIsSelected() {
        DataSet importData = importOsmFile(new File("test/data/replace_building_1.osm"), "");
        assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];
        assertEquals(4, buildingToImport.getNodesCount() - 1);

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);


        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.getNodesCount() == 5).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        assertEquals(4, buildingToReplace.getNodesCount() - 1);
        assertTrue(isBuildingWayValid(buildingToReplace));
    }

    @Test
    public void testImportBuildingWithReplaceButMoreThanOneSoNullSoBuildingIsSelectedSoCancelImport() {
        DataSet importData = importOsmFile(new File("test/data/replace_building_1.osm"), "");
        assertNotNull(importData);

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);
        ds.setSelected(ds.getWays());
        assertTrue(ds.getAllSelected().size() > 1);

        Set<Integer> versions = ds.getWays().stream().map(AbstractPrimitive::getVersion).collect(Collectors.toSet());

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        assertTrue(ds.getWays().stream().allMatch(way -> versions.contains(way.getVersion())));
    }

    @Test
    public void testImportBuildingWithReplaceWithOneBuildingIsSelectedUndoRedo() {
        DataSet importData = importOsmFile(new File("test/data/replace_building_1.osm"), "");
        assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];
        assertEquals(4, buildingToImport.getNodesCount() - 1);

        DataSet ds = importOsmFile(new File("test/data/replace_multiple_buildings.osm"), "");
        assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.getNodesCount() == 5).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        assertTrue(isBuildingWayValid(buildingToReplace));
        assertEquals(4, buildingToReplace.getNodesCount() - 1);

        UndoRedoHandler.getInstance().undo(2);
        assertTrue(isBuildingWayValid(buildingToReplace));
        assertEquals(5, buildingToReplace.getNodesCount());

        UndoRedoHandler.getInstance().redo(2);
        assertTrue(isBuildingWayValid(buildingToReplace));
        assertEquals(4, buildingToReplace.getNodesCount() - 1);
    }
}
