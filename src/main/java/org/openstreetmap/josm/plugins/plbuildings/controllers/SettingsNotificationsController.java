package org.openstreetmap.josm.plugins.plbuildings.controllers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openstreetmap.josm.plugins.plbuildings.gui.SettingsNotificationsPanel;
import org.openstreetmap.josm.plugins.plbuildings.models.NotifiableImportStatuses;

public class SettingsNotificationsController implements SettingsTabController {
    private final NotifiableImportStatuses notifiableImportStatusesModel;
    private final SettingsNotificationsPanel settingsNotificationsPanelView;

    public SettingsNotificationsController(NotifiableImportStatuses notifiableImportStatusesModel,
                                           SettingsNotificationsPanel settingsNotificationsPanelView) {
        this.notifiableImportStatusesModel = notifiableImportStatusesModel;
        this.settingsNotificationsPanelView = settingsNotificationsPanelView;

        notifiableImportStatusesModel.addPropertyChangeListener(
            NotifiableImportStatuses.NOTIFIABLE_IMPORT_STATUSES, propertyChangeEvent -> updateCheckboxes()
        );

        initCheckboxes();
        updateCheckboxes();
    }

    private void initCheckboxes() {
        settingsNotificationsPanelView.setCheckboxes(NotifiableImportStatuses.getNotifiableStatusesNames());
        NotificationCheckboxesChanged listener = new NotificationCheckboxesChanged();

        for (int i = 0; i < NotifiableImportStatuses.notifiableStatuses.size(); i++) {
            settingsNotificationsPanelView.checkboxAddActionListener(i, listener);
        }
    }

    private void updateCheckboxes() {
        for (int i = 0; i < NotifiableImportStatuses.notifiableStatuses.size(); i++) {
            boolean isSelected = notifiableImportStatusesModel.isNotifiable(
                NotifiableImportStatuses.notifiableStatuses.get(i)
            );
            settingsNotificationsPanelView.setCheckboxSelected(i, isSelected);
        }
    }

    private class NotificationCheckboxesChanged implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            for (int i = 0; i < NotifiableImportStatuses.notifiableStatuses.size(); i++) {
                boolean isSelected = settingsNotificationsPanelView.isCheckboxSelected(i);
                notifiableImportStatusesModel.setNotifiable(
                    NotifiableImportStatuses.notifiableStatuses.get(i), isSelected);
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
