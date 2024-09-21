package org.openstreetmap.josm.plugins.plbuildings;

import static org.openstreetmap.josm.plugins.plbuildings.utils.TagConflictUtils.resolveTagConflictsDefault;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;

public class TagConflictUtilsTest {
    boolean isConflict(Collection<Tag> targetTags, Collection<Tag> sourceTags) {
        TagCollection allTags = new TagCollection();
        Way target = new Way();
        Way source = new Way();

        targetTags.forEach(target::put);
        sourceTags.forEach(source::put);
        allTags.add(target.getKeys().getTags());
        allTags.add(source.getKeys().getTags());

        Set<String> confictKeys = allTags.getKeysWithMultipleValues();

        resolveTagConflictsDefault(allTags, target, source);

        // If conflict is resolved, then each conflicting key contain 2 values, first empty "" second "new value"
        // e.g. "yes" ->"house" transforms to "" and "house" for "building" key.
        for (String key : confictKeys) {
            Set<String> values = allTags.getValues(key);
            if (values.size() != 2) {
                return true;
            } else if (!values.contains("")) {
                return true;
            }
        }
        return false;
    }

    @Test
    void testSameTags() {
        Assertions.assertFalse(isConflict(List.of(new Tag("building", "yes")), List.of(new Tag("building", "yes"))));
    }

    @Test
    void testConflictBuildingLevelsValuesButNotBuildingValues() {
        Assertions.assertTrue(isConflict(
            List.of(new Tag("building:levels", "1"), new Tag("building", "yes")),
            List.of(new Tag("building:levels", "2"), new Tag("building", "yes")))
        );
    }

    @ParameterizedTest
    @CsvSource({"yes,house,false", "yes,apartments,false", "yes,retail,false", "yes,construction,true"})
    void testNoCoflictByChangingBuildingYesToEverythingExceptConstruction(
        String currentValue, String newValue, boolean expectedConflictResult
    ) {
        boolean conflictResult = isConflict(
            List.of(new Tag("building", currentValue)), List.of(new Tag("building", newValue))
        );
        Assertions.assertEquals(conflictResult, expectedConflictResult);
    }

    @Test
    void testNoConflictWithAutoChangeFarmAuxiliaryToOutbuilding() {
        Assertions.assertFalse(isConflict(
            List.of(new Tag("building", "farm_auxiliary")), List.of(new Tag("building", "outbuilding")))
        );
    }

    @ParameterizedTest
    @CsvSource({"house", "apartments", "detached", "semidetached_house", "terrace"})
    void testNoConflictWithChangeResidentialForAnyLivingBuilding(String newValue) {
        Assertions.assertFalse(isConflict(
            List.of(new Tag("building", "residential")), List.of(new Tag("building", newValue)))
        );
    }

    @ParameterizedTest
    @CsvSource({"detached", "semidetached_house", "terrace"})
    void testNoConflictWithChangeHouseForDetailedHouseBuilding(String newValue) {
        Assertions.assertFalse(isConflict(
            List.of(new Tag("building", "residential")), List.of(new Tag("building", newValue)))
        );
    }


    @Test
    void testConflictWithChangeResidentialForNotLivingBuilding() {
        Assertions.assertTrue(isConflict(
            List.of(new Tag("building", "residential")), List.of(new Tag("building", "retail")))
        );
    }

    @Test
    void testNoConflictButHasSourceTagsButWithoutSurveyValue() {
        Assertions.assertFalse(isConflict(List.of(new Tag("source", "test")), List.of(new Tag("source", "new data"))));
        Assertions.assertFalse(
            isConflict(List.of(new Tag("source:building", "test")), List.of(new Tag("source:building", "new data")))
        );
        Assertions.assertFalse(
            isConflict(List.of(new Tag("source:geometry", "test")), List.of(new Tag("source:geometry", "new data")))
        );
    }

    @ParameterizedTest
    @CsvSource({"survey", "My survey"})
    void testConflictSourceTagsWithSurveyValue(String surveyValue) {
        Assertions.assertTrue(isConflict(
            List.of(new Tag("source", surveyValue)), List.of(new Tag("source", "new value")))
        );
        Assertions.assertTrue(isConflict(
            List.of(new Tag("source:building", surveyValue)), List.of(new Tag("source:building", "new value")))
        );
        Assertions.assertTrue(isConflict(
            List.of(new Tag("source:geometry", surveyValue)), List.of(new Tag("source:geometry", "new value")))
        );
    }

    @Test
    void testConflictWithNotBuildingTags() {
        Assertions.assertTrue(isConflict(List.of(new Tag("office", "yes")), List.of(new Tag("office", "company"))));
    }


}
