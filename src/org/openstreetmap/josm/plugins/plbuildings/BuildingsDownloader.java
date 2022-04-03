package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BuildingsDownloader {
    static final String BUDYNKI_URL = "https://budynki.openstreetmap.org.pl/josm_data";

    /**
     * Download buildings from budynki.openstreetmap.org.pl site and parse it as DataSet
     * @param geometry is result from createGeometryFeature function
     * @return DataSet with "raw building" from geojson or null
     */
    public static DataSet downloadBuildings(JsonObject geometry){
        String encodedGeometry;
        try {
            encodedGeometry = URLEncoder.encode(geometry.toString(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder urlBuilder = new StringBuilder(BUDYNKI_URL);
        urlBuilder.append("?");
        urlBuilder.append("filter_by=geojson_geometry");
        urlBuilder.append("&");
        urlBuilder.append("layers=buildings"); // layers=buildings – all, layers=buildings_to_import – missing
        urlBuilder.append("&");
        urlBuilder.append("geom=");
        urlBuilder.append(encodedGeometry);

        try {
            URL url = new URL(urlBuilder.toString());
            HttpClient httpClient = new Http1Client(url, "GET");
            httpClient.connect();
            HttpClient.Response response = httpClient.getResponse();

            return OsmReader.parseDataSet(response.getContent(), null);
        } catch (IOException | IllegalDataException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates JSON geometry which is required as query parameter for budynki.openstreetmap.org.pl site
     * to pass location of Point (using very small Polygon as hacky solution)
     * @return JSONObject: {"type": "Polygon", "coordinates": [lon1, lat1], [lon2, lat2], [lon3, lat3]} where
     * the first one is from point parameter, 2nd and 3rd are moved by small offset (about few meters).
     */
    public static JsonObject createGeometryFeature(LatLon point){
        final double diff = 0.000001;
        JsonArray pos1 = Json.createArrayBuilder().add(point.lon()).add(point.lat()).build();
        JsonArray pos2 = Json.createArrayBuilder().add(point.lon() + diff).add(point.lat()).build();
        JsonArray pos3 = Json.createArrayBuilder().add(point.lon()).add(point.lat() + diff).build();
        return Json.createObjectBuilder()
                .add("type", "Polygon")
                .add("coordinates", Json.createArrayBuilder()
                        .add(Json.createArrayBuilder()
                                .add(pos1)
                                .add(pos2)
                                .add(pos3)
                                .add(pos1)
                                .build()
                        )
                        .build()
                )
                .build();
    }
}
