package org.openstreetmap.josm.plugins.plbuildings.gui;

import javax.swing.JOptionPane;
import org.openstreetmap.josm.gui.Notification;

public class NotificationPopup {
    public static void showNotification(String message) {
        Notification notification = new Notification(message);
        notification.setDuration(Notification.TIME_SHORT);
        notification.setIcon(JOptionPane.WARNING_MESSAGE);
        notification.show();
    }
}
