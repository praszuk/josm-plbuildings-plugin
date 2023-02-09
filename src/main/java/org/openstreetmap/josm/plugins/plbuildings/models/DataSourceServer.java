package org.openstreetmap.josm.plugins.plbuildings.models;

/**
 * responsible for handling servers where plugin will connect to download buildings data
 */
public class DataSourceServer {
    private final String name;
    private final String url;

    public DataSourceServer(String name, String url){
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    // TODO setter need some logic with related DataSourceProfile
}
