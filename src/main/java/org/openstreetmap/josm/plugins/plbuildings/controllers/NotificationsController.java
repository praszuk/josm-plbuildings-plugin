package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.plugins.plbuildings.gui.NotificationPopup.showNotification;

import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.plugins.plbuildings.models.NotifiableImportStatuses;

public class NotificationsController {

    public void handleStatus(ImportStatus status, String reason) {
        if (!NotifiableImportStatuses.getInstance().isNotifiable(status)) {
            return;
        }
        showNotification(status + ": " + reason);
    }
}
