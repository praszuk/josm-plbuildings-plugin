package org.openstreetmap.josm.plugins.plbuildings;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;

public class BuildingLevelsWithRoofTest {
    @Test
    public void testNotCompleteLevelsData() {
        OsmPrimitive selected1 = new Way();
        selected1.put("building", "house");

        OsmPrimitive newPrimitive1 = new Way();
        newPrimitive1.put("building", "house");

        Assertions.assertFalse(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected1, newPrimitive1));

        OsmPrimitive selected2 = new Way();
        selected2.put("building", "house");
        selected2.put("building:levels", "2");

        OsmPrimitive newPrimitive2 = new Way();
        newPrimitive2.put("building", "house");

        Assertions.assertFalse(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected2, newPrimitive2));

        OsmPrimitive selected3 = new Way();
        selected3.put("building", "house");
        selected3.put("building:levels", "2");

        OsmPrimitive newPrimitive3 = new Way();
        newPrimitive3.put("building", "house");
        newPrimitive3.put("building:levels", "2");

        Assertions.assertFalse(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected3, newPrimitive3));
    }

    @Test
    public void testCompleteDataButWithIncorrectNumberFormat() {
        OsmPrimitive selected1 = new Way();
        selected1.put("building", "house");
        selected1.put("building:levels", "1");
        selected1.put("roof:levels", "1");

        OsmPrimitive newPrimitive1 = new Way();
        newPrimitive1.put("building", "house");
        newPrimitive1.put("building:levels", "LEVELS2");

        Assertions.assertFalse(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected1, newPrimitive1));
    }

    @Test
    public void testCompleteDataWithDifferentLevelsButMatchedIfSumBuildingLevelsWithRoofLevels() {
        OsmPrimitive selected1 = new Way();
        selected1.put("building", "house");
        selected1.put("building:levels", "1");
        selected1.put("roof:levels", "1");

        OsmPrimitive newPrimitive1 = new Way();
        newPrimitive1.put("building", "house");
        newPrimitive1.put("building:levels", "2");

        Assertions.assertTrue(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected1, newPrimitive1));

        OsmPrimitive selected2 = new Way();
        selected2.put("building:levels", "1");
        selected2.put("roof:levels", "1");

        OsmPrimitive newPrimitive2 = new Way();
        newPrimitive2.put("building", "house");
        newPrimitive2.put("building:levels", "3");

        Assertions.assertFalse(PreCheckUtils.isBuildingLevelsWithRoofEquals(selected2, newPrimitive2));
    }
}
