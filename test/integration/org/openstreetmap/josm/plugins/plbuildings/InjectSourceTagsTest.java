package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

import java.io.File;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.plugins.plbuildings.gui.ImportedBuildingOverlappingOptionDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.annotations.Projection;

@Projection
public class InjectSourceTagsTest {
    @Test
    void testOneDsImportOnlySourceBuilding() {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds1", "ds1", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds1", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testTwoDsImportSourceBuildingAndSourceGeometry() {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds1", "ds2", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds1", importDataSet, "ds2", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getTags());
        Assertions.assertEquals(resultBuilding.get("source:geometry"), profile.getGeometry());
    }

    @Test
    void testTwoDsImportButTagsSourceMissing() {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds_geom", importDataSet, "ds_tags", new DataSet()));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getGeometry());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testTwoDsImportButGeometrySourceMissing() {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds_geom", new DataSet(), "ds_tags", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectMergeBoth(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD.put(120.);
        new Expectations() {{
            dialog.getUserConfirmedStrategy();
            result = CombineNearestOverlappingStrategy.MERGE_BOTH;
        }};

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds_geom", importDataSet, "ds_tags", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getTags());
        Assertions.assertEquals(resultBuilding.get("source:geometry"), profile.getGeometry());
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectAcceptTagsSource(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD.put(120.);
        new Expectations() {{
            dialog.getUserConfirmedStrategy();
            result = CombineNearestOverlappingStrategy.ACCEPT_TAGS_SOURCE;
        }};

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds_geom", importDataSet, "ds_tags", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectAcceptGeometrySource(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        DataSourceProfile profile = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile");

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_OVERLAP_THRESHOLD.put(120.);
        new Expectations() {{
            dialog.getUserConfirmedStrategy();
            result = CombineNearestOverlappingStrategy.ACCEPT_GEOMETRY_SOURCE;
        }};

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData("ds_geom", importDataSet, "ds_tags", importDataSet));
        manager.setCurrentProfile(profile);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profile.getGeometry());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }
}
