package org.openstreetmap.josm.plugins.plbuildings.controllers;

import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;

import javax.swing.*;

import java.util.List;

import static org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus.*;

public class NotificationsController {

    public static final List<ImportStatus> notifiableStatuses = List.of(NO_DATA,NO_UPDATE, CONNECTION_ERROR, IMPORT_ERROR);

    public void handleStatus(ImportStatus status, String reason){
        if (!notifiableStatuses.contains(status)){
            return;
        }
        // TODO add if status enabled in settings

        Notification notification = new Notification(status + ": " + reason);
        notification.setDuration(Notification.TIME_SHORT);
        notification.setIcon(JOptionPane.WARNING_MESSAGE);
        notification.show();
    }
}
