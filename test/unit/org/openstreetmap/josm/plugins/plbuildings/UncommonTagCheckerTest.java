package org.openstreetmap.josm.plugins.plbuildings;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils;

public class UncommonTagCheckerTest {

    @Test
    public void testPrimitiveHasUncommonTag() {
        OsmPrimitive building1 = new Way();
        building1.put("building", "yes");
        building1.put("amenity", "vehicle_inspection");

        TagMap uncommon1 = PostCheckUtils.findUncommonTags(building1);
        Assertions.assertEquals(1, uncommon1.size());
        Assertions.assertEquals("vehicle_inspection", uncommon1.get("amenity"));

        OsmPrimitive building2 = new Way();
        building2.put("building", "hotel");
        building2.put("name", "xyz");

        TagMap uncommon2 = PostCheckUtils.findUncommonTags(building2);
        Assertions.assertEquals(1, uncommon2.size());
        Assertions.assertEquals("hotel", uncommon2.get("building"));

        OsmPrimitive building3 = new Way();
        building3.put("building", "house");
        building3.put("note", "amenity");
        Assertions.assertEquals(0, PostCheckUtils.findUncommonTags(building3).size());

        // this value is not in the gugik2osm buildings_categories_mappings it's just "living building"
        OsmPrimitive building4 = new Way();
        building4.put("building", "semidetached_house");
        Assertions.assertEquals(0, PostCheckUtils.findUncommonTags(building4).size());

        // Check multiple uncommon values
        OsmPrimitive building5 = new Way();
        building5.put("building", "church");
        building5.put("historic", "yes");
        building5.put("amenity", "place_of_worship");

        TagMap uncommon5 = PostCheckUtils.findUncommonTags(building5);
        Assertions.assertEquals(3, uncommon5.size());
        Assertions.assertEquals("church", uncommon5.get("building"));
        Assertions.assertEquals("yes", uncommon5.get("historic"));
        Assertions.assertEquals("place_of_worship", uncommon5.get("amenity"));
    }
}
