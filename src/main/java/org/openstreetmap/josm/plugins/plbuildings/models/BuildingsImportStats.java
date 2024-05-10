package org.openstreetmap.josm.plugins.plbuildings.models;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsSettings;
import org.openstreetmap.josm.tools.Logging;


/**
 * BuildingsImportStats keeps simple importing statics.
 * and manages serialization/deserialization between BuildingSettings
 */
public class BuildingsImportStats {
    private int importNewBuildingCounter;
    private int importWithReplaceCounter;

    private int importWithTagsUpdateCounter;

    private int totalImportActionCounter;

    // FIELD_* strings are used to name fields to (de)serialization to JOSM Settings
    private static final String FIELD_IMPORT_NEW_BUILDING_COUNTER = "importNewBuilding";
    private static final String FIELD_IMPORT_WITH_REPLACE_COUNTER = "importWithReplace";
    private static final String FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER = "importWithTagsUpdate";
    private static final String FIELD_TOTAL_IMPORT_ACTION = "totalImportAction";

    private static BuildingsImportStats instance;

    public static BuildingsImportStats getInstance() {
        if (instance == null) {
            instance = new BuildingsImportStats();
        }
        return instance;
    }

    public int getImportNewBuildingCounter() {
        return this.importNewBuildingCounter;
    }

    public int getImportWithReplaceCounter() {
        return this.importWithReplaceCounter;
    }

    public int getImportWithTagsUpdateCounter() {
        return importWithTagsUpdateCounter;
    }

    public int getTotalImportActionCounter() {
        return totalImportActionCounter;
    }

    public void addImportNewBuildingCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.importNewBuildingCounter += value;
        save();
    }

    public void addImportWithReplaceCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.importWithReplaceCounter += value;
        save();
    }

    public void addImportWithTagsUpdateCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.importWithTagsUpdateCounter += value;
        save();
    }

    public void addTotalImportActionCounter(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }
        this.totalImportActionCounter += value;
        save();
    }

    public HashMap<String, Object> getStats() {
        HashMap<String, Object> stats = new HashMap<>();
        stats.put(FIELD_IMPORT_NEW_BUILDING_COUNTER, this.importNewBuildingCounter);
        stats.put(FIELD_IMPORT_WITH_REPLACE_COUNTER, this.importWithReplaceCounter);
        stats.put(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, this.importWithTagsUpdateCounter);
        stats.put(FIELD_TOTAL_IMPORT_ACTION, this.totalImportActionCounter);

        return stats;
    }

    @Override
    public String toString() {
        return getStats().toString();
    }

    private BuildingsImportStats() {
        load();
    }

    /**
     * Saves data to JOSM settings.
     */
    private void save() {
        Logging.debug("Saving import stats: {0}", this.toString());
        JsonObject jsonStats = Json.createObjectBuilder()
            .add(FIELD_IMPORT_NEW_BUILDING_COUNTER, this.importNewBuildingCounter)
            .add(FIELD_IMPORT_WITH_REPLACE_COUNTER, this.importWithReplaceCounter)
            .add(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, this.importWithTagsUpdateCounter)
            .add(FIELD_TOTAL_IMPORT_ACTION, this.totalImportActionCounter)
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
        JsonReader jsonReader = Json.createReader(new StringReader(decodedJsonStats));
        JsonObject jsonStats = jsonReader.readObject();
        jsonReader.close();

        this.importNewBuildingCounter = jsonStats.getInt(FIELD_IMPORT_NEW_BUILDING_COUNTER, 0);
        this.importWithReplaceCounter = jsonStats.getInt(FIELD_IMPORT_WITH_REPLACE_COUNTER, 0);
        this.importWithTagsUpdateCounter = jsonStats.getInt(FIELD_IMPORT_WITH_TAGS_UPDATE_COUNTER, 0);
        this.totalImportActionCounter = jsonStats.getInt(FIELD_TOTAL_IMPORT_ACTION, 0);
        Logging.debug("Loaded import stats: {0}", this.toString());
    }
}
