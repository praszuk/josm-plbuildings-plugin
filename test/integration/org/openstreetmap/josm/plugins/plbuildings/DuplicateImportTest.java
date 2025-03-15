package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;

import java.io.File;
import java.util.Collections;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.plbuildings.commands.UpdateBuildingTagsCommand;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator;

public class DuplicateImportTest {
    static {
        new MockUp<UpdateBuildingTagsCommand>() {
            @Mock
            private List<Command> prepareUpdateTagsCommands(Way selectedBuilding, Way newBuilding) {
                return Collections.singletonList(
                    new ChangePropertyCommand(
                        selectedBuilding.getDataSet(),
                        Collections.singletonList(selectedBuilding),
                        newBuilding.getKeys()
                    ));
            }
        };

    }

    @BeforeEach
    public void setUp() {
        ProjectionRegistry.setProjection(Projections.getProjectionByCode("EPSG:4326"));
    }

    @Test
    public void testSimpleDuplicateCheckNotDuplicate() {
        DataSet importDataSet = importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
        DataSet ds = importOsmFile(new File("test/data/duplicate_import/simple_replace_base.osm"), "");
        Assertions.assertNotNull(importDataSet);
        Assertions.assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "yes")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().stream().filter(BuildingsWayValidator::isBuildingWayValid).count());
    }

    @Test
    public void testSimpleDuplicateCheckDuplicateAllNodesEqualAndSameTags() {
        DataSet importDataSet = importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
        DataSet ds = importOsmFile(new File("test/data/duplicate_import/simple_duplicate_base.osm"), "");
        Assertions.assertNotNull(importDataSet);
        Assertions.assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream().filter(way -> way.hasTag("building", "house")).toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
        Assertions.assertEquals(1, ds.getWays().stream().filter(way -> way.hasTag("building", "house")).count());
    }

    @Test
    public void testSimpleDuplicateCheckNotDuplicateAllNodesEqualAndDifferentTags() {
        DataSet importDataSet = importOsmFile(new File("test/data/duplicate_import/import_building.osm"), "");
        DataSet ds = importOsmFile(
            new File("test/data/duplicate_import/simple_duplicate_different_tags_base.osm"),
            "");
        Assertions.assertNotNull(importDataSet);
        Assertions.assertNotNull(ds);

        Way buildingToReplace = (Way) ds.getWays().stream()
            .filter(way -> way.hasTag("building", "yes"))
            .toArray()[0];
        ds.setSelected(buildingToReplace);

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
        Assertions.assertEquals(1, ds.getWays().stream().filter(way -> way.hasTag("building", "house")).count());
    }
}
