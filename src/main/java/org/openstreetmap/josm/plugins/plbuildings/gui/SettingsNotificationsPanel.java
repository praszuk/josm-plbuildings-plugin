package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.openstreetmap.josm.plugins.plbuildings.enums.Notification;
import org.openstreetmap.josm.plugins.plbuildings.enums.NotificationType;

public class SettingsNotificationsPanel extends JPanel {
    private Map<String, JCheckBox> notificationCheckboxes;
    private final JPanel checkBoxPanelGroups;

    public SettingsNotificationsPanel() {
        super(new BorderLayout());

        this.checkBoxPanelGroups = new JPanel();
        checkBoxPanelGroups.setLayout(new GridBagLayout());

        add(checkBoxPanelGroups, BorderLayout.NORTH);
    }

    public void checkboxAddActionListener(Notification notification, ActionListener listener) {
        notificationCheckboxes.get(notification.getName()).addActionListener(listener);
    }

    public void setCheckboxSelected(Notification notification, boolean selected) {
        notificationCheckboxes.get(notification.getName()).setSelected(selected);
    }

    public void setCheckboxes(Notification[] notifications) {
        checkBoxPanelGroups.removeAll();
        notificationCheckboxes = new LinkedHashMap<>();

        LinkedHashMap<NotificationType, List<JCheckBox>> groupedCheckBoxes = new LinkedHashMap<>();
        groupedCheckBoxes.put(NotificationType.IMPORT_STATUS, new ArrayList<>());
        groupedCheckBoxes.put(NotificationType.DATA_SOURCES, new ArrayList<>());

        for (Notification notification : notifications) {
            JCheckBox checkBox = new JCheckBox(notification.getLabel());
            notificationCheckboxes.put(notification.getName(), checkBox);
            groupedCheckBoxes.get(notification.getType()).add(checkBox);
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Stretch horizontally

        checkBoxPanelGroups.add(
            createCheckBoxPanel(tr("Import status change"), groupedCheckBoxes.get(NotificationType.IMPORT_STATUS)), gbc
        );

        gbc.gridy++;
        checkBoxPanelGroups.add(
            createCheckBoxPanel(tr("Data sources"), groupedCheckBoxes.get(NotificationType.DATA_SOURCES)), gbc
        );
    }

    public boolean isCheckboxSelected(Notification notification) {
        return notificationCheckboxes.get(notification.getName()).isSelected();
    }

    private JPanel createCheckBoxPanel(String title, List<JCheckBox> checkBoxes) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(title)
        ));
        checkBoxes.forEach(panel::add);

        return panel;
    }
}
