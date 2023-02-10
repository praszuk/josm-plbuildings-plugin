package org.openstreetmap.josm.plugins.plbuildings.models;

public class DataSourceProfile {
    private final String dataSourceServerName;
    private String geometry;
    private String tags;
    private String name;

    // FIELD_* strings are used to name fields to (de)serialization to JOSM Settings
    private static final String FIELD_NAME = "name";
    private static final String FIELD_GEOMETRY = "geometry";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_SERVER_NAME = "server_name";

    public DataSourceProfile(String dataSourceServerName, String geometry, String tags, String name) {
        this.dataSourceServerName = dataSourceServerName;
        this.geometry = geometry;
        this.tags = tags;
        this.name = name;
    }

    public String getDataSourceServerName() {
        return dataSourceServerName;
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
