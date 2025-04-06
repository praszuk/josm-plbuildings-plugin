package org.openstreetmap.josm.plugins.plbuildings.importstrategy;

import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.DATA_SOURCE;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.importOsmFile;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.testProfile;
import static org.openstreetmap.josm.plugins.plbuildings.validators.BuildingsWayValidator.isBuildingWayValid;

import java.io.File;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.actions.ExpertToggleAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.plugins.plbuildings.actions.BuildingsImportAction;
import org.openstreetmap.josm.plugins.plbuildings.enums.ImportMode;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportStats;
import org.openstreetmap.josm.testutils.annotations.Projection;

@Projection
public class GeometryUpdateTest {
    @BeforeEach
    public void setUp() {
        ExpertToggleAction.getInstance().setExpert(true);
        BuildingsSettings.IMPORT_MODE.put(ImportMode.GEOMETRY);
        BuildingsSettings.IMPORT_STATS.put(BuildingsSettings.IMPORT_STATS.getDefaultValue());

        new MockUp<BuildingsImportAction>() {
            @Mock
            public Bounds getUserFrameViewBounds() {
                return null;
            }
        };
    }

    @AfterEach
    public void teardown() {
        BuildingsSettings.IMPORT_STATS.put(BuildingsSettings.IMPORT_STATS.getDefaultValue());
        UndoRedoHandler.getInstance().clean();
    }

    @Test
    void testImportWithUpdateGeometry() {
        DataSet importData = importOsmFile(new File("test/data/import_strategy/imported_building.osm"), "");
        Assertions.assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];

        DataSet ds = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "test", null));

        BuildingsImportStats stats = BuildingsImportStats.getInstance();
        int replaceCounter = stats.getImportWithReplaceCounter();
        int tagsUpdateCounter = stats.getImportWithTagsUpdateCounter();
        int geometryUpdateCounter = stats.getImportWithGeometryUpdateCounter();

        Assertions.assertNotNull(ds);

        Way buildingToReplace = ds.getWays().stream().findFirst().orElseThrow();
        ds.setSelected(buildingToReplace);
        int numberOfTags = buildingToReplace.getKeys().size();

        Assertions.assertNotEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
        Assertions.assertNotEquals(buildingToReplace.get("building"), buildingToImport.get("building"));

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);

        manager.processDownloadedData();
        Assertions.assertTrue(isBuildingWayValid(buildingToReplace));

        Assertions.assertEquals(buildingToImport.getNodesCount(), buildingToReplace.getNodesCount());
        Assertions.assertNotEquals(buildingToImport.get("building"), buildingToReplace.get("building"));
        Assertions.assertEquals(numberOfTags, buildingToReplace.getKeys().size());

        Assertions.assertEquals(replaceCounter + 1, stats.getImportWithReplaceCounter());
        Assertions.assertEquals(geometryUpdateCounter + 1, stats.getImportWithGeometryUpdateCounter());
        Assertions.assertEquals(tagsUpdateCounter, stats.getImportWithTagsUpdateCounter());

        Command command = UndoRedoHandler.getInstance().getLastCommand();
        Assertions.assertNotNull(command);

        command.undoCommand();

        Assertions.assertNotEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
    }

    @Test
    void testImportCanceledWithoutSelectedBuilding() {
        DataSet importData = importOsmFile(new File("test/data/import_strategy/imported_building.osm"), "");
        Assertions.assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];

        DataSet ds = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "test", null));

        BuildingsImportStats stats = BuildingsImportStats.getInstance();
        int replaceCounter = stats.getImportWithReplaceCounter();
        int tagsUpdateCounter = stats.getImportWithTagsUpdateCounter();
        int geometryUpdateCounter = stats.getImportWithGeometryUpdateCounter();

        Assertions.assertNotNull(ds);

        Way buildingToReplace = ds.getWays().stream().findFirst().orElseThrow();

        Assertions.assertNotEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
        Assertions.assertNotEquals(buildingToReplace.get("building"), buildingToImport.get("building"));

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, null);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);

        manager.processDownloadedData();

        Assertions.assertNotEquals(buildingToImport.getNodesCount(), buildingToReplace.getNodesCount());
        Assertions.assertNotEquals(buildingToImport.get("building"), buildingToReplace.get("building"));

        Assertions.assertEquals(replaceCounter, stats.getImportWithReplaceCounter());
        Assertions.assertEquals(geometryUpdateCounter, stats.getImportWithGeometryUpdateCounter());
        Assertions.assertEquals(tagsUpdateCounter, stats.getImportWithTagsUpdateCounter());
    }

    @Test
    void testImportCanceledDuplicatedBuilding() {
        DataSet importData = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        Assertions.assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];

        DataSet ds = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "test", null));

        BuildingsImportStats stats = BuildingsImportStats.getInstance();
        int replaceCounter = stats.getImportWithReplaceCounter();
        int tagsUpdateCounter = stats.getImportWithTagsUpdateCounter();
        int geometryUpdateCounter = stats.getImportWithGeometryUpdateCounter();

        Assertions.assertNotNull(ds);

        Way buildingToReplace = ds.getWays().stream().findFirst().orElseThrow();

        Assertions.assertEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
        Assertions.assertEquals(buildingToReplace.get("building"), buildingToImport.get("building"));

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);

        manager.processDownloadedData();

        Assertions.assertEquals(buildingToImport.getNodesCount(), buildingToReplace.getNodesCount());
        Assertions.assertEquals(buildingToImport.get("building"), buildingToReplace.get("building"));

        Assertions.assertEquals(replaceCounter, stats.getImportWithReplaceCounter());
        Assertions.assertEquals(geometryUpdateCounter, stats.getImportWithGeometryUpdateCounter());
        Assertions.assertEquals(tagsUpdateCounter, stats.getImportWithTagsUpdateCounter());
        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }

    @Test
    void testImportCanceledNodeWithEntranceOnTheBuildingEdge() {
        DataSet importData = importOsmFile(new File("test/data/import_strategy/imported_building.osm"), "");
        Assertions.assertNotNull(importData);

        Way buildingToImport = (Way) importData.getWays().toArray()[0];

        DataSet ds = importOsmFile(new File("test/data/import_strategy/current_building.osm"), "");
        MainApplication.getLayerManager().addLayer(new OsmDataLayer(ds, "test", null));

        BuildingsImportStats stats = BuildingsImportStats.getInstance();
        int replaceCounter = stats.getImportWithReplaceCounter();
        int tagsUpdateCounter = stats.getImportWithTagsUpdateCounter();
        int geometryUpdateCounter = stats.getImportWithGeometryUpdateCounter();

        Assertions.assertNotNull(ds);

        Way buildingToReplace = ds.getWays().stream().findFirst().orElseThrow();
        buildingToReplace.getNode(0).put("entrance", "yes");
        ds.setSelected(buildingToReplace);
        int numberOfTags = buildingToReplace.getKeys().size();

        Assertions.assertNotEquals(buildingToReplace.getNodesCount(), buildingToImport.getNodesCount());
        Assertions.assertNotEquals(buildingToReplace.get("building"), buildingToImport.get("building"));

        BuildingsImportManager manager = new BuildingsImportManager(ds, null, buildingToReplace);
        manager.setImportedData(new BuildingsImportData(DATA_SOURCE, importData));
        manager.setCurrentProfile(testProfile);

        manager.processDownloadedData();
        Assertions.assertTrue(isBuildingWayValid(buildingToReplace));

        Assertions.assertNotEquals(buildingToImport.getNodesCount(), buildingToReplace.getNodesCount());
        Assertions.assertNotEquals(buildingToImport.get("building"), buildingToReplace.get("building"));
        Assertions.assertEquals(numberOfTags, buildingToReplace.getKeys().size());

        Assertions.assertEquals(replaceCounter, stats.getImportWithReplaceCounter());
        Assertions.assertEquals(geometryUpdateCounter, stats.getImportWithGeometryUpdateCounter());
        Assertions.assertEquals(tagsUpdateCounter, stats.getImportWithTagsUpdateCounter());

        Assertions.assertNull(UndoRedoHandler.getInstance().getLastCommand());
    }
}
