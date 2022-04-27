package org.openstreetmap.josm.plugins.plbuildings;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;

import java.io.IOException;
import java.net.URL;

public class BuildingsDownloader {
    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     * Use default search_distance parameter.
     * @param latLon location of searching building (EPSG 4386)
     * @param dataSource dataSource of buildings. Currently, only "bdot" is available
     * @return DataSet with "raw building" from .osm response or null
     */
    public static DataSet downloadBuildings(LatLon latLon, String dataSource){
        return downloadBuildings(latLon, dataSource, BuildingsSettings.SEARCH_DISTANCE.get());
    }

    /**
     * Download buildings from PLBuildings Server API and parse it as DataSet
     * @param latLon location of searching building (EPSG 4386)
     * @param dataSource dataSource of buildings. Currently, only "bdot" is available
     * @param searchDistance distance in meters to find the nearest building from latLon
     * @return DataSet with "raw building" from .osm response or null
     */
    public static DataSet downloadBuildings(LatLon latLon, String dataSource, Double searchDistance){

        StringBuilder urlBuilder = new StringBuilder(BuildingsSettings.SERVER_URL.get());

        urlBuilder.append("?");
        urlBuilder.append("lat=");
        urlBuilder.append(latLon.lat());

        urlBuilder.append("&");
        urlBuilder.append("lon=");
        urlBuilder.append(latLon.lon());

        urlBuilder.append("&");
        urlBuilder.append("data_source=");
        urlBuilder.append(dataSource);

        urlBuilder.append("&");
        urlBuilder.append("search_distance=");
        urlBuilder.append(searchDistance);

        System.out.println(urlBuilder);

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
}
