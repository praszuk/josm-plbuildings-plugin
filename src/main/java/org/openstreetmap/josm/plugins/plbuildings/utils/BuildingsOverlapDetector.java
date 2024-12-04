package org.openstreetmap.josm.plugins.plbuildings.utils;

import static org.openstreetmap.josm.tools.Geometry.nodeInsidePolygon;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.tools.Logging;

public class BuildingsOverlapDetector {

    /**
     * Detect buildings overlapping using "dotting" algorithm
     * It works like this:
     * - Getting BBOX from 2 given buildings
     * - Iterating thought points in BBOX (like a grid) with constant distance frequency:
     * For each point check if is in both buildings and then increase 1 from 3 counters (1st/2nd/both)
     * It uses OVERLAP_DETECT_FREQ_DEGREE_STEP setting to set frequency between points.
     * Smaller will give better accuracy, but it will be slower.
     * There is no difference with order of b1 and 2
     *
     * @param building1 closed building not multipolygon
     * @param building2 closed building not multipolygon
     * @return percentage of buildings overlapping:
     *     <ul><li>if no overlapping then 0.00, if crossing <b>smaller</b> intersection will be return</li>
     *     <li>if 1st is completely inside in 2nd or 2nd in 1st then it will return percentage of ratio that
     *     inner building takes for outside building </li>
     *     <li>if something went wrong it will return -1.</li></ul>
     */
    public static double detect(OsmPrimitive building1, OsmPrimitive building2) {
        final double freqDegreeStep = BuildingsSettings.OVERLAP_DETECT_FREQ_DEGREE_STEP.get();
        if (building1.isMultipolygon() || building2.isMultipolygon()) {
            throw new IllegalArgumentException("Buildings cannot be a multipolygon!");
        }
        Way b1 = (Way) building1;
        Way b2 = (Way) building2;
        if (!b1.isClosed() || !b2.isClosed()) {
            throw new IllegalArgumentException("Incorrect building – it must be closed line!");
        }

        BBox bbox = new BBox(b1);
        BBox bbox2 = new BBox(b2);

        if (!bbox.intersects(bbox2) && !bbox.bounds(bbox2) && !bbox2.bounds(bbox)) {
            return 0.0;
        }

        bbox.add(new BBox(b2));
        double minLat = bbox.getMinLat();
        double maxLat = bbox.getMaxLat();
        double minLon = bbox.getMinLon();
        double maxLon = bbox.getMaxLon();

        int latPointCount = (int) ((maxLat - minLat) / freqDegreeStep);
        int lonPointCount = (int) ((maxLon - minLon) / freqDegreeStep);

        AtomicInteger bothCounter = new AtomicInteger();
        AtomicInteger b1Counter = new AtomicInteger(); // only nodes in the first building without in both
        AtomicInteger b2Counter = new AtomicInteger(); // similar as up, but second building

        ArrayList<Node> nodesToCheck = new ArrayList<>();
        DoubleStream.iterate(minLat, lat -> lat + freqDegreeStep).limit(latPointCount + 1).forEach(
            lat -> DoubleStream.iterate(minLon, lon -> lon + freqDegreeStep).limit(lonPointCount + 1)
                .forEach(lon -> nodesToCheck.add(new Node(new LatLon(lat, lon)))));

        nodesToCheck.forEach(node -> {
            boolean isB1 = nodeInsidePolygon(node, b1.getNodes());
            boolean isB2 = nodeInsidePolygon(node, b2.getNodes());
            if (isB1 && isB2) {
                bothCounter.getAndIncrement();
            } else if (isB1) {
                b1Counter.getAndIncrement();
            } else if (isB2) {
                b2Counter.getAndIncrement();
            }
        });

        // 5 types of intersection

        // Crossing
        if (b1Counter.get() != 0 && b2Counter.get() != 0) {
            double b1Overlapping = bothCounter.get() * 100. / (b1Counter.get() + bothCounter.get());
            double b2Overlapping = bothCounter.get() * 100. / (b2Counter.get() + bothCounter.get());
            return Math.min(b1Overlapping, b2Overlapping);
        }
        // Crossing with same coordinates (actually duplicated object)
        else if (b1Counter.get() == 0 && b2Counter.get() == 0 & bothCounter.get() != 0) {
            return 100.0;
        }
        // First building is completely inside second
        else if (b1Counter.get() == 0) {
            return 100.0 - (b2Counter.get() * 100.0 / (b2Counter.get() + bothCounter.get()));
        }
        // Second building is completely inside first
        else if (b2Counter.get() == 0) {
            return 100.0 - (b1Counter.get() * 100.0 / (b1Counter.get() + bothCounter.get()));
        }

        Logging.error(String.format(
            "Something went wrong with detecting intersection of building: b1: %d, b2: %d, both: %d ",
            b1Counter.get(),
            b2Counter.get(),
            bothCounter.get()
        ));
        return -1.0;
    }
}
