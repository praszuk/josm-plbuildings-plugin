package org.openstreetmap.josm.plugins.plbuildings.gui;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.plbuildings.BuildingsPlugin;
import org.openstreetmap.josm.plugins.plbuildings.data.ImportStatus;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Create sidebar window which contains the latest status of import action and allows to change some cfg.
 */
public class BuildingsToggleDialog extends ToggleDialog {
    private final JLabel status;
    private final JLabel building;
    private final JLabel bLevels;
    private final JLabel extraTags;

    private final JComboBox<String> dataSourceComboBox;

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

        // this.setPreferredSize(new Dimension(0, 50));
        this.status = new JLabel("");
        this.dataSourceComboBox = new JComboBox<>(new String[]{"BDOT"});
        this.building = new JLabel("");
        this.bLevels = new JLabel("");
        this.extraTags = new JLabel("");

        JPanel rootPanel = new JPanel(new GridLayout(0, 1));

        JPanel configPanel = new JPanel(new GridLayout(2,2));
        JLabel statusLabel = new JLabel(tr("Status") + ": ");
        statusLabel.setBorder(new EmptyBorder(0,5,0,0));

        configPanel.add(statusLabel);
        configPanel.add(status);

        JLabel dataSourceLabel = new JLabel(tr("Data source") + ": ");
        dataSourceLabel.setBorder(new EmptyBorder(0,5,0,0));
        dataSourceComboBox.setEnabled(false);
        dataSourceComboBox.setSelectedIndex(0);

        configPanel.add(dataSourceLabel);
        configPanel.add(dataSourceComboBox);

        JPanel lastImportTagsPanel = new JPanel(new GridLayout(0, 2));

        lastImportTagsPanel.add(new JLabel("building: "));
        lastImportTagsPanel.add(building);

        lastImportTagsPanel.add(new JLabel("building:levels: "));
        lastImportTagsPanel.add(bLevels);

        lastImportTagsPanel.add(new JLabel("extraTags: "));
        lastImportTagsPanel.add(extraTags);

        lastImportTagsPanel.setBorder(BorderFactory.createTitledBorder(tr("Latest tags")));
        rootPanel.add(configPanel);

        rootPanel.add(lastImportTagsPanel);

        rootPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        createLayout(rootPanel, true, null);

        setDefaultStatus();
        updateTags("", "", false);
    }

    private void setDefaultStatus(){
        Logging.debug("Changing status to default");
        GuiHelper.scheduleTimer(
            1500,
            actionEvent -> this.status.setText(ImportStatus.IDLE.toString()),
            false
        );
    }

    public void setStatus(@Nonnull ImportStatus status, boolean autoChangeToDefault) {
        GuiHelper.runInEDT(() ->{
            Logging.info("Changing status to: {0}", status);
            this.status.setText(status.toString());
        });

        if (autoChangeToDefault){
            setDefaultStatus();
        }

    }

    public void setStatus(@Nonnull ImportStatus status) {
        setStatus(status, true);
    }

    public void updateTags(String buildingVal, String bLevelsVal, boolean extraTags){
        GuiHelper.runInEDT(() -> {
            Logging.info(
                "Updating tags: building: {0}, building:levels: {1}, extraTags: {2}",
                buildingVal,
                bLevelsVal,
                extraTags
            );
            this.building.setText(buildingVal.isEmpty() ? "--":buildingVal);
            this.bLevels.setText(bLevelsVal.isEmpty() ? "--":bLevelsVal);
            this.extraTags.setText(extraTags ? "true":"false");
        });
    }



}
