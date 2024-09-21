package org.openstreetmap.josm.plugins.plbuildings.utils;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.spi.JsonProvider;

public class JsonUtil {
    /**
     * Check: https://github.com/jakartaee/jsonp-api/issues/154 – Jakarta is slow
     */
    public static final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(null);
    public static final JsonProvider provider = JsonProvider.provider();
}
