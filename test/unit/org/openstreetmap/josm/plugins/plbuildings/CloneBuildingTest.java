package org.openstreetmap.josm.plugins.plbuildings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openstreetmap.josm.plugins.plbuildings.ImportUtils.isSameButClonedBuilding;
import static org.openstreetmap.josm.plugins.plbuildings.utils.CloneBuilding.cloneBuilding;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class CloneBuildingTest {
    Way buildingToClone;

    @BeforeEach
    public void setUp() {
        this.buildingToClone = new Way(5);
        this.buildingToClone.put("building", "house");
        this.buildingToClone.put("building:levels", "2");

        Node startNode = new Node(new LatLon(0.0, 0.0));
        // yes it's BIG building, but it doesn't matter
        this.buildingToClone.setNodes(List.of(
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
    public void testClonedTags() {
        Way clonedBuilding = (Way) cloneBuilding(this.buildingToClone);
        assertEquals(this.buildingToClone.getKeys(), clonedBuilding.getKeys());

        this.buildingToClone.remove("building");
        assertNotEquals(this.buildingToClone.getKeys(), clonedBuilding.getKeys());
    }

    @Test
    public void testClonedNodes() {
        Way clonedBuilding = (Way) cloneBuilding(this.buildingToClone);
        assertTrue(isSameButClonedBuilding(clonedBuilding, this.buildingToClone));

        this.buildingToClone.removeNode(this.buildingToClone.getNode(0));
        assertNotEquals(this.buildingToClone.getNodes().size(), clonedBuilding.getNodes().size());
        assertEquals(clonedBuilding.firstNode(), clonedBuilding.lastNode());
    }

    @Test
    public void testClonedId() {
        Way clonedBuilding = (Way) cloneBuilding(this.buildingToClone);
        assertNotEquals(this.buildingToClone.getId(), clonedBuilding.getId());
    }

}
