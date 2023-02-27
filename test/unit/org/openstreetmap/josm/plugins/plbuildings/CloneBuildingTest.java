package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.isSameButClonedBuilding;
import static org.openstreetmap.josm.plugins.plbuildings.utils.CloneBuilding.cloneBuilding;

public class CloneBuildingTest {
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().main();

    Way buildingToClone;

    @Before
    public void setUp(){
        this.buildingToClone = new Way(5);
        this.buildingToClone.put("building", "house");
        this.buildingToClone.put("building:levels", "2");

        Node startNode = new Node(new LatLon(0.0, 0.0));
        this.buildingToClone.setNodes(List.of( // yes it's BIG building, but it doesn't matter
            startNode,
            new Node(new LatLon(0.0, 0.1)),
            new Node(new LatLon(0.1, 0.1)),
            new Node(new LatLon(0.1, 0.0)),
            startNode
        ));

        this.buildingToClone.setOsmId(1, 1); // to avoid incomplete errors
        assertTrue(this.buildingToClone.isClosed());
    }

    @Test
    public void testClonedTags(){
        Way clonedBuilding = (Way) cloneBuilding(this.buildingToClone);
        assertEquals(clonedBuilding.getKeys(), this.buildingToClone.getKeys());

        this.buildingToClone.remove("building");
        assertNotEquals(clonedBuilding.getKeys(), this.buildingToClone.getKeys());
    }

    @Test
    public void testClonedNodes(){
        Way clonedBuilding = (Way)cloneBuilding(this.buildingToClone);
        assertTrue(isSameButClonedBuilding(clonedBuilding, this.buildingToClone));

        this.buildingToClone.removeNode(this.buildingToClone.getNode(0));
        assertNotEquals(clonedBuilding.getNodes().size(), this.buildingToClone.getNodes().size());
    }

    @Test
    public void testClonedId(){
        Way clonedBuilding = (Way)cloneBuilding(this.buildingToClone);
        assertNotEquals(clonedBuilding.getId(), this.buildingToClone.getId());
    }

}
