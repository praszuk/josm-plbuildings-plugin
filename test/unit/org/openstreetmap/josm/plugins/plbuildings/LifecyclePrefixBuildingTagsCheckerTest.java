package org.openstreetmap.josm.plugins.plbuildings;


import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils;

public class LifecyclePrefixBuildingTagsCheckerTest {

    @ParameterizedTest
    @CsvSource({
        "building=house|building:levels=2,",
        "building=house|proposed:building:levels=2,proposed:building:levels=2",
        "construction:building=house|building:levels=2,construction:building=house",
        "building=house|proposed:building=yes,proposed:building=yes",
        "building=yes|proposed:building=yes|construction:building=yes,proposed:building=yes|construction:building=yes",
        "building=house|construction:roof:levels=1,construction:roof:levels=1",
        "building=house|proposed:name=test,",
    })
    public void testPrimitiveLifecycleBuildingTag(String rawPrimitveTags, String rawExpectedTags) {
        OsmPrimitive building = new Way();
        for (String keyValue : rawPrimitveTags.split("\\|")) {
            String[] keyValueSplitted = keyValue.split("=");
            building.put(keyValueSplitted[0], keyValueSplitted[1]);
        }

        List<Tag> expectedTags = new ArrayList<>();
        if (rawExpectedTags != null) {
            for (String keyValue : rawExpectedTags.split("\\|")) {
                String[] keyValueSplitted = keyValue.split("=");
                expectedTags.add(new Tag(keyValueSplitted[0], keyValueSplitted[1]));
            }
        }

        TagMap lifecyclePrefixBuildingTags = PostCheckUtils.findLifecyclePrefixBuildingTags(building);
        Assertions.assertTrue(expectedTags.containsAll(lifecyclePrefixBuildingTags.getTags()));
        Assertions.assertEquals(expectedTags.size(), lifecyclePrefixBuildingTags.size());
    }
}
