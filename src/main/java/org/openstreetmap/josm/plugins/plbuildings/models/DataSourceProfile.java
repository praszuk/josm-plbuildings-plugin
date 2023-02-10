package org.openstreetmap.josm.plugins.plbuildings.models;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

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

    public static JsonArray toJson(Collection<DataSourceProfile> collection){
        JsonArrayBuilder builder = Json.createArrayBuilder();
        collection.forEach(obj -> builder.add(
                Json.createObjectBuilder()
                    .add(FIELD_NAME, obj.name)
                    .add(FIELD_GEOMETRY, obj.geometry)
                    .add(FIELD_TAGS, obj.tags)
                    .add(FIELD_SERVER_NAME, obj.dataSourceServerName)
                    .build()
        ));
        return builder.build();
    }

    public static Collection<DataSourceProfile> fromStringJson(String jsonString){
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonArray jsonArray = jsonReader.readArray();

        Collection <DataSourceProfile> collection = new ArrayList<>();
        for (JsonValue jsonValue : jsonArray){
            JsonObject jsonObject = jsonValue.asJsonObject();
            collection.add(
                new DataSourceProfile(
                    jsonObject.getString(FIELD_SERVER_NAME),
                    jsonObject.getString(FIELD_GEOMETRY),
                    jsonObject.getString(FIELD_TAGS),
                    jsonObject.getString(FIELD_NAME)
                )
            );
        }

        jsonReader.close();

        return collection;
    }
}
