package org.openstreetmap.josm.plugins.plbuildings.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SettingsNotificationsPanel extends JPanel {

    private static final String ENABLED_NOTIFICATONS = tr("Enabled notifications") + ":";
    private JCheckBox[] notificationCheckboxes;
    private final JPanel checkboxesPanel;

    public SettingsNotificationsPanel() {
        super();

        this.checkboxesPanel = new JPanel();
        checkboxesPanel.setLayout(new BoxLayout(checkboxesPanel, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout());
        add(checkboxesPanel, BorderLayout.CENTER);
    }

    /**
     * @param index should correspond to checkboxesNames
     */
    public void checkboxAddActionListener(int index, ActionListener listener) {
        notificationCheckboxes[index].addActionListener(listener);
    }

    /**
     * @param index should correspond to checkboxesNames
     */
    public void setCheckboxSelected(int index, boolean selected) {
        notificationCheckboxes[index].setSelected(selected);
    }

    public void setCheckboxes(String[] checkboxesNames) {
        checkboxesPanel.removeAll();
        notificationCheckboxes = new JCheckBox[checkboxesNames.length];

        for (int i = 0; i < checkboxesNames.length; i++) {
            notificationCheckboxes[i] = new JCheckBox(checkboxesNames[i]);
            checkboxesPanel.add(notificationCheckboxes[i]);
        }

        checkboxesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkboxesPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(ENABLED_NOTIFICATONS)
        ));
    }

    /**
     * @param index should correspond to checkboxesNames
     */
    public boolean isCheckboxSelected(int index) {
        return notificationCheckboxes[index].isSelected();
    }
}
