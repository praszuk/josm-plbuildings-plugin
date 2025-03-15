package org.openstreetmap.josm.plugins.plbuildings;


import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.utils.CloneBuilding.cloneBuilding;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;

public class SimpleBuildingImportTest {
    @Test
    public void testImportBuildingNoCloseNodesJustOneBuildingInDataset() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(importDataSet);

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
        Way building = (Way) ds.getWays().toArray()[0];
        Assertions.assertEquals(4, building.getNodesCount() - 1);
        Assertions.assertTrue(building.hasTag("building"));
    }

    @Test
    public void testImportEmptyDataSet() {
        DataSet importDataSet = new DataSet();

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();
        Assertions.assertTrue(ds.isEmpty());
    }

    @Test
    public void testImportDataSetWithMultipleBuildingsButImportOnlyOne() {
        DataSet importDataSet = importOsmFile(new File("test/data/simple_multiple_buildings.osm"), "");
        Assertions.assertNotNull(importDataSet);
        Assertions.assertTrue(importDataSet.getWays().size() > 1);

        DataSet ds = new DataSet();

        Way expectedBuilding = importDataSet.getWays().iterator().next();
        Node buildingNode = expectedBuilding.getNodes().iterator().next();
        LatLon latLon = new LatLon(buildingNode.lat(), buildingNode.lon());

        BuildingsImportManager manager = new BuildingsImportManager(ds, latLon, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();
        Assertions.assertEquals(1, ds.getWays().size());
    }

    @Test
    public void testImportBuildingWithoutOsmMetaData() {
        DataSet rawDataSet = importOsmFile(new File("test/data/simple_building.osm"), "");
        Assertions.assertNotNull(rawDataSet);

        DataSet importDataSet = new DataSet();
        rawDataSet.getWays().forEach(w -> importDataSet.addPrimitiveRecursive(cloneBuilding(w)));

        DataSet ds = new DataSet();
        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importDataSet));
        manager.setCurrentProfile(testProfile);
        manager.processDownloadedData();

        Assertions.assertEquals(1, ds.getWays().size());
    }
}
