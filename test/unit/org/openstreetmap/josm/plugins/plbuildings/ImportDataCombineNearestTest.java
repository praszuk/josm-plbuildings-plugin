package org.openstreetmap.josm.plugins.plbuildings;

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
import org.openstreetmap.josm.plugins.plbuildings.data.CombineNearestStrategy;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.isSameButClonedBuilding;

public class ImportDataCombineNearestTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    public DataSourceProfile profileOneDS;
    public DataSourceProfile profileTwoDS;
    public DataSet emptyDS;
    public DataSet oneBuildingDS;
    public DataSet multipleBuildingDS;

    public DataSet bothOverlapOver60oneBuildingGeometryDS;
    public DataSet bothOverlapOver60oneBuildingTagsDS;

    public DataSet bothOverlapLt60oneBuildingGeometryDS;
    public DataSet bothOverlapLt60oneBuildingTagsDS;

    @Before
    public void setUp(){
        ProjectionRegistry.setProjection(Projections.getProjectionByCode("EPSG:4326"));

        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");

        this.profileOneDS = new DataSourceProfile(server.getName(), "ds1", "ds1", "profile1");
        this.profileTwoDS = new DataSourceProfile(server.getName(), "ds1", "ds2", "profile2");

        this.emptyDS = new DataSet();
        this.oneBuildingDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/one_building.osm"),
            ""
        );
        this.multipleBuildingDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/multiple_buildings.osm"),
            ""
        );

        this.bothOverlapOver60oneBuildingGeometryDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapOver60oneBuildingTagsDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_tags.osm"),
            ""
        );

        this.bothOverlapLt60oneBuildingGeometryDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapLt60oneBuildingTagsDS = importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_tags.osm"),
            ""
        );

    }

    @Test
    public void testOneDSEmptyDS(){
        OsmPrimitive nearestBuilding = BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDS.getGeometry(), emptyDS),
            profileOneDS,
            null
        );
        assertNull(nearestBuilding);
    }

    @Test
    public void testOneDSOneBuilding(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDS.getGeometry(), oneBuildingDS),
            profileOneDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testOneDSMultipleBuilding(){
        Way[] buildings = multipleBuildingDS.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileOneDS.getGeometry(), multipleBuildingDS),
            profileOneDS,
            latLon
        );

        assertTrue(multipleBuildingDS.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSEmptyBothDS(){
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(profileTwoDS.getGeometry(), emptyDS),
            profileTwoDS,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAcceptOneDS(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ACCEPT.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), emptyDS,
                profileTwoDS.getTags(), oneBuildingDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAcceptOneDSMultipleBuilding(){
        Way[] buildings = multipleBuildingDS.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ACCEPT.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), emptyDS,
                profileTwoDS.getTags(), multipleBuildingDS
            ),
            profileTwoDS,
            latLon
        );

        assertTrue(multipleBuildingDS.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyCancelOneDS(){
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.CANCEL.toString());

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), emptyDS,
                profileTwoDS.getTags(), oneBuildingDS
            ),
            profileTwoDS,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAskUserAcceptOneDS(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingDataOneDsStrategy(String availableDataSource){
                return CombineNearestStrategy.ACCEPT;
            }
        };
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), emptyDS,
                profileTwoDS.getTags(), oneBuildingDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAskUserCancelOneDS(){
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingDataOneDsStrategy(String availableDataSource){
                return CombineNearestStrategy.CANCEL;
            }
        };
        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                    profileTwoDS.getGeometry(), emptyDS,
                    profileTwoDS.getTags(), oneBuildingDS
            ),
            profileTwoDS,
            null
        );

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDSBothDSOverlapOver60Merge(){
        Way expectedGeometryBuilding =  bothOverlapOver60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapOver60oneBuildingTagsDS.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());
        
        Way expectedBuilding = new Way();
        expectedGeometryBuilding.getNodes().forEach(expectedBuilding::addNode);
        expectedTagsBuilding.getKeys().forEach(expectedBuilding::put);

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), bothOverlapOver60oneBuildingGeometryDS,
                profileTwoDS.getTags(), bothOverlapOver60oneBuildingTagsDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserAccept(){
        Way expectedGeometryBuilding =  bothOverlapLt60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDS.getWays().iterator().next();
        
        Way expectedBuilding = new Way();
        expectedGeometryBuilding.getNodes().forEach(expectedBuilding::addNode);
        expectedTagsBuilding.getKeys().forEach(expectedBuilding::put);

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingOverlapStrategy(
                    String geomDS, String tagsDS, double overlapPercentage){
                return CombineNearestStrategy.ACCEPT;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
                profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(expectedBuilding, nearestBuilding));
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserAcceptGeometry(){
        Way expectedGeometryBuilding =  bothOverlapLt60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDS.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingOverlapStrategy(
                    String geomDS, String tagsDS, double overlapPercentage
            ){
                return CombineNearestStrategy.ACCEPT_GEOMETRY;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                    profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
                    profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(expectedTagsBuilding.getKeys().containsKey("building"));
        assertFalse(nearestBuilding.getKeys().containsKey("building"));
        assertTrue(isSameButClonedBuilding(nearestBuilding, expectedGeometryBuilding));
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserAcceptTags(){
        Way expectedGeometryBuilding =  bothOverlapLt60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDS.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingOverlapStrategy(
                    String geomDS, String tagsDS, double overlapPercentage
            ){
                return CombineNearestStrategy.ACCEPT_TAGS;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
                profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
            ),
            profileTwoDS,
            latLon
        );

        assertNotNull(nearestBuilding);
        assertTrue(isSameButClonedBuilding(nearestBuilding, expectedTagsBuilding));
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserCancel(){
        new MockUp<BuildingsImportManager>(){
            @Mock
            CombineNearestStrategy getImportBuildingOverlapStrategy(
                    String geomDS, String tagsDS, double overlapPercentage
            ){
                return CombineNearestStrategy.CANCEL;
            }
        };

        Way nearestBuilding = (Way) BuildingsImportManager.getNearestImportedBuilding(
            new BuildingsImportData(
                profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
                profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
            ),
            profileTwoDS,
            null
        );

        assertNull(nearestBuilding);
    }

}
