package org.openstreetmap.josm.plugins.plbuildings;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.utils.PostCheckUtils;

import static org.junit.Assert.*;

public class UncommonTagCheckerTest {

    @Test
    public void testPrimitiveHasUncommonTag(){
        OsmPrimitive building1 = new Way();
        building1.put("building", "yes");
        building1.put("amenity", "vehicle_inspection");

        TagMap uncommon1 = PostCheckUtils.findUncommonTags(building1);
        assertEquals(uncommon1.size(), 1);
        assertEquals(uncommon1.get("amenity"), "vehicle_inspection");

        OsmPrimitive building2 = new Way();
        building2.put("building", "hotel");
        building2.put("name", "xyz");

        TagMap uncommon2 = PostCheckUtils.findUncommonTags(building2);
        assertEquals(uncommon2.size(), 1);
        assertEquals(uncommon2.get("building"),"hotel");

        OsmPrimitive building3 = new Way();
        building3.put("building", "house");
        building3.put("note", "amenity");
        assertEquals(PostCheckUtils.findUncommonTags(building3).size(), 0);

        // this value is not in the gugik2osm buildings_categories_mappings it's just "living building"
        OsmPrimitive building4 = new Way();
        building4.put("building", "semidetached_house");
        assertEquals(PostCheckUtils.findUncommonTags(building4).size(), 0);

        // Check multiple uncommon values
        OsmPrimitive building5 = new Way();
        building5.put("building", "church");
        building5.put("historic", "yes");
        building5.put("amenity", "place_of_worship");

        TagMap uncommon5 = PostCheckUtils.findUncommonTags(building5);
        assertEquals(uncommon5.size(), 3);
        assertEquals(uncommon5.get("building"), "church");
        assertEquals(uncommon5.get("historic"), "yes");
        assertEquals(uncommon5.get("amenity"), "place_of_worship");
    }
}
