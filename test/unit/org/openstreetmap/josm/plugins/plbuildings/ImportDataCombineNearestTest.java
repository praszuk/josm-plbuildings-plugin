package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.data.CombineNearestStrategy;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

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
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");

        this.profileOneDS = new DataSourceProfile(server.getName(), "ds1", "ds1", "profile1");
        this.profileTwoDS = new DataSourceProfile(server.getName(), "ds1", "ds2", "profile2");

        this.emptyDS = new DataSet();
        this.oneBuildingDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/one_building.osm"),
            ""
        );
        this.multipleBuildingDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_one_ds/multiple_buildings.osm"),
            ""
        );

        this.bothOverlapOver60oneBuildingGeometryDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapOver60oneBuildingTagsDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_gt_60/one_building_tags.osm"),
            ""
        );

        this.bothOverlapLt60oneBuildingGeometryDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_geometry.osm"),
            ""
        );
        this.bothOverlapLt60oneBuildingTagsDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest_both_ds_overlap_lt_60/one_building_tags.osm"),
            ""
        );

    }

    @Test
    public void testOneDSEmptyDS(){
        BuildingsImportManager manager = new BuildingsImportManager(null,null, null);
        manager.setImportedData(new BuildingsImportData(profileOneDS.getGeometry(), emptyDS));
        manager.setDataSourceProfile(profileOneDS);

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertNull(nearestBuilding);
    }

    @Test
    public void testOneDSOneBuilding(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(null, latLon, null);
        manager.setImportedData(new BuildingsImportData(profileOneDS.getGeometry(), oneBuildingDS));
        manager.setDataSourceProfile(profileOneDS);

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertNotNull(nearestBuilding);
        assertEquals(expectedBuilding, nearestBuilding);
    }

    @Test
    public void testOneDSMultipleBuilding(){
        Way[] buildings = multipleBuildingDS.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(null, latLon, null);
        manager.setImportedData(new BuildingsImportData(profileOneDS.getGeometry(), multipleBuildingDS));
        manager.setDataSourceProfile(profileOneDS);

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertTrue(multipleBuildingDS.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertEquals(expectedBuilding, nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyBothDS(){
        BuildingsImportManager manager = new BuildingsImportManager(null,null, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), emptyDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAcceptOneDS(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());
        
        BuildingsImportManager manager = new BuildingsImportManager(null,latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), oneBuildingDS
        ));
        manager.setDataSourceProfile(profileTwoDS);
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(
            CombineNearestStrategy.ACCEPT.toString()
        );

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertEquals(nearestBuilding, expectedBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAcceptOneDSMultipleBuilding(){
        Way[] buildings = multipleBuildingDS.getWays().toArray(Way[]::new);

        Way expectedBuilding = buildings[2]; // avoid first building to check "nearest" instead of id or something else
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(null, latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), multipleBuildingDS
        ));
        manager.setDataSourceProfile(profileTwoDS);
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(
            CombineNearestStrategy.ACCEPT.toString()
        );

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertTrue(multipleBuildingDS.getWays().size() > 2);
        assertNotNull(nearestBuilding);
        assertEquals(expectedBuilding, nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyCancelOneDS(){
        BuildingsImportManager manager = new BuildingsImportManager(null,null, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), oneBuildingDS
        ));
        manager.setDataSourceProfile(profileTwoDS);
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.CANCEL.toString());

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertNull(nearestBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAskUserAcceptOneDS(){
        Way expectedBuilding = oneBuildingDS.getWays().iterator().next();

        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());
        
        BuildingsImportManager manager = new BuildingsImportManager(null, latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), oneBuildingDS
        ));
        manager.setDataSourceProfile(profileTwoDS);
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseOneDS(){
                return CombineNearestStrategy.ACCEPT;
            }
        };
        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertEquals(nearestBuilding, expectedBuilding);
    }

    @Test
    public void testTwoDSEmptyOneDSStrategyAskUserCancelOneDS(){
        BuildingsImportManager manager = new BuildingsImportManager(null,null, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), emptyDS,
            profileTwoDS.getTags(), oneBuildingDS
        ));
        manager.setDataSourceProfile(profileTwoDS);
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestStrategy.ASK_USER.toString());

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseOneDS(){
                return CombineNearestStrategy.CANCEL;
            }
        };
        Way nearestBuilding = manager.getNearestBuildingFromImportData();

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

        BuildingsImportManager manager = new BuildingsImportManager(null,latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), bothOverlapOver60oneBuildingGeometryDS,
            profileTwoDS.getTags(), bothOverlapOver60oneBuildingTagsDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertEquals(nearestBuilding, expectedBuilding);
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
        
        BuildingsImportManager manager = new BuildingsImportManager(null,latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
            profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseBothDS(){
                return CombineNearestStrategy.ACCEPT;
            }
        };

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertEquals(nearestBuilding, expectedBuilding);
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserAcceptGeometry(){
        Way expectedGeometryBuilding =  bothOverlapLt60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDS.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(null,latLon, null);
        manager.setImportedData(new BuildingsImportData(
                profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
                profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseBothDS(){
                return CombineNearestStrategy.ACCEPT_GEOMETRY;
            }
        };

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertTrue(expectedTagsBuilding.getKeys().containsKey("building"));
        assertFalse(nearestBuilding.getKeys().containsKey("building"));
        assertEquals(nearestBuilding, expectedGeometryBuilding);
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserAcceptTags(){
        Way expectedGeometryBuilding =  bothOverlapLt60oneBuildingGeometryDS.getWays().iterator().next();
        Way expectedTagsBuilding = bothOverlapLt60oneBuildingTagsDS.getWays().iterator().next();

        Node buildingNode = expectedGeometryBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(null,latLon, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
            profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseBothDS(){
                return CombineNearestStrategy.ACCEPT_TAGS;
            }
        };

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertEquals(nearestBuilding, expectedTagsBuilding);
    }

    @Test
    public void testTwoDSBothDSOverlapLt60AskUserCancel(){
        BuildingsImportManager manager = new BuildingsImportManager(null, null, null);
        manager.setImportedData(new BuildingsImportData(
            profileTwoDS.getGeometry(), bothOverlapLt60oneBuildingGeometryDS,
            profileTwoDS.getTags(), bothOverlapLt60oneBuildingTagsDS
        ));
        manager.setDataSourceProfile(profileTwoDS);

        new MockUp<BuildingsImportManager>(){
            @Mock
            public CombineNearestStrategy askUserToUseBothDS(){
                return CombineNearestStrategy.CANCEL;
            }
        };

        Way nearestBuilding = manager.getNearestBuildingFromImportData();

        assertNull(nearestBuilding);
    }

}
