package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.openstreetmap.josm.plugins.plbuildings.enums.Notification;

public class SettingsNotificationsPanel extends JPanel {

    private static final String ENABLED_NOTIFICATONS = tr("Enabled notifications") + ":";
    private Map<String, JCheckBox> notificationCheckboxes;
    private final JPanel checkboxesPanel;

    public SettingsNotificationsPanel() {
        super();

        this.checkboxesPanel = new JPanel();
        checkboxesPanel.setLayout(new BoxLayout(checkboxesPanel, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout());
        add(checkboxesPanel, BorderLayout.CENTER);
    }

    public void checkboxAddActionListener(Notification notification, ActionListener listener) {
        notificationCheckboxes.get(notification.getName()).addActionListener(listener);
    }

    public void setCheckboxSelected(Notification notification, boolean selected) {
        notificationCheckboxes.get(notification.getName()).setSelected(selected);
    }

    public void setCheckboxes(Notification[] notifications) {
        checkboxesPanel.removeAll();
        notificationCheckboxes = new LinkedHashMap<>();

        for (Notification notification : notifications) {
            JCheckBox checkBox = new JCheckBox(notification.getLabel());
            notificationCheckboxes.put(notification.getName(), checkBox);
            checkboxesPanel.add(checkBox);
        }

        checkboxesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkboxesPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(ENABLED_NOTIFICATONS)
        ));
    }

    public boolean isCheckboxSelected(Notification notification) {
        return notificationCheckboxes.get(notification.getName()).isSelected();
    }
}
