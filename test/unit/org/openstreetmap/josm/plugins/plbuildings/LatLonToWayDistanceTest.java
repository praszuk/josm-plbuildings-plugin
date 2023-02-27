package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.LatLonToWayDistance;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LatLonToWayDistanceTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    @Test
    public void testSimpleWayWithVeryCloseNode(){
        LatLon latLon = new LatLon(0, 0);
        Way way = new Way();
        way.addNode(new Node(new LatLon(1, 0)));
        way.addNode(new Node(new LatLon(1, 5)));

        assertTrue(LatLonToWayDistance.minDistance(latLon, way) < 2.0);
    }

    @Test
    public void testSimpleWayWithVeryFarNodesButCloseLineAndItDoesNotMatter(){
        LatLon latLon = new LatLon(0, 0);
        Way way = new Way();
        way.addNode(new Node(new LatLon(-10, 0)));
        way.addNode(new Node(new LatLon(10, 0)));

        assertTrue(LatLonToWayDistance.minDistance(latLon, way) > 3.0);
    }
}
