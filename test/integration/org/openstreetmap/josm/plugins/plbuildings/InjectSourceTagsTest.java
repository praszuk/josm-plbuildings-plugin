package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;

import java.io.File;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportMode;
import org.openstreetmap.josm.plugins.plbuildings.gui.ImportedBuildingOverlappingOptionDialog;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.annotations.Projection;

@Projection
public class InjectSourceTagsTest {
    private DataSourceProfile profileSameDs;
    private DataSourceProfile profileDifferentDs;

    @BeforeEach
    void setUp() {
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");
        profileSameDs = new DataSourceProfile(server.getName(), "ds", "ds", "profile1");
        profileDifferentDs = new DataSourceProfile(server.getName(), "ds_geom", "ds_tags", "profile2");

        BuildingsSettings.IMPORT_MODE.put(ImportMode.FULL);
    }

    Way createClosedWay(DataSet dataSet) {
        Node node1 = new Node(new LatLon(0.0, 0.0));
        Node node2 = new Node(new LatLon(0.1, 0.1));
        Node node3 = new Node(new LatLon(0.2, 0.2));
        dataSet.addPrimitive(node1);
        dataSet.addPrimitive(node2);
        dataSet.addPrimitive(node3);
        Way closedWay = new Way();
        dataSet.addPrimitive(closedWay);

        return closedWay;
    }

    @Test
    void testOneDsImportOnlySourceBuilding() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData(profileSameDs.getTags(), importDataSet));
        manager.setCurrentProfile(profileSameDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileSameDs.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testOneDsImportOnlySourceBuildingButSelectedBuildingAlreadyContainsBothSourceTagsSharedTagsSource() {
        ExpertToggleAction.getInstance().setExpert(true);
        BuildingsSettings.IMPORT_MODE.put(ImportMode.TAGS);

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();
        Way selectedBuilding = createClosedWay(currentDataSet);
        selectedBuilding.put("source:building", profileDifferentDs.getTags());
        selectedBuilding.put("source:geometry", profileDifferentDs.getGeometry());

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, selectedBuilding);
        manager.setImportedData(new BuildingsImportData(profileSameDs.getTags(), importDataSet));
        manager.setCurrentProfile(profileSameDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileSameDs.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testOneDsImportOnlySourceBuildingButSelectedBuildingAlreadyContainsBothSourceTagsSharedGeometrySource() {
        ExpertToggleAction.getInstance().setExpert(true);
        BuildingsSettings.IMPORT_MODE.put(ImportMode.TAGS);

        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();
        Way selectedBuilding = createClosedWay(currentDataSet);
        selectedBuilding.put("source:building", profileDifferentDs.getTags());
        selectedBuilding.put("source:geometry", profileDifferentDs.getGeometry());

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, selectedBuilding);
        manager.setImportedData(new BuildingsImportData(profileSameDs.getGeometry(), importDataSet));
        manager.setCurrentProfile(profileSameDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileSameDs.getGeometry());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testTwoDsImportSourceBuildingAndSourceGeometry() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), importDataSet, profileDifferentDs.getTags(), importDataSet
        ));
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getTags());
        Assertions.assertEquals(resultBuilding.get("source:geometry"), profileDifferentDs.getGeometry());
    }

    @Test
    void testTwoDsImportButTagsSourceMissing() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), importDataSet, profileDifferentDs.getTags(), new DataSet())
        );
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getGeometry());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @Test
    void testTwoDsImportButGeometrySourceMissing() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:building"));
        Assertions.assertFalse(importDataSet.getWays().stream().findFirst().orElseThrow().hasTag("source:geometry"));

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        DataSet currentDataSet = new DataSet();

        BuildingsImportManager manager = new BuildingsImportManager(currentDataSet, null, null);
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), new DataSet(), profileDifferentDs.getTags(), importDataSet)
        );
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectMergeBoth(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
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
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), importDataSet, profileDifferentDs.getTags(), importDataSet)
        );
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getTags());
        Assertions.assertEquals(resultBuilding.get("source:geometry"), profileDifferentDs.getGeometry());
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectAcceptTagsSource(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
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
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), importDataSet, profileDifferentDs.getTags(), importDataSet)
        );
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getTags());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Test
    void testTwoDsImportButNotOverlappingEnoughUserSelectAcceptGeometrySource(
        @Mocked ImportedBuildingOverlappingOptionDialog dialog
    ) {
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
        manager.setImportedData(new BuildingsImportData(
            profileDifferentDs.getGeometry(), importDataSet, profileDifferentDs.getTags(), importDataSet)
        );
        manager.setCurrentProfile(profileDifferentDs);
        manager.processDownloadedData();

        Way resultBuilding = currentDataSet.getWays().stream().findFirst().orElseThrow();
        Assertions.assertEquals(resultBuilding.get("source:building"), profileDifferentDs.getGeometry());
        Assertions.assertFalse(resultBuilding.hasTag("source:geometry"));
    }
}
