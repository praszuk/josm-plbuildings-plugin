package org.openstreetmap.josm.plugins.plbuildings;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openstreetmap.josm.plugins.plbuildings.utils.PreCheckUtils;

public class BuildingValueSimplificationTest {

    @Test
    void testSelectedEmptyBuildingValue() {
        Assertions.assertFalse(PreCheckUtils.isBuildingValueSimplification("", "yes"));
    }

    @Test
    void testNewPrimitiveEmptyValue() {
        Assertions.assertTrue(PreCheckUtils.isBuildingValueSimplification("yes", ""));
    }

    @ParameterizedTest
    @CsvSource({
        "detached,house", "semidetached_house,house",
        "house,residential", "detached,residential",
        "garage,outbuilding", "barn,outbuilding",
        "house,yes", "outbuilding,yes"
    })
    void testNewPrimitiveHasSimplifiedBuildingValue(String selectedValue, String newPrimitiveValue) {
        Assertions.assertTrue(PreCheckUtils.isBuildingValueSimplification(selectedValue, newPrimitiveValue));
    }

    @ParameterizedTest
    @CsvSource({"house,detached", "residential,house", "construction,yes", "yes,yes"})
    public void testNewPrimitiveHasNotSimplifiedBuildingValue(String selectedValue, String newPrimitiveValue) {
        Assertions.assertFalse(PreCheckUtils.isBuildingValueSimplification(selectedValue, newPrimitiveValue));
    }
}
