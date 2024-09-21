package org.openstreetmap.josm.plugins.plbuildings;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;

public class BuildingValueSimplificationTest {

    @Test
    void testSelectedEmptyBuildingValue() {
        OsmPrimitive selected = new Way();
        OsmPrimitive newPrimitive = new Way();
        newPrimitive.put("building", "yes");

        Assertions.assertFalse(PreCheckUtils.isBuildingValueSimplification(selected, newPrimitive));
    }

    @Test
    void testNewPrimitiveEmptyValue() {
        OsmPrimitive selected = new Way();
        selected.put("building", "yes");
        OsmPrimitive newPrimitive = new Way();

        Assertions.assertTrue(PreCheckUtils.isBuildingValueSimplification(selected, newPrimitive));
    }

    @ParameterizedTest
    @CsvSource({
        "detached,house", "semidetached_house,house",
        "house,residential", "detached,residential",
        "garage,outbuilding", "barn,outbuilding",
        "house,yes", "outbuilding,yes"
    })
    void testNewPrimitiveHasSimplifiedBuildingValue(String selectedValue, String newPrimitiveValue) {
        OsmPrimitive selected = new Way();
        OsmPrimitive newPrimitive = new Way();
        selected.put("building", selectedValue);
        newPrimitive.put("building", newPrimitiveValue);

        Assertions.assertTrue(PreCheckUtils.isBuildingValueSimplification(selected, newPrimitive));
    }

    @ParameterizedTest
    @CsvSource({"house,detached", "residential,house", "construction,yes", "yes,yes"})
    public void testNewPrimitiveHasNotSimplifiedBuildingValue(String selectedValue, String newPrimitiveValue) {
        OsmPrimitive selected = new Way();
        OsmPrimitive newPrimitive = new Way();
        selected.put("building", selectedValue);
        newPrimitive.put("building", newPrimitiveValue);

        Assertions.assertFalse(PreCheckUtils.isBuildingValueSimplification(selected, newPrimitive));
    }
}
