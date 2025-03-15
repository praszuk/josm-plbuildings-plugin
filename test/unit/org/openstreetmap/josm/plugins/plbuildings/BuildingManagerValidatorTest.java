package org.openstreetmap.josm.plugins.plbuildings;

import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceServer;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class BuildingManagerValidatorTest {
    @RegisterExtension
    static JOSMTestRules rule = new JOSMTestRules();

    private DataSet allowedDataSet;
    private Way allowedWay;
    private LatLon allowedLatLon;
    private DataSourceConfig dataSourceConfig;

    @BeforeEach
    void setUp() {
        allowedDataSet = new DataSet();

        Node node1 = new Node(new LatLon(0.0, 0.0));
        Node node2 = new Node(new LatLon(0.1, 0.1));
        Node node3 = new Node(new LatLon(0.2, 0.2));
        allowedDataSet.addPrimitive(node1);
        allowedDataSet.addPrimitive(node2);
        allowedDataSet.addPrimitive(node3);
        Way selectedWay = new Way();
        allowedDataSet.addPrimitive(selectedWay);
        selectedWay.addNode(node1);
        selectedWay.addNode(node2);
        selectedWay.addNode(node3);
        selectedWay.addNode(node1);

        allowedWay = selectedWay;
        allowedLatLon = new LatLon(52.231, 21.123);

        dataSourceConfig = new DataSourceConfig();

        DataSourceServer server = new DataSourceServer("Test server", "http://example.com");
        DataSourceProfile profile = new DataSourceProfile(
            "Test server",
            "geometry_name",
            "tag_name",
            "test profile",
            true
        );
        dataSourceConfig.addServer(server);
        dataSourceConfig.addProfile(profile);
        dataSourceConfig.setCurrentProfile(profile);
    }

    @Test
    void testValid() {
        final boolean[] methodCalled = {false};

        new MockUp<PreCheckUtils>() {
            @Mock
            public void validateSelectedWay(Way ignore) {
                methodCalled[0] = true;
            }
        };

        Assertions.assertDoesNotThrow(() -> new BuildingsImportManager(
            allowedDataSet,
            allowedLatLon,
            allowedWay
        ).validate());

        assert methodCalled[0] : "Expected validateSelectedWay to be called";
    }

    @Test
    void testInvalidLatLon() {
        Exception exception = Assertions.assertThrows(
            ImportActionCanceledException.class,
            () -> new BuildingsImportManager(allowedDataSet, null, allowedWay).validate()
        );
        Assertions.assertEquals("Cursor outside the map view!", exception.getMessage());
    }

    @Test
    void testInvalidCurrentProfile() {
        dataSourceConfig.setCurrentProfile(null);
        Exception exception = Assertions.assertThrows(
            ImportActionCanceledException.class,
            () -> new BuildingsImportManager(allowedDataSet, allowedLatLon, allowedWay).validate()
        );
        Assertions.assertEquals("No data source profile selected!", exception.getMessage());
    }
}
