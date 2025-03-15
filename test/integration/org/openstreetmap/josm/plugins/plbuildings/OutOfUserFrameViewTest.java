package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testServer;

import java.io.File;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.enums.CombineNearestOneDsStrategy;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;

public class OutOfUserFrameViewTest {
    @Test
    public void testImportBuildingCompletelyInUserFrameView() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                BBox bbox = new BBox();
                importDataSet.getNodes().forEach(node -> bbox.addPrimitive(node, 0.001));
                return new Bounds(bbox.getMinLat(), bbox.getMinLon(), bbox.getMaxLat(), bbox.getMaxLon());
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
    }

    @Test
    public void testImportBuildingPartiallyInUserFrameView() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                BBox bbox = new BBox();
                bbox.addPrimitive(importDataSet.getNodes().stream().findFirst().orElseThrow(), 0.0000001);
                Assertions.assertFalse(
                    bbox.contains(importDataSet.getNodes().stream().skip(1).findFirst().orElseThrow()));

                return new Bounds(bbox.getMinLat(), bbox.getMinLon(), bbox.getMaxLat(), bbox.getMaxLon());
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
    }

    @Test
    public void testImportBuildingNotInUserFrameView() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                return new Bounds(new LatLon(0.0, 0.0), new LatLon(0.01, 0.01));
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertTrue(ds.getWays().isEmpty());
    }

    @Test
    public void testImportBuildingOneDsNotInUserFrameView() {
        DataSet correctDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(correctDataSet);

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                BBox bbox = new BBox();
                correctDataSet.getNodes().forEach(node -> bbox.addPrimitive(node, 0.001));
                return new Bounds(bbox.getMinLat(), bbox.getMinLon(), bbox.getMaxLat(), bbox.getMaxLon());
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        DataSourceProfile testProfile = new DataSourceProfile(
            testServer.getName(), "geometry_source", "tags_source", "profile1"
        );
        DataSet incorrectImportDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        incorrectImportDataSet.getNodes().forEach(node -> {
            LatLon nodeLatLon = node.getCoor();
            node.setCoor(new LatLon(nodeLatLon.lat() + 10, nodeLatLon.lon() + 10));
        });

        manager.setImportedData(new BuildingsImportData(
            testProfile.getGeometry(), correctDataSet, testProfile.getTags(), incorrectImportDataSet)
        );
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertTrue(ds.getWays().isEmpty());
    }

    @Test
    public void testImportBuildingOneDsCorrectSecondDsEmpty() {
        BuildingsSettings.COMBINE_NEAREST_BUILDING_ONE_DS_STRATEGY.put(CombineNearestOneDsStrategy.ACCEPT.toString());

        DataSet correctDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(correctDataSet);

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                BBox bbox = new BBox();
                correctDataSet.getNodes().forEach(node -> bbox.addPrimitive(node, 0.001));
                return new Bounds(bbox.getMinLat(), bbox.getMinLon(), bbox.getMaxLat(), bbox.getMaxLon());
            }
        };

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        DataSourceProfile testProfile = new DataSourceProfile(
            testServer.getName(), "geometry_source", "tags_source", "profile1"
        );
        DataSet emptydataSet = new DataSet();
        manager.setImportedData(new BuildingsImportData(
            testProfile.getGeometry(), correctDataSet, testProfile.getTags(), emptydataSet)
        );
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
    }
}
