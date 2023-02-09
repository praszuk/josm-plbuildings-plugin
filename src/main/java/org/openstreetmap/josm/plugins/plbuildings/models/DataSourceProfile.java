package org.openstreetmap.josm.plugins.plbuildings.models;

public class DataSourceProfile {
    private final DataSourceServer dataSourceServer;
    private String geometry;
    private String tags;
    private String name;

    public DataSourceProfile(DataSourceServer dataSourceServer, String geometry, String tags, String name) {
        this.dataSourceServer = dataSourceServer;
        this.geometry = geometry;
        this.tags = tags;
        this.name = name;
    }

    public DataSourceServer getDataSourceServer() {
        return dataSourceServer;
    }

    public String getGeometry() {
        return geometry;
    }

    public String getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setName(String name) {
        this.name = name;
    }
}
