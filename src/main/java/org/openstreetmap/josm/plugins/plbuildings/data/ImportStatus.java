package org.openstreetmap.josm.plugins.plbuildings.data;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Represents current state for the building import (BuildingsImportAction).
 */
public enum ImportStatus {
    IDLE(tr("Idle")),                           // waiting for the import action
    DOWNLOADING(tr("Downloading")),             // connecting/downloading data from import server
    DONE(tr("Done")),                           // downloaded successfully building
    NO_DATA(tr("No data")),                     // empty response/without buildings
    NO_UPDATE(tr("No update")),                 // canceled import by same/duplicated data
    CANCELED(tr("Canceled")),                   // canceled by user input
    CONNECTION_ERROR(tr("Connection error")),   // can be e.g. timeout or server 400/500 response
    IMPORT_ERROR(tr("Import error")),           // caused by import action (plugin side) not serve/connection
    ACTION_REQUIRED(tr("Action required"));     // user action required e.g. resolve conflict with tags

    private final String text;
    ImportStatus(final String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
