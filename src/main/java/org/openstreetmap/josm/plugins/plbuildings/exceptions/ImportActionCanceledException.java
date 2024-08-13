package org.openstreetmap.josm.plugins.plbuildings.exceptions;

import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;

public class ImportActionCanceledException  extends Exception {
    private final ImportStatus status;

    public ImportActionCanceledException(String message, ImportStatus status) {
        super(message);
        this.status = status;
    }

    public ImportStatus getStatus() {
        return status;
    }
}
