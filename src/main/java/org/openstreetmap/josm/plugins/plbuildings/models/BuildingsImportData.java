package org.openstreetmap.josm.plugins.plbuildings.models;

import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.josm.data.osm.DataSet;

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

}
