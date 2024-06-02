package org.openstreetmap.josm.plugins.plbuildings.models;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * responsible for handling servers where plugin will connect to download buildings data.
 */
public class DataSourceServer {
    private final String name;
    private final String url;

    // FIELD_* strings are used to name fields to (de)serialization to JOSM Settings
    private static final String FIELD_NAME = "name";
    private static final String FIELD_URL = "url";


    public DataSourceServer(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public static JsonArray toJson(Collection<DataSourceServer> collection) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        collection.forEach(obj -> builder.add(
            Json.createObjectBuilder()
                .add(FIELD_NAME, obj.name)
                .add(FIELD_URL, obj.url)
                .build()
        ));
        return builder.build();
    }

    public static Collection<DataSourceServer> fromStringJson(String jsonString) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonArray jsonArray = jsonReader.readArray();

        Collection<DataSourceServer> collection = new ArrayList<>();
        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = jsonValue.asJsonObject();
            collection.add(
                new DataSourceServer(
                    jsonObject.getString(FIELD_NAME),
                    jsonObject.getString(FIELD_URL)
                )
            );
        }
        jsonReader.close();

        return collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSourceServer that = (DataSourceServer) o;
        return Objects.equals(name, that.name) && Objects.equals(url, that.url);
    }

    @Override
    public String toString() {
        return "DataSourceServer{"
            + "name='" + name + '\''
            + ", url='" + url + '\''
            + '}';
    }
}
