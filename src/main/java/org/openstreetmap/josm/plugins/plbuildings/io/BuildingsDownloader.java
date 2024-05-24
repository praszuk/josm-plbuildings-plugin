package org.openstreetmap.josm.plugins.plbuildings.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmJsonReader;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsImportManager;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.models.BuildingsImportData;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceConfig;
import org.openstreetmap.josm.plugins.plbuildings.models.DataSourceProfile;
import org.openstreetmap.josm.tools.Http1Client;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

public class BuildingsDownloader {
    /**
     * Download buildings from PLBuildings Server API and parse it as DataSets. It also updates the status on any error.
     *
     * @param manager ImportManager which contains DataSourceConfig for current download.
     * @return BuildingsImportData with downloaded data or empty datasets or null if IO/parse error
     */
    public static BuildingsImportData getBuildingsImportData(BuildingsImportManager manager) {
        String url = buildUrl(
            DataSourceConfig.getInstance(), manager.getCursorLatLon(), manager.getDataSourceProfile()
        );
        try {
            InputStream responseStream = download(url);
            return parseData(responseStream);
        } catch (IOException ioException) {
            Logging.warn("Connection error with getting building data: {0}", ioException.getMessage());
            manager.setStatus(ImportStatus.CONNECTION_ERROR, ioException.getMessage());
        } catch (Exception parseAndOtherExceptions) {
            Logging.error("Parsing error – dataset from the server: {0}", parseAndOtherExceptions.getMessage());
            manager.setStatus(ImportStatus.IMPORT_ERROR, parseAndOtherExceptions.getMessage());
        }
        return null;
    }

    /**
     * @param latLon location of searching building (EPSG 4326)
     */
    static String buildUrl(DataSourceConfig dataSourceConfig, LatLon latLon, DataSourceProfile currentProfile) {
        String serverBaseApiUrl = dataSourceConfig.getServerByName(currentProfile.getDataSourceServerName()).getUrl();

        String dataSourceQueryParam = currentProfile.getGeometry();
        if (!currentProfile.getGeometry().equals(currentProfile.getTags())) {
            dataSourceQueryParam += "," + currentProfile.getTags();
        }
        dataSourceQueryParam = dataSourceQueryParam.toLowerCase();

        return String.format(
            "%s%s?lat=%f&lon=%f&data_sources=%s",
            serverBaseApiUrl, DownloaderConstants.API_BUILDING_AT, latLon.lat(), latLon.lon(), dataSourceQueryParam
        );
    }

    /**
     * @param serverUrl full url to make request (with params etc.)
     */
    static InputStream download(String serverUrl) throws IOException {
        Logging.info("Getting buildings data from: {0}", serverUrl);

        URL url = new URL(serverUrl);
        HttpClient httpClient = new Http1Client(url, "GET");
        httpClient.setHeader("User-Agent", DownloaderConstants.USER_AGENT);
        httpClient.connect();
        HttpClient.Response response = httpClient.getResponse();

        return response.getContent();
    }

    static BuildingsImportData parseData(InputStream responseStream) throws IllegalDataException {
        BuildingsImportData dataSourceBuildingsData = new BuildingsImportData();

        JsonReader reader = Json.createReader(responseStream);
        JsonArray dataSourcesObjectsData = reader.readArray();
        for (JsonValue jsonValue : dataSourcesObjectsData) {
            JsonObject dataSourceObject = jsonValue.asJsonObject();

            String source = dataSourceObject.getString("source");
            String format = dataSourceObject.getString("format");
            if (dataSourceObject.isNull("data")) {
                dataSourceBuildingsData.add(source, new DataSet());
                continue;
            }
            String data = dataSourceObject.getJsonObject("data").toString();

            if (format.equals("geojson")) {
                dataSourceBuildingsData.add(
                    source, GeoJSONReader.parseDataSet(new ByteArrayInputStream(data.getBytes()), null)
                );
            } else if (format.equals("osmjson")) {
                dataSourceBuildingsData.add(
                    source, OsmJsonReader.parseDataSet(new ByteArrayInputStream(data.getBytes()), null)
                );
            } else {
                Logging.warn("Error at parsing buildings data: Unsupported data format! ({0))", format);
            }
        }
        reader.close();
        return dataSourceBuildingsData;
    }
}
