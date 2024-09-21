package org.openstreetmap.josm.plugins.plbuildings.enums;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Represents current state for the building import (BuildingsImportAction).
 */
public enum ImportStatus {
    /** Waiting for the import action */
    IDLE(tr("Idle")),
    /** Connecting/downloading data from import server */
    DOWNLOADING(tr("Downloading")),
    /** Downloaded/import building succesfully */
    DONE(tr("Done")),
    /** Empty response/dataset – without buildings */
    NO_DATA(tr("No data")),
    /** Import canceled by same/duplicated data */
    NO_UPDATE(tr("No update")),
    /** Import canceled by user input e.g. reject conflict tags dialog */
    CANCELED(tr("Canceled")),
    /** Server unreachable or timeout, it could alsoo just 400/500 response */
    CONNECTION_ERROR(tr("Connection error")),
    /** Caused by import action (plugin side – not server/connection) */
    IMPORT_ERROR(tr("Import error")),
    /** User input action required e.g. resolve conflict with tags */
    ACTION_REQUIRED(tr("Action required"));

    private final String text;

    ImportStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
