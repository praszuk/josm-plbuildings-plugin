package org.openstreetmap.josm.plugins.plbuildings.models;

import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.jsonFactory;
import static org.openstreetmap.josm.plugins.plbuildings.utils.JsonUtil.provider;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.tools.Logging;


/**
 * BuildingsImportStats keeps simple importing statics.
 * and manages serialization/deserialization between BuildingSettings
 */
public class BuildingsImportStats {
    private static final BuildingsImportStats instance = new BuildingsImportStats();


    private int importNewBuildingCounter;
    private int importWithReplaceCounter;

    private int importWithTagsUpdateCounter;
    private int importWithGeometryUpdateCounter;

    private int totalImportActionCounter;

    // FIELD_* strings are used to name fields to (de)serialization to JOSM Settings
    private static final String FIELD_IMPORT_NEW_BUILDING_COUNTER = "importNewBuilding";
    private static final String FIELD_IMPORT_WITH_REPLACE_COUNTER = "importWithReplace";
    private static final String FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER = "importWithTagsUpdate";
    private static final String FIELD_IMPORT_WITH_GEOMETRY_UPDATE_COUNTER = "importWithGeometryUpdate";
    private static final String FIELD_TOTAL_IMPORT_ACTION = "totalImportAction";

    private BuildingsImportStats() {
        BuildingsSettings.IMPORT_STATS.addListener(valueChangeEvent -> load());
        load();
    }

    public static BuildingsImportStats getInstance() {
        return instance;
    }

    public int getImportNewBuildingCounter() {
        return importNewBuildingCounter;
    }

    public int getImportWithReplaceCounter() {
        return importWithReplaceCounter;
    }

    public int getImportWithTagsUpdateCounter() {
        return importWithTagsUpdateCounter;
    }

    public int getImportWithGeometryUpdateCounter() {
        return importWithGeometryUpdateCounter;
    }

    public int getTotalImportActionCounter() {
        return totalImportActionCounter;
    }

    public void addImportNewBuildingCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        importNewBuildingCounter += value;
    }

    public void addImportWithReplaceCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        importWithReplaceCounter += value;
    }

    public void addImportWithGeometryUpdateCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        importWithGeometryUpdateCounter += value;
    }

    public void addImportWithTagsUpdateCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        importWithTagsUpdateCounter += value;
    }

    public void addTotalImportActionCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        totalImportActionCounter += value;
    }

    public HashMap<String, Object> getStats() {
        HashMap<String, Object> stats = new HashMap<>();
        stats.put(FIELD_IMPORT_NEW_BUILDING_COUNTER, importNewBuildingCounter);
        stats.put(FIELD_IMPORT_WITH_REPLACE_COUNTER, importWithReplaceCounter);
        stats.put(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, importWithTagsUpdateCounter);
        stats.put(FIELD_IMPORT_WITH_GEOMETRY_UPDATE_COUNTER, importWithGeometryUpdateCounter);
        stats.put(FIELD_TOTAL_IMPORT_ACTION, totalImportActionCounter);

        return stats;
    }

    @Override
    public String toString() {
        return getStats().toString();
    }


    /**
     * Saves data to JOSM settings.
     */
    public void save() {
        Logging.debug("Saving import stats: {0}", toString());
        JsonObject jsonStats = jsonFactory.createObjectBuilder()
            .add(FIELD_IMPORT_NEW_BUILDING_COUNTER, importNewBuildingCounter)
            .add(FIELD_IMPORT_WITH_REPLACE_COUNTER, importWithReplaceCounter)
            .add(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, importWithTagsUpdateCounter)
            .add(FIELD_IMPORT_WITH_GEOMETRY_UPDATE_COUNTER, importWithGeometryUpdateCounter)
            .add(FIELD_TOTAL_IMPORT_ACTION, totalImportActionCounter)
            .build();

        String encodedB64Stats = Base64.getEncoder().encodeToString(
            jsonStats.toString().getBytes(StandardCharsets.UTF_8)
        );
        BuildingsSettings.IMPORT_STATS.put(encodedB64Stats);
    }

    /**
     * Loads data from JOSM settings.
     * decode from base64 to json string and then map to fields
     */
    private void load() {
        String encodedB64Stats = BuildingsSettings.IMPORT_STATS.get();
        String decodedJsonStats = new String(Base64.getDecoder().decode(encodedB64Stats));
        JsonReader jsonReader = provider.createReader(new StringReader(decodedJsonStats));
        JsonObject jsonStats = jsonReader.readObject();
        jsonReader.close();

        importNewBuildingCounter = jsonStats.getInt(FIELD_IMPORT_NEW_BUILDING_COUNTER, 0);
        importWithReplaceCounter = jsonStats.getInt(FIELD_IMPORT_WITH_REPLACE_COUNTER, 0);
        importWithTagsUpdateCounter = jsonStats.getInt(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, 0);
        importWithGeometryUpdateCounter = jsonStats.getInt(FIELD_IMPORT_WITH_GEOMETRY_UPDATE_COUNTER, 0);
        totalImportActionCounter = jsonStats.getInt(FIELD_TOTAL_IMPORT_ACTION, 0);
        Logging.debug("Loaded import stats: {0}", toString());
    }
}
