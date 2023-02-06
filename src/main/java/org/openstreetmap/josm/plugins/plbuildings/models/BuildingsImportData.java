package org.openstreetmap.josm.plugins.plbuildings.models;

import org.openstreetmap.josm.data.osm.DataSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for keeping import buildings data (as datasets) from multiple data sources.
 */
public class BuildingsImportData {
    private final Map<String, DataSet> data = new HashMap<>();


    public void add(String source, DataSet dataSet){
        this.data.put(source, dataSet);
    }

    public DataSet get(String source){
        return this.data.getOrDefault(source, null);
    }

}
