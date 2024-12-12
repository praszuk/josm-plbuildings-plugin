package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openstreetmap.josm.plugins.plbuildings.enums.Notification;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsNotificationsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.NotificationConfig;

public class SettingsNotificationsController implements SettingsTabController {
    private final NotificationConfig notificationConfigModel;
    private final SettingsNotificationsPanel settingsNotificationsPanelView;

    public SettingsNotificationsController(
        NotificationConfig notificationConfigModel, SettingsNotificationsPanel settingsNotificationsPanelView
    ) {
        this.notificationConfigModel = notificationConfigModel;
        this.settingsNotificationsPanelView = settingsNotificationsPanelView;

        this.notificationConfigModel.addPropertyChangeListener(
            NotificationConfig.NOTIFICATION_STATE_CHANGED, propertyChangeEvent -> updateCheckboxes()
        );

        initCheckboxes();
        updateCheckboxes();
    }

    private void initCheckboxes() {
        Notification[] notifications = Notification.values();
        settingsNotificationsPanelView.setCheckboxes(notifications);

        NotificationCheckboxesChanged listener = new NotificationCheckboxesChanged();
        for (Notification notification : notifications) {
            settingsNotificationsPanelView.checkboxAddActionListener(notification, listener);
        }
    }

    private void updateCheckboxes() {
        for (Notification notification : Notification.values()) {
            boolean isSelected = notificationConfigModel.isNotificationEnabled(notification);
            settingsNotificationsPanelView.setCheckboxSelected(notification, isSelected);
        }
    }

    private class NotificationCheckboxesChanged implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            for (Notification notification : Notification.values()) {
                boolean isSelected = settingsNotificationsPanelView.isCheckboxSelected(notification);
                notificationConfigModel.setNotificationEnabled(notification, isSelected);
            }
        }
    }

    @Override
    public String getTabTitle() {
        return tr("Notifications");
    }

    @Override
    public Component getTabView() {
        return settingsNotificationsPanelView;
    }
}
