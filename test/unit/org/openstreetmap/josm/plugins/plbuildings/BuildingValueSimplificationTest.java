package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildingValueSimplificationTest {

    @Test
    public void testNewPrimitiveHasSimplifiedBuildingValue(){
        OsmPrimitive selected1 = new Way();
        selected1.put("building", "detached");

        OsmPrimitive newPrimitive1 = new Way();
        newPrimitive1.put("building", "house");

        assertTrue(PreCheckUtils.isBuildingValueSimplification(selected1, newPrimitive1));

        OsmPrimitive selected2 = new Way();
        selected2.put("building", "detached");

        OsmPrimitive newPrimitive2 = new Way();

        assertTrue(PreCheckUtils.isBuildingValueSimplification(selected2, newPrimitive2));
    }

    @Test
    public void testNewPrimitiveHasNotSimplifiedBuildingValue(){
        OsmPrimitive selected1 = new Way();
        selected1.put("building", "yes");

        OsmPrimitive newPrimitive1 = new Way();
        newPrimitive1.put("building", "house");

        assertFalse(PreCheckUtils.isBuildingValueSimplification(selected1, newPrimitive1));

        OsmPrimitive selected2 = new Way();
        selected2.put("building", "house");

        OsmPrimitive newPrimitive2 = new Way();
        newPrimitive2.put("building", "detached");

        assertFalse(PreCheckUtils.isBuildingValueSimplification(selected2, newPrimitive2));

        OsmPrimitive selected3 = new Way();

        OsmPrimitive newPrimitive3 = new Way();
        newPrimitive3.put("building", "house");

        assertFalse(PreCheckUtils.isBuildingValueSimplification(selected3, newPrimitive3));
    }
}
