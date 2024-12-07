package org.openstreetmap.josm.plugins.plbuildings.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainFrame;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;

/**
 * Class responsible for keeping import buildings data (as datasets) from multiple data sources.
 */
public class BuildingsImportData {
    private final Map<String, DataSet> data = new HashMap<>();

    /**
     * Create instance with imported datasets (generally for tests).
     *
     * @param args in order Source1, DataSet1, Source2, DataSet2.... where source is String
     */
    public BuildingsImportData(Object... args) {
        for (int i = 0; i < args.length; i += 2) {
            data.put((String) args[i], (DataSet) args[i + 1]);
        }
    }

    public void add(String source, DataSet dataSet) {
        this.data.put(source, dataSet);
    }

    /**
     * @param source – geometry/tags source
     * @return dataset for given source or new empty dataset if no found any source.
     *     It's never return null.
     */
    public DataSet get(String source) {
        return this.data.getOrDefault(source, new DataSet());
    }

    /**
     *
     * @return true if in each dataset (imported datasource) there is no at least one node lat lon,
     *      which is in frame user bbox
     */
    public boolean isOutOfUserFrameView(Bounds userView) {
        if (userView == null) {
            return false;
        }
        return data.values().stream()
            .filter(dataSet -> !dataSet.isEmpty())
            .anyMatch(
                dataSet -> dataSet.getNodes().stream().noneMatch(node -> userView.contains(node.getCoor()))
            );
    }
}
