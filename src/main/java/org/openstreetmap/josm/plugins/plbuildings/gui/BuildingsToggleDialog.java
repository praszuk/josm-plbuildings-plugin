package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Create sidebar window which contains the latest status of import action and allows to change some cfg.
 */
public class BuildingsToggleDialog extends ToggleDialog {
    private static final int DATA_SOURCE_PROFILE_MAX_CHARS = 20;

    private final JLabel status;

    private final JComboBox<Object> dataSourceProfilesComboBox;

    private final JLabel buildingType;

    private final JLabel buildingLevels;
    private final JLabel hasUncommonTag;

    public BuildingsToggleDialog() {
        super(
            "PlBuildings",
            "plbuildings",
            tr("Open the {0} window", BuildingsPlugin.info.name),
            Shortcut.registerShortcut(
                "plbuildings:window",
                tr("PlBuildings window"),
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE
            ),
            150
        );

        this.status = new JLabel("");
        this.dataSourceProfilesComboBox = new JComboBox<>();
        this.dataSourceProfilesComboBox.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null){
                    return this;
                }
                String profileName = (String) value;
                setText(profileName.substring(0, Math.min(DATA_SOURCE_PROFILE_MAX_CHARS, profileName.length())));
                return this;
            }
        });

        this.buildingType = new JLabel("");
        this.buildingLevels = new JLabel("");
        this.hasUncommonTag = new JLabel("");

        JPanel rootPanel = new JPanel(new GridLayout(0, 1));

        JPanel configPanel = new JPanel(new GridLayout(2,2));
        JLabel statusLabel = new JLabel(tr("Status") + ": ");
        statusLabel.setBorder(new EmptyBorder(0,5,0,0));

        configPanel.add(statusLabel);
        configPanel.add(status);

        JLabel dataSourceLabel = new JLabel(tr("Data source") + ": ");
        dataSourceLabel.setBorder(new EmptyBorder(0,5,0,0));

        configPanel.add(dataSourceLabel);
        configPanel.add(dataSourceProfilesComboBox);

        JPanel lastImportTagsPanel = new JPanel(new GridLayout(0, 2));

        lastImportTagsPanel.add(new JLabel("building: "));
        lastImportTagsPanel.add(buildingType);

        lastImportTagsPanel.add(new JLabel("building:levels: "));
        lastImportTagsPanel.add(buildingLevels);

        lastImportTagsPanel.add(new JLabel(tr("Uncommon tags") +": "));
        lastImportTagsPanel.add(hasUncommonTag);

        lastImportTagsPanel.setBorder(BorderFactory.createTitledBorder(tr("Latest tags")));
        rootPanel.add(configPanel);

        rootPanel.add(lastImportTagsPanel);

        rootPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        createLayout(rootPanel, true, null);
    }


    public int getDataSourceProfilesComboBoxSelectedIndex() {
        return dataSourceProfilesComboBox.getSelectedIndex();
    }

    public void addDataSourceProfilesComboBoxActionListener(ActionListener listener){
        dataSourceProfilesComboBox.addActionListener(listener);
    }

    public void setBuildingTypeText(String buildingTypeText) {
        buildingType.setText(buildingTypeText);
    }
    public void setBuildingLevelsText(String buildingLevelsText) {
        buildingLevels.setText(buildingLevelsText);
    }
    public void setHasUncommonTagText(String hasUncommonTagText) {
        hasUncommonTag.setText(hasUncommonTagText);
    }
    public void setStatusText(String statusText){
        status.setText(statusText);
    }

    public void setBuildingTypeForeground(Color color){
        buildingType.setForeground(color);
    }
    public void setHasUncommonTagForeground(Color color){
        hasUncommonTag.setForeground(color);
    }
    public void setStatusForeground(Color color){
        status.setForeground(color);
    }

    public void setDataSourceProfilesComboBoxModel(ComboBoxModel<Object> model){
        dataSourceProfilesComboBox.setModel(model);
    }
    public void setDataSourceProfilesComboBoxSelectedIndex(int index) {
        dataSourceProfilesComboBox.setSelectedIndex(index);
    }

}
