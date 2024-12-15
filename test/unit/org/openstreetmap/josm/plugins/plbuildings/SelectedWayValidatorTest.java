package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils.validateSelectedWay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.exceptions.ImportActionCanceledException;

public class SelectedWayValidatorTest {
    private Way allowedWay;

    @BeforeEach
    void setUp() {
        DataSet ds = new DataSet();
        Node node1 = new Node(new LatLon(0.0, 0.0));
        Node node2 = new Node(new LatLon(0.1, 0.1));
        Node node3 = new Node(new LatLon(0.2, 0.2));
        ds.addPrimitive(node1);
        ds.addPrimitive(node2);
        ds.addPrimitive(node3);
        Way selectedWay = new Way();
        ds.addPrimitive(selectedWay);
        selectedWay.addNode(node1);
        selectedWay.addNode(node2);
        selectedWay.addNode(node3);
        selectedWay.addNode(node1);

        this.allowedWay = selectedWay;
    }

    @Test
    void testSelectedWayIsNull() {
        Assertions.assertDoesNotThrow(() -> validateSelectedWay(null));
    }

    @Test
    void testSelectedWayIsCorrectWay() {
        Assertions.assertDoesNotThrow(() -> validateSelectedWay(allowedWay));

        allowedWay.put("building", "yes");
        Assertions.assertDoesNotThrow(() -> validateSelectedWay(allowedWay));

        allowedWay.put("shop", "yes");
        Assertions.assertDoesNotThrow(() -> validateSelectedWay(allowedWay));

        allowedWay.put("amenity", "restaurant");
        Assertions.assertDoesNotThrow(() -> validateSelectedWay(allowedWay));
    }


    @Test
    void testWayIsNotClosed() {
        allowedWay.removeNode(allowedWay.lastNode());

        ImportActionCanceledException exception = Assertions.assertThrows(
            ImportActionCanceledException.class,
            () -> validateSelectedWay(allowedWay)
        );
        Assertions.assertEquals(
            "Selected object is not a closed line!", exception.getMessage()
        );
    }

    @Test
    void testWayContainsUnallowedTags() {
        allowedWay.put("landuse", "grass");
        ImportActionCanceledException exception = Assertions.assertThrows(
            ImportActionCanceledException.class,
            () -> validateSelectedWay(allowedWay)
        );
        Assertions.assertEquals("Selected object contains unallowed keys: landuse!", exception.getMessage());

        allowedWay.put("barrier", "fence");
        allowedWay.put("natural", "yes");
        exception = Assertions.assertThrows(
            ImportActionCanceledException.class,
            () -> validateSelectedWay(allowedWay)
        );
        Assertions.assertEquals(
            "Selected object contains unallowed keys: barrier, landuse, natural!", exception.getMessage()
        );
    }
}
