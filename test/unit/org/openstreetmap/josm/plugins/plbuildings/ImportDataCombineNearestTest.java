package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.isSameButClonedBuilding;

import java.io.File;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOverlappingStrategy;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class ImportDataCombineNearestTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    public DataSourceProfile profileOneDs;
    public DataSourceProfile profileTwoDs;
    public DataSet emptyDs;
    public DataSet oneBuildingDs;
    public DataSet multipleBuildingDs;

    public DataSet bothOverlapOver60oneBuildingGeometryDs;
    public DataSet bothOverlapOver60oneBuildingTagsDs;

    public DataSet bothOverlapLt60oneBuildingGeometryDs;
    public DataSet bothOverlapLt60oneBuildingTagsDs;

    @Before
    public void setUp() {
        ProjectionRegistry.setProjection(Projections.getProjectionByCode("EPSG:4326"));

        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");

        this.profileOneDs = new DataSourceProfile(server.getName(), "ds1", "ds1", "profile1");
        this.profileTwoDs = new DataSourceProfile(server.getName(), "ds1", "ds2", "profile2");

        this.emptyDs = new DataSet();
        this.oneBuildingDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/one_building.osm"),
            ""
        );
        this.multipleBuildingDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/multiple_buildings.osm"),
            ""
        );

        this.bothOverlapOver60oneBuildingGeometryDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapOver60oneBuildingTagsDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_tags.osm"),
            ""
        );

        this.bothOverlapLt60oneBuildingGeometryDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapLt60oneBuildingTagsDs = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_tags.osm"),
            ""
        );
        new MockUp<BuildingsImportManager>() {
            @Mock
            public void injectSourceTags(OsmPrimitive importedBuilding, String geometrySource, String tagsSource) {}
        };
    }

    @Test
    public void testOneDsEmptyDs() {
        OsmPrimitive nearestBuilding = BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDs.getGeometry(), emptyDs),
            profileOneDs,
            null
        );
        assertNull(nearestBuilding);
    }

    @Test
    public void testOneDsOneBuilding() {
        Way expectedBuilding = oneBuildingDs.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDs.getGeometry(), oneBuildingDs),
            profileOneDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testOneDsMultipleBuilding() {
        Way[] buildings = multipleBuildingDs.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDs.getGeometry(), multipleBuildingDs),
            profileOneDs,
            latLon
        );

        assertTrue(multipleBuildingDs.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsEmptyBothDs() {
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileTwoDs.getGeometry(), emptyDs),
            profileTwoDs,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDsEmptyOneDsStrategyAcceptOneDs() {
        Way expectedBuilding = oneBuildingDs.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), emptyDs,
                profileTwoDs.getTags(), oneBuildingDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsEmptyOneDsStrategyAcceptOneDsMultipleBuilding() {
        Way[] buildings = multipleBuildingDs.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), emptyDs,
                profileTwoDs.getTags(), multipleBuildingDs
            ),
            profileTwoDs,
            latLon
        );

        assertTrue(multipleBuildingDs.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsEmptyOneDsStrategyCancelOneDs() {
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.CANCEL.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), emptyDs,
                profileTwoDs.getTags(), oneBuildingDs
            ),
            profileTwoDs,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDsEmptyOneDsStrategyAskUserAcceptOneDs() {
        Way expectedBuilding = oneBuildingDs.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOneDsStrategy getImportBuildingDataOneDsStrategy(String availableDataSource) {
                return CombineNearestOneDsStrategy.ACCEPT;
            }
        };
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), emptyDs,
                profileTwoDs.getTags(), oneBuildingDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsEmptyOneDsStrategyAskUserCancelOneDs() {
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOneDsStrategy getImportBuildingDataOneDsStrategy(String availableDataSource) {
                return CombineNearestOneDsStrategy.CANCEL;
            }
        };
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), emptyDs,
                profileTwoDs.getTags(), oneBuildingDs
            ),
            profileTwoDs,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDsBothDsOverlapOver60Merge() {
        Way expectedGeometryBuilding = bothOverlapOver60oneBuildingGeometryDs.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapOver60oneBuildingTagsDs.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        Way expectedBuilding = new Way();
        expectedGeometryBuilding.getNodes().forEach(expectedBuilding::addNode);
        expectedTagsBuilding.getKeys().forEach(expectedBuilding::put);

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), bothOverlapOver60oneBuildingGeometryDs,
                profileTwoDs.getTags(), bothOverlapOver60oneBuildingTagsDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsBothDsOverlapLt60AskUserMergeBoth() {
        Way expectedGeometryBuilding = bothOverlapLt60oneBuildingGeometryDs.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDs.getWays().iterator().next();

        Way expectedBuilding = new Way();
        expectedGeometryBuilding.getNodes().forEach(expectedBuilding::addNode);
        expectedTagsBuilding.getKeys().forEach(expectedBuilding::put);

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
                String geomDs, String tagsDs, double overlapPercentage) {
                return CombineNearestOverlappingStrategy.MERGE_BOTH;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), bothOverlapLt60oneBuildingGeometryDs,
                profileTwoDs.getTags(), bothOverlapLt60oneBuildingTagsDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDsBothDsOverlapLt60AskUserAcceptGeometrySource() {
        Way expectedGeometryBuilding = bothOverlapLt60oneBuildingGeometryDs.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDs.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
                String geomDs, String tagsDs, double overlapPercentage
            ) {
                return CombineNearestOverlappingStrategy.ACCEPT_GEOMETRY_SOURCE;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), bothOverlapLt60oneBuildingGeometryDs,
                profileTwoDs.getTags(), bothOverlapLt60oneBuildingTagsDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(expectedTagsBuilding.getKeys().containsKey("building"));
        assertFalse(nearestBuilding.getKeys().containsKey("building"));
        assertTrue(isSameButClonedBuilding(nearestBuilding, expectedGeometryBuilding));
    }

    @Test
    public void testTwoDsBothDsOverlapLt60AskUserAcceptTagsSource() {
        Way expectedGeometryBuilding = bothOverlapLt60oneBuildingGeometryDs.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDs.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
                String geomDs, String tagsDs, double overlapPercentage
            ) {
                return CombineNearestOverlappingStrategy.ACCEPT_TAGS_SOURCE;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), bothOverlapLt60oneBuildingGeometryDs,
                profileTwoDs.getTags(), bothOverlapLt60oneBuildingTagsDs
            ),
            profileTwoDs,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(nearestBuilding, expectedTagsBuilding));
    }

    @Test
    public void testTwoDsBothDsOverlapLt60AskUserCancel() {
        new MockUp<BuildingsImportManager>() {
            @Mock
            CombineNearestOverlappingStrategy getImportBuildingOverlappingStrategy(
                String geomDs, String tagsDs, double overlapPercentage
            ) {
                return CombineNearestOverlappingStrategy.CANCEL;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDs.getGeometry(), bothOverlapLt60oneBuildingGeometryDs,
                profileTwoDs.getTags(), bothOverlapLt60oneBuildingTagsDs
            ),
            profileTwoDs,
            null
        );

        assertNull(nearestBuilding);
    }

}
