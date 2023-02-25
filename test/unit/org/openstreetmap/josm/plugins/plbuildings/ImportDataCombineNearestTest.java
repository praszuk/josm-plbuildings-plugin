package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
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

    @Before
    public void setUp(){
        DataSourceServer server = new DataSourceServer("server", "127.0.0.1");

        this.profileOneDS = new DataSourceProfile(server.getName(), "ds1", "ds1", "profile1");
        this.profileTwoDS = new DataSourceProfile(server.getName(), "ds1", "ds2", "profile2");

        this.emptyDS = new DataSet();
        this.oneBuildingDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest/one_building.osm"),
            ""
        );
        this.multipleBuildingDS = ImportUtils.importOsmFile(
            new File("test/data/import_data_combine_nearest/multiple_buildings.osm"),
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
}
