package org.openstreetmap.josm.plugins.plbuildings.models;

import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.jsonFactory;
import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class DataSourceProfile {
    private final String dataSourceServerName;
    private String geometry;
    private String tags;
    private String name;
    private boolean visible;

    // FIELD_* strings are used to name fields to (de)serialization to JOSM Settings
    private static final String FIELD_NAME = "name";
    private static final String FIELD_GEOMETRY = "geometry";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_SERVER_NAME = "server_name";
    private static final String FIELD_VISIBLE = "visible";

    public DataSourceProfile(String dataSourceServerName, String geometry, String tags, String name,
                             Boolean visible) {
        this.dataSourceServerName = dataSourceServerName;
        this.geometry = geometry;
        this.tags = tags;
        this.name = name;
        this.visible = visible;
    }

    public DataSourceProfile(String dataSourceServerName, String geometry, String tags, String name) {
        this(dataSourceServerName, geometry, tags, name, true);
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

    public Boolean isVisible() {
        return visible;
    }

    private void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    private void setTags(String tags) {
        this.tags = tags;
    }

    private void setName(String name) {
        this.name = name;
    }

    protected void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void updateProfile(DataSourceProfile newProfile) {
        setName(newProfile.getName());
        setTags(newProfile.getTags());
        setGeometry(newProfile.getGeometry());
        setVisible(newProfile.isVisible());
    }

    public static JsonArray toJson(Collection<DataSourceProfile> collection) {
        JsonArrayBuilder builder = provider.createArrayBuilder();
        collection.forEach(obj -> builder.add(
            jsonFactory.createObjectBuilder()
                .add(FIELD_NAME, obj.name)
                .add(FIELD_GEOMETRY, obj.geometry)
                .add(FIELD_TAGS, obj.tags)
                .add(FIELD_SERVER_NAME, obj.dataSourceServerName)
                .add(FIELD_VISIBLE, obj.visible)
                .build()
        ));
        return builder.build();
    }

    public static Collection<DataSourceProfile> fromStringJson(String jsonString) {
        JsonReader jsonReader = provider.createReader(new StringReader(jsonString));
        JsonArray jsonArray = jsonReader.readArray();

        Collection<DataSourceProfile> collection = new ArrayList<>();
        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = jsonValue.asJsonObject();
            collection.add(
                new DataSourceProfile(
                    jsonObject.getString(FIELD_SERVER_NAME),
                    jsonObject.getString(FIELD_GEOMETRY),
                    jsonObject.getString(FIELD_TAGS),
                    jsonObject.getString(FIELD_NAME),
                    jsonObject.getBoolean(FIELD_VISIBLE)
                )
            );
        }

        jsonReader.close();

        return collection;
    }

    /**
     * @return true if data source for tags and geometry is the same otherwise false
     */
    public boolean isOneDataSource() {
        return this.tags.equals(this.geometry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSourceProfile that = (DataSourceProfile) o;
        return visible == that.visible && Objects.equals(dataSourceServerName, that.dataSourceServerName)
            && Objects.equals(geometry, that.geometry) && Objects.equals(tags, that.tags)
            && Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return "DataSourceProfile{"
            + "dataSourceServerName='" + dataSourceServerName + '\''
            + ", geometry='" + geometry + '\''
            + ", tags='" + tags + '\''
            + ", name='" + name + '\''
            + ", visible=" + visible
            + '}';
    }
}
